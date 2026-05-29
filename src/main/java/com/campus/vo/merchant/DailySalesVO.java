package com.campus.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "每日销售明细")
public class DailySalesVO {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "当日销售额")
    private BigDecimal dailyAmount;

    @Schema(description = "当日订单数")
    private Integer dailyOrders;

    @Schema(description = "当日销量")
    private Integer dailyDishes;

    @Schema(description = "当日客单价")
    private BigDecimal dailyAvgPrice;

    @Schema(description = "环比（%）")
    private String ringRatio;
}