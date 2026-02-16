package com.leyi.mapper;

import com.leyi.entity.OrderItem;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(Long orderId);
    
    @Select("SELECT * FROM order_item WHERE id = #{id}")
    OrderItem findById(Long id);
    
    @Insert("INSERT INTO order_item(order_id, goods_id, goods_name, goods_image, price, quantity, subtotal) " +
            "VALUES(#{orderId}, #{goodsId}, #{goodsName}, #{goodsImage}, #{price}, #{quantity}, #{subtotal})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItem item);
    
    @Update("UPDATE order_item SET is_commented = 1, updated_at = NOW() WHERE id = #{id}")
    int markCommented(Long id);
    
    @Select("SELECT oi.goods_id, oi.goods_name, SUM(oi.quantity) as total_quantity " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "WHERE o.status = 'completed' AND o.verify_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY oi.goods_id, oi.goods_name " +
            "ORDER BY total_quantity DESC LIMIT #{limit}")
    List<java.util.Map<String, Object>> getTopSales(@Param("days") Integer days, @Param("limit") Integer limit);
}



