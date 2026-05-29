package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long merchantId;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;

    private String payMethod;
    private Integer status;       // 订单状态 0待支付 2待接单 3制作中 4待取餐 5已完成 6已取消

    private String cancelReason;
    private LocalDateTime paidAt;
    private LocalDateTime finishedAt;

    @TableField(exist = false)
    private String phone;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
