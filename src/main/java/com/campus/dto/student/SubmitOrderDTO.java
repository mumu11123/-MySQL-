package com.campus.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "提交订单请求")
public class SubmitOrderDTO {

    @Schema(description = "商家ID", example = "1")
    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @Schema(description = "订单菜品列表")
    @Valid
    @NotEmpty(message = "订单菜品不能为空")
    private List<OrderDishDTO> dishes;

    @Data
    @Schema(description = "订单菜品项")
    public static class OrderDishDTO {

        @Schema(description = "菜品ID", example = "1")
        @NotNull(message = "菜品ID不能为空")
        private Long dishId;

        @Schema(description = "购买数量", example = "2")
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
