package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "日期范围查询参数")
public class DateRangeDTO {

    @Schema(description = "开始日期 yyyy-MM-dd")
    private String startDate;

    @Schema(description = "结束日期 yyyy-MM-dd")
    private String endDate;

    @Schema(description = "快捷类型：today今日/week本周/month本月")
    private String quickType;
}