package com.campus.service;

import com.campus.dto.merchant.DateRangeDTO;
import com.campus.dto.merchant.DishRankDTO;
import com.campus.vo.merchant.DailySalesVO;
import com.campus.vo.merchant.DishRankVO;
import com.campus.vo.merchant.HourlySalesVO;
import com.campus.vo.merchant.SalesOverviewVO;

import java.util.List;
import java.util.Map;

public interface SalesStatsService {

    /**
     * 首页统计
     * @param merchantId 商家ID，从Token中获取
     * @return 首页统计数据
     */
    Map<String, Object> getHomeStats(Long merchantId);

    /**
     * 销售总览
     * @param merchantId 商家ID，从Token中获取
     * @param dto 查询参数
     * @return 销售总览数据
     */
    SalesOverviewVO getSalesOverview(Long merchantId, DateRangeDTO dto);

    /**
     * 时段销售数据
     * @param merchantId 商家ID，从Token中获取
     * @param date 日期
     * @return 各时段销售数据列表
     */
    List<HourlySalesVO> getHourlySales(Long merchantId, String date);

    /**
     * 每日明细
     * @param merchantId 商家ID，从Token中获取
     * @param dto 查询参数
     * @return 每日销售明细列表
     */
    List<DailySalesVO> getDailySalesList(Long merchantId, DateRangeDTO dto);

    /**
     * 菜品排行
     * @param merchantId 商家ID，从Token中获取
     * @param dto 查询参数
     * @return 菜品销售排行列表
     */
    List<DishRankVO> getDishRank(Long merchantId, DishRankDTO dto);
}