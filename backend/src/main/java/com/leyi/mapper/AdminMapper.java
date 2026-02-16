package com.leyi.mapper;

import com.leyi.entity.Admin;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AdminMapper {
    
    @Select("SELECT * FROM admin WHERE phone = #{phone} AND is_deleted = 0")
    Admin findByPhone(String phone);
    
    @Select("SELECT * FROM admin WHERE id = #{id} AND is_deleted = 0")
    Admin findById(Long id);
    
    @Select("SELECT * FROM admin WHERE is_deleted = 0 ORDER BY created_at DESC")
    List<Admin> findAll();
    
    @Insert("INSERT INTO admin(name, phone, password, role) VALUES(#{name}, #{phone}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Admin admin);
    
    @Update("UPDATE admin SET name = #{name}, phone = #{phone}, role = #{role}, updated_at = NOW() WHERE id = #{id}")
    int update(Admin admin);
    
    @Update("UPDATE admin SET password = #{password}, updated_at = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Update("UPDATE admin SET is_deleted = 1, updated_at = NOW() WHERE id = #{id}")
    int delete(Long id);
}



