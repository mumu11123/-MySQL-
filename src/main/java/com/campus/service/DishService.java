package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.merchant.AddDishDTO;
import com.campus.dto.merchant.UpdateDishDTO;
import com.campus.entity.Dish;
import java.util.List;

public interface DishService extends IService<Dish> {

    /**
     * 商家查看自己的菜品列表
     * @param merchantId 商家ID，从Token中获取
     * @param name 菜品名称（模糊查询）
     * @param category 菜品分类
     * @param shelfStatus 上架状态筛选
     * @return 菜品列表
     */
    List<Dish> listByMerchant(Long merchantId, String name, String category, Integer shelfStatus);

    /**
     * 商家新增菜品
     * @param dto 菜品信息
     * @param merchantId 商家ID，从Token中获取
     */
    void addDish(AddDishDTO dto, Long merchantId);

    /**
     * 商家修改菜品
     * @param dto 菜品信息
     * @param merchantId 商家ID，从Token中获取
     */
    void updateDish(UpdateDishDTO dto, Long merchantId);

    /**
     * 商家上下架菜品
     * @param dishId 菜品ID
     * @param status 上架状态（0-下架，1-上架）
     * @param merchantId 商家ID，从Token中获取
     */
    void changeShelfStatus(Long dishId, Integer status, Long merchantId);

    /**
     * 批量上下架菜品
     * @param dishIds 菜品ID列表
     * @param status 上架状态（0-下架，1-上架）
     * @param merchantId 商家ID，从Token中获取
     */
    void batchChangeShelfStatus(List<Long> dishIds, Integer status, Long merchantId);
}