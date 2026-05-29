package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.OrderItem;
import com.campus.entity.Orders;
import com.campus.mapper.DishMapper;
import com.campus.mapper.OrderItemMapper;
import com.campus.mapper.OrdersMapper;
import com.campus.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.campus.service.impl.SalesStatsServiceImpl.DATE_FORMAT;

@Service
@RequiredArgsConstructor


public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;

    @Override
    public List<Orders> listByMerchant(Long merchantId, Integer status, String orderNo, String startDate, String endDate) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            start = LocalDateTime.of(LocalDate.parse(startDate, DATE_FORMAT), LocalTime.MIN);
            end = LocalDateTime.of(LocalDate.parse(endDate, DATE_FORMAT), LocalTime.MAX);
        }

        return ordersMapper.selectOrdersWithPhone(merchantId, status, orderNo, start, end);
    }

    @Override
    public void acceptOrder(Long orderId, Long merchantId) {
        Orders order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getMerchantId().equals(merchantId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 2) throw new RuntimeException("当前订单状态无法接单");

        order.setStatus(3); // 制作中
        updateById(order);
    }

    @Override
    public void batchAccept(List<Long> orderIds, Long merchantId) {
        for (Long id : orderIds) {
            acceptOrder(id, merchantId);
        }
    }

    @Override
    public void outMealOrder(Long orderId, Long merchantId) {
        Orders order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getMerchantId().equals(merchantId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() != 3) throw new RuntimeException("当前订单状态无法出餐");

        order.setStatus(4); // 待取餐
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void batchOutMeal(List<Long> orderIds, Long merchantId) {
        for (Long id : orderIds) {
            outMealOrder(id, merchantId);
        }
    }

    @Override
    public void cancelOrder(Long orderId, String reason, Long merchantId) {
        Orders order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getMerchantId().equals(merchantId)) throw new RuntimeException("无权操作");

        int status = order.getStatus();
        if (status != 2 && status != 3) {
            throw new RuntimeException("只有待接单/制作中可取消");
        }

        order.setStatus(6);
        order.setCancelReason(reason);
        order.setFinishedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional
    public void confirmPickup(Long orderId, Long userId) {
        Orders order = getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (!Integer.valueOf(4).equals(order.getStatus())) {
            throw new RuntimeException("只有待取餐订单可以确认取餐");
        }

        order.setStatus(5);
        order.setFinishedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        List<OrderItem> items = orderItemMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(OrderItem.class)
                        .eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            dishMapper.increaseMonthlySales(item.getDishId(), item.getQuantity());
        }
    }
}
