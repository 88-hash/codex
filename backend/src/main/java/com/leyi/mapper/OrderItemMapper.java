package com.leyi.mapper;

import com.leyi.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

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
    List<Map<String, Object>> getTopSales(@Param("days") Integer days, @Param("limit") Integer limit);

    @Select("SELECT COALESCE(parent.name, child.name) as categoryName, COALESCE(SUM(oi.subtotal), 0) as amount " +
            "FROM order_item oi " +
            "JOIN `order` o ON oi.order_id = o.id " +
            "JOIN goods g ON oi.goods_id = g.id " +
            "JOIN category child ON g.category_id = child.id " +
            "LEFT JOIN category parent ON child.parent_id = parent.id " +
            "WHERE o.status = 'completed' AND o.verify_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY COALESCE(parent.name, child.name) " +
            "ORDER BY amount DESC")
    List<Map<String, Object>> getCategorySales(Integer days);
}
