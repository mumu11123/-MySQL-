package com.campus.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.dto.merchant.DateRangeDTO;
import com.campus.dto.merchant.DishRankDTO;
import com.campus.entity.Dish;
import com.campus.entity.OrderItem;
import com.campus.entity.Orders;
import com.campus.mapper.DishMapper;
import com.campus.mapper.OrderItemMapper;
import com.campus.mapper.OrdersMapper;
import com.campus.service.SalesStatsService;
import com.campus.vo.merchant.DailySalesVO;
import com.campus.vo.merchant.DishRankVO;
import com.campus.vo.merchant.HourlySalesVO;
import com.campus.vo.merchant.SalesOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesStatsServiceImpl implements SalesStatsService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;

    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, Object> getHomeStats(Long merchantId) {
        // 在售菜品数
        Long dishCount = dishMapper.selectCount(
                Wrappers.lambdaQuery(Dish.class)
                        .eq(Dish::getMerchantId, merchantId)
                        .eq(Dish::getShelfStatus, 1)
                        .eq(Dish::getIsDeleted, 0)
        );

        // 待接单数
        Long waitOrderCount = ordersMapper.selectCount(
                Wrappers.lambdaQuery(Orders.class)
                        .eq(Orders::getMerchantId, merchantId)
                        .eq(Orders::getStatus, 2)
        );

        // 今日完成数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayFinishCount = ordersMapper.selectCount(
                Wrappers.lambdaQuery(Orders.class)
                        .eq(Orders::getMerchantId, merchantId)
                        .eq(Orders::getStatus, 5)
                        .between(Orders::getFinishedAt, todayStart, todayEnd)
        );

        Map<String, Object> stats = new HashMap<>();
        stats.put("dishCount", dishCount.intValue());
        stats.put("waitOrderCount", waitOrderCount.intValue());
        stats.put("todayFinishCount", todayFinishCount.intValue());
        return stats;
    }

    @Override
    public SalesOverviewVO getSalesOverview(Long merchantId, DateRangeDTO dto) {
        // 解析时间范围
        DateRange range = parseDateRange(dto);

        // 当前周期数据
        List<Orders> currentOrders = getFinishedOrders(merchantId, range.start(), range.end());

        // 上一周期数据（环比）
        DateRange prevRange = getPrevRange(range);
        List<Orders> prevOrders = getFinishedOrders(merchantId, prevRange.start(), prevRange.end());

        // 计算当前周期
        BigDecimal totalAmount = currentOrders.stream()
                .map(Orders::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalOrders = currentOrders.size();
        int totalDishes = countTotalDishes(currentOrders);
        BigDecimal avgPrice = totalOrders > 0
                ? totalAmount.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 计算环比
        BigDecimal prevAmount = prevOrders.stream()
                .map(Orders::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SalesOverviewVO vo = new SalesOverviewVO();
        vo.setTotalAmount(totalAmount);
        vo.setTotalOrders(totalOrders);
        vo.setTotalDishes(totalDishes);
        vo.setAvgPrice(avgPrice);
        vo.setAmountRingRatio(calcRingRatio(totalAmount, prevAmount));
        vo.setOrderRingRatio(calcRingRatio(
                new BigDecimal(totalOrders),
                new BigDecimal(prevOrders.size())
        ));
        return vo;
    }

    @Override
    public List<HourlySalesVO> getHourlySales(Long merchantId, String date) {
        LocalDate targetDate = date != null
                ? LocalDate.parse(date, DATE_FORMAT)
                : LocalDate.now();

        LocalDateTime dayStart = LocalDateTime.of(targetDate, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(targetDate, LocalTime.MAX);

        // 查询当日已完成订单
        List<Orders> orders = ordersMapper.selectList(
                Wrappers.lambdaQuery(Orders.class)
                        .eq(Orders::getMerchantId, merchantId)
                        .eq(Orders::getStatus, 5)
                        .between(Orders::getFinishedAt, dayStart, dayEnd)
        );

        // 按小时分组统计
        Map<Integer, List<Orders>> hourlyGroup = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getFinishedAt().getHour()));

        // 构建全天24小时数据（每2小时一个点，共12个时段）
        List<HourlySalesVO> result = new ArrayList<>();
        for (int h = 0; h <= 22; h += 2) {  // 0,2,4,6,8,10,12,14,16,18,20,22
            String hourStr = String.format("%02d:00", h);

            // 统计该2小时时段内的订单（h 和 h+1）
            List<Orders> hourOrders = new ArrayList<>();
            hourOrders.addAll(hourlyGroup.getOrDefault(h, List.of()));
            hourOrders.addAll(hourlyGroup.getOrDefault(h + 1, List.of()));

            BigDecimal amount = hourOrders.stream()
                    .map(Orders::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            HourlySalesVO vo = new HourlySalesVO();
            vo.setHour(hourStr);
            vo.setAmount(amount);
            vo.setOrders(hourOrders.size());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DailySalesVO> getDailySalesList(Long merchantId, DateRangeDTO dto) {
        DateRange range;
        if (dto.getQuickType() != null) {
            range = parseQuickType(dto.getQuickType());
        } else {
            range = parseDateRange(dto.getStartDate(), dto.getEndDate());
        }

        List<Orders> orders = getFinishedOrders(merchantId, range.start(), range.end());

        // 按日期分组
        Map<String, List<Orders>> dailyGroup = orders.stream()
                .collect(Collectors.groupingBy(o -> {
                    if (o.getFinishedAt() != null) {
                        return o.getFinishedAt().format(DATE_FORMAT);
                    }
                    return o.getCreatedAt().format(DATE_FORMAT);
                }));

        // 计算每日数据
        List<DailySalesVO> result = new ArrayList<>();
        String prevDate = null;
        BigDecimal prevAmount = BigDecimal.ZERO;

        // 按日期排序
        List<String> sortedDates = new ArrayList<>(dailyGroup.keySet());
        Collections.sort(sortedDates);

        for (String date : sortedDates) {
            List<Orders> dayOrders = dailyGroup.get(date);
            BigDecimal dailyAmount = dayOrders.stream()
                    .map(Orders::getActualAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DailySalesVO vo = new DailySalesVO();
            vo.setDate(date);
            vo.setDailyAmount(dailyAmount);
            vo.setDailyOrders(dayOrders.size());
            vo.setDailyDishes(countTotalDishes(dayOrders));
            vo.setDailyAvgPrice(!dayOrders.isEmpty()
                    ? dailyAmount.divide(new BigDecimal(dayOrders.size()), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            // 环比
            if (prevDate != null) {
                vo.setRingRatio(calcRingRatio(dailyAmount, prevAmount));
            } else {
                vo.setRingRatio("-");
            }

            result.add(vo);
            prevDate = date;
            prevAmount = dailyAmount;
        }

        return result;
    }

    @Override
    public List<DishRankVO> getDishRank(Long merchantId, DishRankDTO dto) {
        DateRange range;
        if (dto.getQuickType() != null) {
            range = parseQuickType(dto.getQuickType());
        } else {
            range = parseDateRange(dto.getStartDate(), dto.getEndDate());
        }

        // 获取当前周期已完成订单
        List<Orders> orders = getFinishedOrders(merchantId, range.start(), range.end());
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询订单明细
        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class)
                        .in(OrderItem::getOrderId, orderIds)
        );

        // 按菜品分组统计
        Map<String, List<OrderItem>> dishGroup = items.stream()
                .collect(Collectors.groupingBy(OrderItem::getDishName));

        // 计算总销售额
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 构建排行数据
        List<DishRankVO> rankList = new ArrayList<>();
        for (Map.Entry<String, List<OrderItem>> entry : dishGroup.entrySet()) {
            List<OrderItem> dishItems = entry.getValue();
            DishRankVO vo = new DishRankVO();
            vo.setDishName(entry.getKey());
            vo.setSaleCount(dishItems.stream().mapToInt(OrderItem::getQuantity).sum());
            vo.setSaleAmount(dishItems.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setAmountPercent(vo.getSaleAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(totalAmount, 2, RoundingMode.HALF_UP)
                        + "%");
            } else {
                vo.setAmountPercent("0%");
            }
            rankList.add(vo);
        }

        // 默认按销量降序排序
        rankList.sort((a, b) -> b.getSaleCount() - a.getSaleCount());

        // 限制条数
        Integer limit = dto.getLimit();
        if (limit != null && limit > 0 && limit < rankList.size()) {
            return rankList.subList(0, limit);
        }
        return rankList;
    }

    // ==================== 工具方法 ====================

    /**
     * 查询指定商家在指定时间范围内的已完成订单
     * @param merchantId 商家ID，从Token中获取
     * @param start 查询起始时间
     * @param end 查询结束时间
     * @return 已完成订单列表
     */
    private List<Orders> getFinishedOrders(Long merchantId, LocalDateTime start, LocalDateTime end) {
        return ordersMapper.selectList(
                Wrappers.lambdaQuery(Orders.class)
                        .eq(Orders::getMerchantId, merchantId)
                        .eq(Orders::getStatus, 5)
                        .between(Orders::getFinishedAt, start, end)
        );
    }

    /**
     * 统计订单列表中的菜品总数量
     * @param orders 订单列表
     * @return 菜品总数量
     */
    private int countTotalDishes(List<Orders> orders) {
        List<Long> orderIds = orders.stream().map(Orders::getId).collect(Collectors.toList());
        if (orderIds.isEmpty()) return 0;

        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.lambdaQuery(OrderItem.class)
                        .in(OrderItem::getOrderId, orderIds)
        );
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    /**
     * 解析销售总览DTO中的时间范围
     * @param dto 销售总览查询参数
     * @return 解析后的时间范围
     */
    private DateRange parseDateRange(DateRangeDTO dto) {
        if (dto.getQuickType() != null) {
            return parseQuickType(dto.getQuickType());
        }
        return parseDateRange(dto.getStartDate(), dto.getEndDate());
    }

    /**
     * 解析字符串格式的起止日期为时间范围
     * @param startDate 起始日期，格式yyyy-MM-dd，为空则取当天
     * @param endDate 结束日期，格式yyyy-MM-dd，为空则取当天
     * @return 解析后的时间范围
     */
    private DateRange parseDateRange(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate, DATE_FORMAT) : LocalDate.now();
        LocalDate end = endDate != null ? LocalDate.parse(endDate, DATE_FORMAT) : LocalDate.now();
        return new DateRange(
                LocalDateTime.of(start, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX)
        );
    }

    /**
     * 根据快捷类型解析时间范围
     * @param quickType 快捷类型（today-今日，week-本周，month-本月）
     * @return 解析后的时间范围
     */
    private DateRange parseQuickType(String quickType) {
        LocalDate today = LocalDate.now();
        return switch (quickType) {
            case "today" -> new DateRange(
                    LocalDateTime.of(today, LocalTime.MIN),
                    LocalDateTime.of(today, LocalTime.MAX)
            );
            case "week" -> {
                LocalDate weekStart = today.with(WeekFields.of(Locale.CHINA).dayOfWeek(), 1);
                yield new DateRange(
                        LocalDateTime.of(weekStart, LocalTime.MIN),
                        LocalDateTime.of(today, LocalTime.MAX)
                );
            }
            case "month" -> {
                LocalDate monthStart = today.withDayOfMonth(1);
                yield new DateRange(
                        LocalDateTime.of(monthStart, LocalTime.MIN),
                        LocalDateTime.of(today, LocalTime.MAX)
                );
            }
            default -> parseDateRange(null, null);
        };
    }

    /**
     * 获取上一个周期的时间范围（用于环比计算）
     * @param current 当前周期时间范围
     * @return 上一周期时间范围
     */
    private DateRange getPrevRange(DateRange current) {
        long days = java.time.Duration.between(current.start(), current.end()).toDays() + 1;
        return new DateRange(
                current.start().minusDays(days),
                current.end().minusDays(days)
        );
    }

    /**
     * 计算环比增长率
     * @param current 当前周期数值
     * @param previous 上一周期数值
     * @return 格式化后的环比字符串（如"+12.34%"、"-5.67%"）
     */
    private String calcRingRatio(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        BigDecimal ratio = current.subtract(previous)
                .multiply(new BigDecimal("100"))
                .divide(previous, 2, RoundingMode.HALF_UP);
        return (ratio.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + ratio + "%";
    }

    /**
     * 时间范围内部记录类
     * @param start 起始时间
     * @param end 结束时间
     */
    public record DateRange(LocalDateTime start, LocalDateTime end) { }
}