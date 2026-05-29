package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.merchant.ReplyMessageDTO;
import com.campus.entity.Message;
import java.util.List;

public interface MessageService extends IService<Message> {

    /**
     * 获取商家未读消息总数
     * @param merchantId 商家ID，从Token中获取
     * @return 未读消息数量
     */
    long getUnreadCount(Long merchantId);

    /**
     * 按订单ID列表获取未读消息
     * @param orderIds 订单ID列表
     * @param merchantId 商家ID，从Token中获取
     * @return 未读消息列表
     */
    List<Message> getUnreadMessagesByOrderIds(List<Long> orderIds, Long merchantId);

    /**
     * 根据订单ID获取聊天记录
     * @param orderId 订单ID
     * @param merchantId 商家ID，从Token中获取
     * @return 消息列表
     */
    List<Message> getMessageListByOrderId(Long orderId, Long merchantId);

    /**
     * 标记订单消息为已读
     * @param orderId 订单ID
     * @param merchantId 商家ID，从Token中获取
     * @param readerType 阅读者类型（merchant-商家，user-用户）
     */
    void markReadByOrder(Long orderId, Long merchantId, String readerType);

    /**
     * 商家回复消息
     * @param dto 回复消息参数
     * @param merchantId 商家ID，从Token中获取
     */
    void replyMessage(ReplyMessageDTO dto, Long merchantId);
}