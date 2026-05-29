package com.campus.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "菜品排行数据")
public class DishRankVO {

    @Schema(description = "菜品名称")
    private String dishName;

    @Schema(description = "销量")
    private Integer saleCount;

    @Schema(description = "销售额")
    private BigDecimal saleAmount;

    @Schema(description = "销售额占比（%）")
    private String amountPercent;
}