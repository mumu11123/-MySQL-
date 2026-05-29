package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 查询商家订单列表（带学生手机号）
     */
    @Select("<script>" +
            "SELECT o.*, u.phone " +
            "FROM orders o " +
            "LEFT JOIN user u ON o.user_id = u.id " +
            "WHERE o.merchant_id = #{merchantId} " +
            "<if test='status != null'> AND o.status = #{status} </if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND o.order_no LIKE CONCAT('%', #{orderNo}, '%') </if>" +
            "<if test='start != null'> AND o.created_at &gt;= #{start} </if>" +
            "<if test='end != null'> AND o.created_at &lt;= #{end} </if>" +
            "ORDER BY o.created_at DESC" +
            "</script>")
    List<Orders> selectOrdersWithPhone(@Param("merchantId") Long merchantId,
                                       @Param("status") Integer status,
                                       @Param("orderNo") String orderNo,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}