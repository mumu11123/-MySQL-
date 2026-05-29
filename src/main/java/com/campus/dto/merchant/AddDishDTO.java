package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "菜品新增请求参数")
public class AddDishDTO {

    @NotBlank(message = "菜品名称不能为空")
    @Schema(description = "菜品名称")
    private String name;

    @NotBlank(message = "菜品分类不能为空")
    @Schema(description = "菜品分类")
    private String category;

    @NotNull(message = "菜品单价不能为空")
    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "菜品描述")
    private String description;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "折扣，如 0.85 表示85折")
    private BigDecimal discount;

    @Schema(description = "口味")
    private String taste;

    @NotNull(message = "库存状态不能为空")
    @Schema(description = "库存状态 1有库存 0无库存")
    private Integer stockStatus;
}