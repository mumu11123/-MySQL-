package com.campus.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @Update("UPDATE dish SET monthly_sales = COALESCE(monthly_sales, 0) + #{quantity}, updated_at = NOW() WHERE id = #{dishId}")
    int increaseMonthlySales(@Param("dishId") Long dishId, @Param("quantity") Integer quantity);

    @Update("UPDATE dish SET monthly_sales = 0")
    int resetMonthlySales();

    @Update("""
            UPDATE dish d
            LEFT JOIN (
                SELECT oi.dish_id, SUM(oi.quantity) AS sales
                FROM order_item oi
                INNER JOIN orders o ON oi.order_id = o.id
                WHERE o.status = 5
                  AND o.finished_at >= #{start}
                  AND o.finished_at <= #{end}
                GROUP BY oi.dish_id
            ) s ON d.id = s.dish_id
            SET d.monthly_sales = COALESCE(s.sales, 0)
            """)
    int rebuildMonthlySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
