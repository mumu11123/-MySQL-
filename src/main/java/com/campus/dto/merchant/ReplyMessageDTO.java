package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商家回复消息请求参数")
public class ReplyMessageDTO {
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "学生用户ID")
    private Long userId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "回复内容")
    private String content;
}