package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "商家营业状态修改")
public class StatusMerchantDTO {

    @NotNull(message = "状态不能为空")
    @Schema(description = "1营业中 2暂停营业", example = "1")
    private Integer status;
}