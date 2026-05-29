package com.campus.controller.student;

import com.campus.common.result.Result;
import com.campus.dto.student.SubmitOrderDTO;
import com.campus.entity.Dish;
import com.campus.entity.Merchant;
import com.campus.entity.OrderItem;
import com.campus.entity.Orders;
import com.campus.service.DishService;
import com.campus.service.MerchantService;
import com.campus.service.OrderItemService;
import com.campus.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "学生端-订单")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;  // 新增
    private final DishService dishService;
    private final MerchantService merchantService;

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    @Operation(summary = "提交订单")
    @PostMapping("/orders")
    public Result<Long> submitOrder(@Valid @RequestBody SubmitOrderDTO dto,
                                    @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        Merchant merchant = merchantService.getById(dto.getMerchantId());
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            return Result.error(400, "商家当前非营业状态，暂不能下单");
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (SubmitOrderDTO.OrderDishDTO dishDTO : dto.getDishes()) {
            Dish dish = dishService.getById(dishDTO.getDishId());
            if (dish == null || !dto.getMerchantId().equals(dish.getMerchantId())) {
                return Result.error(400, "菜品不存在或不属于当前商家: " + dishDTO.getDishId());
            }
            if (!Integer.valueOf(1).equals(dish.getShelfStatus())) {
                return Result.error(400, "菜品已下架: " + dish.getName());
            }

            BigDecimal subtotal = dish.getPrice().multiply(BigDecimal.valueOf(dishDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem item = new OrderItem();
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishPrice(dish.getPrice());
            item.setQuantity(dishDTO.getQuantity());
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(dto.getMerchantId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setActualAmount(totalAmount);
        order.setStatus(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderService.save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemService.save(item);
        }

        return Result.success(order.getId());
    }

    @Operation(summary = "支付订单")
    @PutMapping("/orders/{id}/pay")
    public Result<Void> payOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        Orders order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(400, "订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            return Result.error(400, "订单状态不正确，无法支付");
        }
        Merchant merchant = merchantService.getById(order.getMerchantId());
        if (merchant == null || !Integer.valueOf(1).equals(merchant.getStatus())) {
            return Result.error(400, "商家当前非营业状态，暂不能支付");
        }

        order.setStatus(2);
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderService.updateById(order);
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PutMapping("/orders/{id}/cancel")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        Orders order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(400, "订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus()) && !Integer.valueOf(2).equals(order.getStatus())) {
            return Result.error(400, "订单状态不正确，无法取消");
        }

        order.setStatus(6);
        order.setCancelReason("用户主动取消");
        order.setFinishedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderService.updateById(order);
        return Result.success();
    }

    @Operation(summary = "确认取餐")
    @PutMapping("/orders/{id}/confirm-pickup")
    public Result<Void> confirmPickup(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        orderService.confirmPickup(id, userId);
        return Result.success();
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/orders")
    public Result<List<Orders>> getOrders(@Parameter(hidden = true) HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Orders> orders = orderService.lambdaQuery()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreatedAt)
                .list();
        return Result.success(orders);
    }

    @Operation(summary = "订单详情")
    @GetMapping("/orders/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);

        Orders order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error(400, "订单不存在");
        }

        Merchant merchant = merchantService.getById(order.getMerchantId());

        List<OrderItem> items = orderItemService.lambdaQuery()
                .eq(OrderItem::getOrderId, id)
                .list();

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("merchantId", order.getMerchantId());
        result.put("merchantName", merchant != null ? merchant.getShopName() : "商家");
        result.put("totalAmount", order.getTotalAmount());
        result.put("actualAmount", order.getActualAmount());
        result.put("status", order.getStatus());
        result.put("createdAt", order.getCreatedAt());
        result.put("paidAt", order.getPaidAt());
        result.put("finishedAt", order.getFinishedAt());
        result.put("items", items);

        return Result.success(result);
    }
}