package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.merchant.ReplyMessageDTO;
import com.campus.entity.Message;
import com.campus.entity.Orders;
import com.campus.mapper.MessageMapper;
import com.campus.mapper.OrdersMapper;
import com.campus.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    private final OrdersMapper ordersMapper;

    public static final int STATUS_SENT = 1;        // 已发送
    public static final int STATUS_READ = 2;        // 已读

    @Override
    public long getUnreadCount(Long merchantId) {
        return lambdaQuery()
                .eq(Message::getReceiverType, "merchant")
                .eq(Message::getReceiverId, merchantId)
                .eq(Message::getStatus, STATUS_SENT)
                .count();
    }

    @Override
    public List<Message> getUnreadMessagesByOrderIds(List<Long> orderIds, Long merchantId) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery()
                .in(Message::getOrderId, orderIds)
                .eq(Message::getReceiverType, "merchant")
                .eq(Message::getReceiverId, merchantId)
                .eq(Message::getStatus, STATUS_SENT)
                .list();
    }

    @Override
    public List<Message> getMessageListByOrderId(Long orderId, Long merchantId) {
        return lambdaQuery()
                .eq(Message::getOrderId, orderId)
                .and(w -> w
                        .eq(Message::getSenderType, "merchant")
                        .eq(Message::getReceiverType, "user")
                        .or()
                        .eq(Message::getSenderType, "user")
                        .eq(Message::getReceiverType, "merchant")
                )
                .orderByAsc(Message::getCreatedAt)
                .list();
    }

    @Override
    public void markReadByOrder(Long orderId, Long merchantId, String readerType) {
        // 校验订单归属
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作该订单的消息");
        }

        // 商家读消息 → 把 user 发送给 merchant 的未读消息(status=1) 改为已读(status=2)
        String senderType = "merchant".equals(readerType) ? "user" : "merchant";
        String receiverType = readerType;
        Long receiverId = "merchant".equals(readerType) ? merchantId : order.getUserId();

        lambdaUpdate()
                .eq(Message::getOrderId, orderId)
                .eq(Message::getSenderType, senderType)
                .eq(Message::getReceiverType, receiverType)
                .eq(Message::getReceiverId, receiverId)
                .eq(Message::getStatus, STATUS_SENT)  // 只改已发送未读的
                .set(Message::getStatus, STATUS_READ)
                .set(Message::getUpdatedAt, LocalDateTime.now())
                .update();
    }

    @Override
    public void replyMessage(ReplyMessageDTO dto, Long merchantId) {
        // 校验订单是否属于当前商家
        Orders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作该订单的消息");
        }

        // 构建消息
        Message message = new Message();
        message.setOrderId(dto.getOrderId());
        message.setSenderType("merchant");
        message.setSenderId(merchantId);
        message.setReceiverType("user");
        message.setReceiverId(dto.getUserId());
        message.setContent(dto.getContent());
        message.setStatus(STATUS_SENT); // 已发送
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        save(message);
    }
}