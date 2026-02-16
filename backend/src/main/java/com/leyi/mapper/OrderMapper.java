package com.leyi.mapper;

import com.leyi.entity.Order;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper {
    
    List<Order> findByUserId(@Param("userId") Long userId, @Param("status") String status);
    
    List<Order> findList(@Param("status") String status, 
                         @Param("keyword") String keyword,
                         @Param("offset") Integer offset,
                         @Param("limit") Integer limit);
    
    Long countList(@Param("status") String status, @Param("keyword") String keyword);
    
    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order findById(Long id);
    
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);
    
    @Select("SELECT * FROM `order` WHERE verify_code = #{verifyCode} AND status = 'pending'")
    Order findByVerifyCode(String verifyCode);
    
    @Insert("INSERT INTO `order`(user_id, user_phone, order_no, total_price, status, verify_code, remark) " +
            "VALUES(#{userId}, #{userPhone}, #{orderNo}, #{totalPrice}, #{status}, #{verifyCode}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
    
    @Update("UPDATE `order` SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE `order` SET status = 'completed', verify_time = NOW(), updated_at = NOW() WHERE id = #{id}")
    int verify(Long id);
    
    @Select("SELECT COUNT(*) FROM `order` WHERE DATE(created_at) = CURDATE()")
    int countTodayOrders();
    
    @Select("SELECT COALESCE(SUM(total_price), 0) FROM `order` WHERE status = 'completed' AND DATE(verify_time) = CURDATE()")
    BigDecimal getTodaySales();
    
    @Select("SELECT COUNT(*) FROM `order` WHERE status = 'completed' AND DATE(verify_time) = CURDATE()")
    int countTodayVerified();
    
    @Select("SELECT DATE(verify_time) as date, COALESCE(SUM(total_price), 0) as amount " +
            "FROM `order` WHERE status = 'completed' AND verify_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(verify_time) ORDER BY date ASC")
    List<java.util.Map<String, Object>> getSalesTrend(Integer days);
    
    @Select("SELECT MAX(CAST(SUBSTRING(order_no, 11) AS UNSIGNED)) FROM `order` WHERE order_no LIKE CONCAT('LY', #{dateStr}, '%')")
    Integer getMaxOrderNoSeq(String dateStr);
}



