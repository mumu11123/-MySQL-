package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "菜品修改请求参数")
public class UpdateDishDTO {

    @NotNull(message = "菜品ID不能为空")
    @Schema(description = "菜品ID")
    private Long id;

    @Schema(description = "菜品名称")
    private String name;

    @Schema(description = "菜品分类")
    private String category;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "口味")
    private String taste;

    @Schema(description = "库存状态 1有库存 0无库存")
    private Integer stockStatus;
}