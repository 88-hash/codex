package com.leyi.mapper;

import com.leyi.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(String phone);
    
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
    
    @Insert("INSERT INTO user(phone, name, avatar) VALUES(#{phone}, #{name}, #{avatar})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE user SET name = #{name}, avatar = #{avatar}, updated_at = NOW() WHERE id = #{id}")
    int update(User user);
    
    @Update("UPDATE user SET updated_at = NOW() WHERE id = #{id}")
    int updateLoginTime(Long id);
}



