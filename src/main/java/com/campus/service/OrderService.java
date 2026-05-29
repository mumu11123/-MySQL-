package com.campus.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Orders;
import java.util.List;

public interface OrderService extends IService<Orders> {

    /**
     * 商家查看自己的订单列表
     * @param merchantId 商家ID，从Token中获取
     * @param status 订单状态
     * @param orderNo 订单编号（模糊查询）
     * @param startDate 自定义查询开始时间
     * @param endDate 自定义查询结束时间
     * @return 订单列表
     */
    List<Orders> listByMerchant(Long merchantId, Integer status, String orderNo, String startDate,String endDate);

    /**
     * 商家接单
     * @param orderId 订单ID
     * @param merchantId 商家ID，从Token中获取
     */
    void acceptOrder(Long orderId, Long merchantId);

    /**
     * 批量接单
     * @param orderIds 订单ID列表
     * @param merchantId 商家ID，从Token中获取
     */
    void batchAccept(List<Long> orderIds, Long merchantId);

    /**
     * 商家出餐
     * @param orderId 订单ID
     * @param merchantId 商家ID，从Token中获取
     */
    void outMealOrder(Long orderId, Long merchantId);

    /**
     * 批量出餐
     * @param orderIds 订单ID列表
     * @param merchantId 商家ID，从Token中获取
     */
    void batchOutMeal(List<Long> orderIds, Long merchantId);

    /**
     * 商家取消订单
     * @param orderId 订单ID
     * @param reason 取消原因
     * @param merchantId 商家ID，从Token中获取
     */
    void cancelOrder(Long orderId, String reason, Long merchantId);

    /**
     * 学生确认取餐
     * @param orderId 订单ID
     * @param userId 学生ID，从Token中获取
     */
    void confirmPickup(Long orderId, Long userId);
}
