package com.leyi.mapper;

import com.leyi.entity.VerifyLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface VerifyLogMapper {
    
    @Select("SELECT * FROM verify_log WHERE order_id = #{orderId} ORDER BY created_at DESC")
    List<VerifyLog> findByOrderId(Long orderId);
    
    @Select("SELECT * FROM verify_log ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<VerifyLog> findList(@Param("offset") Integer offset, @Param("limit") Integer limit);
    
    @Select("SELECT COUNT(*) FROM verify_log")
    Long count();
    
    @Insert("INSERT INTO verify_log(order_id, order_no, admin_id, admin_name, action, remark) " +
            "VALUES(#{orderId}, #{orderNo}, #{adminId}, #{adminName}, #{action}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VerifyLog log);
}



