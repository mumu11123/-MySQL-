package com.campus.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "销售总览数据")
public class SalesOverviewVO {

    @Schema(description = "总销售额")
    private BigDecimal totalAmount;

    @Schema(description = "总订单数")
    private Integer totalOrders;

    @Schema(description = "总销量（菜品份数）")
    private Integer totalDishes;

    @Schema(description = "客单价")
    private BigDecimal avgPrice;

    @Schema(description = "销售额环比（%）")
    private String amountRingRatio;

    @Schema(description = "订单数环比（%）")
    private String orderRingRatio;
}