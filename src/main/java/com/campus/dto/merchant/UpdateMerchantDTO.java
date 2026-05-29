package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "商家信息修改请求参数")
public class UpdateMerchantDTO {

    @NotBlank(message = "店铺名称不能为空")
    @Schema(description = "店铺名称", example = "校园食堂一号店")
    private String shopName;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @NotBlank(message = "店铺地址不能为空")
    @Schema(description = "店铺地址", example = "校园食堂1楼101档口")
    private String address;

    @Schema(description = "店铺Logo URL", example = "https://xxx.com/logo.png")
    private String logo;

    @Schema(description = "店铺简介", example = "主打营养快餐，支持校园卡支付")
    private String description;
}