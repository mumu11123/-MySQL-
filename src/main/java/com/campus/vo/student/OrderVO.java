package com.campus.vo.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long merchantId;
    private String merchantName;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime finishedAt;
    private List<OrderItemVO> items;

    @Data
    public static class OrderItemVO {
        private Long dishId;
        private String dishName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
