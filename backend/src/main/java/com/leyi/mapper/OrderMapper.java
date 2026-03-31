package com.leyi.mapper;

import com.leyi.entity.Order;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    List<Order> findByUserId(@Param("userId") Long userId, @Param("status") String status);

    List<Order> findList(@Param("status") String status,
                         @Param("keyword") String keyword,
                         @Param("excludeStatus") String excludeStatus,
                         @Param("offset") Integer offset,
                         @Param("limit") Integer limit);

    Long countList(@Param("status") String status,
                   @Param("keyword") String keyword,
                   @Param("excludeStatus") String excludeStatus);

    @Select("SELECT * FROM `order` WHERE id = #{id}")
    Order findById(Long id);

    @Select("SELECT * FROM `order` WHERE id = #{id} AND user_id = #{userId}")
    Order findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

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

    @Update("UPDATE `order` SET status = #{toStatus}, updated_at = NOW() WHERE id = #{id} AND status = #{fromStatus}")
    int updateStatusIfCurrent(@Param("id") Long id,
                              @Param("fromStatus") String fromStatus,
                              @Param("toStatus") String toStatus);

    @Update("UPDATE `order` SET status = 'completed', verify_time = NOW(), updated_at = NOW() WHERE id = #{id} AND status = 'pending'")
    int verifyPending(Long id);

    @Select("SELECT COUNT(*) FROM `order` WHERE DATE(created_at) = CURDATE()")
    int countTodayOrders();

    @Select("SELECT COALESCE(SUM(total_price), 0) FROM `order` WHERE status = 'completed' AND DATE(verify_time) = CURDATE()")
    BigDecimal getTodaySales();

    @Select("SELECT COALESCE(SUM(total_price), 0) FROM `order` WHERE status = 'completed' AND DATE(verify_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal getYesterdaySales();

    @Select("SELECT COUNT(*) FROM `order` WHERE status = 'completed' AND DATE(verify_time) = CURDATE()")
    int countTodayVerified();

    @Select("SELECT COUNT(*) FROM `order` WHERE status = 'pending'")
    int countPendingOrders();

    @Select("SELECT DATE(verify_time) as date, COALESCE(SUM(total_price), 0) as amount " +
            "FROM `order` WHERE status = 'completed' AND verify_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(verify_time) ORDER BY date ASC")
    List<Map<String, Object>> getSalesTrend(Integer days);

    @Select("SELECT status as status, COUNT(*) as count FROM `order` GROUP BY status")
    List<Map<String, Object>> getOrderStatusSummary();

    @Select("SELECT CONCAT(LPAD(hour_num, 2, '0'), ':00') as hour, count " +
            "FROM (" +
            "  SELECT HOUR(created_at) as hour_num, COUNT(*) as count " +
            "  FROM `order` " +
            "  WHERE DATE(created_at) = CURDATE() " +
            "  GROUP BY HOUR(created_at)" +
            ") t ORDER BY hour_num")
    List<Map<String, Object>> getHourlyOrders();
}
