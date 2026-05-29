package com.campus.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "时段销售数据")
public class HourlySalesVO {

    @Schema(description = "时段，如 08:00")
    private String hour;

    @Schema(description = "该时段销售额")
    private BigDecimal amount;

    @Schema(description = "该时段订单数")
    private Integer orders;
}