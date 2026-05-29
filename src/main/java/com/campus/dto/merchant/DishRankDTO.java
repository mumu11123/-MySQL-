package com.campus.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜品排行查询参数")
public class DishRankDTO {

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "分类筛选")
    private String category;

    @Schema(description = "限制条数：3/10/全部")
    private Integer limit;

    @Schema(description = "快捷类型：today/week/month")
    private String quickType;
}