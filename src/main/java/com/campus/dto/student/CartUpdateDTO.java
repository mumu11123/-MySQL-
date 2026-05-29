package com.campus.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "修改购物车数量请求")
public class CartUpdateDTO {

    @Schema(description = "购物车项ID", example = "1")
    @NotNull(message = "购物车项ID不能为空")
    private Long cartItemId;

    @Schema(description = "修改后的数量", example = "3")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;
}
