package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.merchant.AddDishDTO;
import com.campus.dto.merchant.UpdateDishDTO;
import com.campus.entity.Dish;
import com.campus.mapper.DishMapper;
import com.campus.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Override
    public List<Dish> listByMerchant(Long merchantId, String name, String category, Integer shelfStatus) {
        return lambdaQuery()
                .eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getIsDeleted, 0)
                .like(StringUtils.hasText(name), Dish::getName, name)
                .like(StringUtils.hasText(category), Dish::getCategory, category)
                .eq(shelfStatus != null, Dish::getShelfStatus, shelfStatus)
                .orderByAsc(Dish::getId)
                .list();
    }

    @Override
    public void addDish(AddDishDTO dto, Long merchantId) {
        Dish dish = new Dish();
        dish.setMerchantId(merchantId);
        dish.setName(dto.getName());
        dish.setCategory(dto.getCategory());
        PriceInfo priceInfo = normalizePrice(dto.getPrice(), dto.getOriginalPrice(), dto.getDiscount());
        dish.setPrice(priceInfo.price());
        dish.setOriginalPrice(priceInfo.originalPrice());
        dish.setDiscount(priceInfo.discount());
        dish.setTaste(dto.getTaste());
        dish.setDescription(dto.getDescription());
        dish.setImageUrl(dto.getImageUrl());
        dish.setStockStatus(dto.getStockStatus());
        dish.setMonthlySales(0);
        dish.setShelfStatus(1);
        dish.setIsDeleted(0);
        dish.setCreatedAt(LocalDateTime.now());
        dish.setUpdatedAt(LocalDateTime.now());
        save(dish);
    }

    @Override
    public void updateDish(UpdateDishDTO dto, Long merchantId) {
        Dish exist = getById(dto.getId());

        if (exist == null || !exist.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权修改此菜品");
        }

        PriceInfo priceInfo = normalizePrice(
                dto.getPrice() != null ? dto.getPrice() : exist.getPrice(),
                dto.getOriginalPrice() != null ? dto.getOriginalPrice() : exist.getOriginalPrice(),
                dto.getDiscount() != null ? dto.getDiscount() : exist.getDiscount()
        );

        Dish dish = new Dish();
        dish.setId(dto.getId());
        dish.setName(dto.getName() != null ? dto.getName() : exist.getName());
        dish.setCategory(dto.getCategory() != null ? dto.getCategory() : exist.getCategory());
        dish.setPrice(priceInfo.price());
        dish.setOriginalPrice(priceInfo.originalPrice());
        dish.setDiscount(priceInfo.discount());
        dish.setTaste(dto.getTaste() != null ? dto.getTaste() : exist.getTaste());
        dish.setDescription(dto.getDescription() != null ? dto.getDescription() : exist.getDescription());

        // 图片保存（不传则保留原来的）
        dish.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl() : exist.getImageUrl());

        dish.setStockStatus(dto.getStockStatus() != null ? dto.getStockStatus() : exist.getStockStatus());
        dish.setUpdatedAt(LocalDateTime.now());

        updateById(dish);
    }

    private PriceInfo normalizePrice(BigDecimal price, BigDecimal originalPrice, BigDecimal discount) {
        BigDecimal normalizedDiscount = discount != null ? discount : BigDecimal.ONE;
        BigDecimal normalizedOriginalPrice = originalPrice;
        BigDecimal normalizedPrice = price;

        if (normalizedOriginalPrice == null) {
            normalizedOriginalPrice = normalizedPrice;
        }

        if (normalizedOriginalPrice != null
                && normalizedDiscount.compareTo(BigDecimal.ZERO) > 0
                && normalizedDiscount.compareTo(BigDecimal.ONE) < 0) {
            normalizedPrice = normalizedOriginalPrice.multiply(normalizedDiscount).setScale(2, RoundingMode.HALF_UP);
        }

        if (normalizedPrice == null) {
            throw new RuntimeException("菜品单价不能为空");
        }

        return new PriceInfo(normalizedPrice, normalizedOriginalPrice, normalizedDiscount);
    }

    private record PriceInfo(BigDecimal price, BigDecimal originalPrice, BigDecimal discount) {
    }

    @Override
    public void changeShelfStatus(Long dishId, Integer status, Long merchantId) {
        if (status != 0 && status != 1) {
            throw new RuntimeException("上下架状态只能为0（下架）或1（上架）");
        }
        Dish dish = getById(dishId);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("无权操作此菜品");
        }
        dish.setShelfStatus(status);
        dish.setUpdatedAt(LocalDateTime.now());
        updateById(dish);
    }

    @Override
    public void batchChangeShelfStatus(List<Long> dishIds, Integer status, Long merchantId) {
        if (status != 0 && status != 1) {
            throw new RuntimeException("上下架状态只能为0（下架）或1（上架）");
        }
        if (dishIds == null || dishIds.isEmpty()) {
            throw new RuntimeException("请选择菜品");
        }

        lambdaUpdate()
                .in(Dish::getId, dishIds)
                .eq(Dish::getMerchantId, merchantId)
                .set(Dish::getShelfStatus, status)
                .set(Dish::getUpdatedAt, LocalDateTime.now())
                .update();
    }
}
