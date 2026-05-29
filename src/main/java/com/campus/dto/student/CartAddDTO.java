package com.campus.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "添加购物车请求")
public class CartAddDTO {

    @Schema(description = "菜品ID", example = "1")
    @NotNull(message = "菜品ID不能为空")
    private Long dishId;

    @Schema(description = "添加数量", example = "2")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
