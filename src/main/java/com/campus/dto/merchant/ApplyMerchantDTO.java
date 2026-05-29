package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "商家入驻申请请求参数")
public class ApplyMerchantDTO {

    @NotBlank(message = "店铺名称不能为空")
    @Schema(description = "店铺名称", example = "校园小吃店")
    private String shopName;

    @NotBlank(message = "经营分类不能为空")
    @Schema(description = "经营分类", example = "餐饮/快餐/超市")
    private String category;

    @NotBlank(message = "联系人不能为空")
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @NotBlank(message = "联系电话不能为空")
    @Schema(description = "联系电话", example = "13800138000")
    private String phone;

    @NotBlank(message = "店铺地址不能为空")
    @Schema(description = "店铺地址", example = "校园食堂二楼")
    private String address;

    @Schema(description = "营业执照图片URL", example = "https://xxx.com/license.jpg")
    private String businessLicense;
}