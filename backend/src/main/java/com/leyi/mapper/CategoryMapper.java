package com.leyi.mapper;

import com.leyi.entity.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CategoryMapper {
    
    @Select("SELECT * FROM category WHERE parent_id = 0 ORDER BY sort ASC, id ASC")
    List<Category> findFirstLevel();
    
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} ORDER BY sort ASC, id ASC")
    List<Category> findByParentId(Long parentId);
    
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(Long id);
    
    @Select("SELECT * FROM category ORDER BY parent_id ASC, sort ASC, id ASC")
    List<Category> findAll();
    
    @Insert("INSERT INTO category(parent_id, name, sort) VALUES(#{parentId}, #{name}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);
    
    @Update("UPDATE category SET name = #{name}, sort = #{sort}, updated_at = NOW() WHERE id = #{id}")
    int update(Category category);
    
    @Delete("DELETE FROM category WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM goods WHERE category_id = #{categoryId} AND is_deleted = 0")
    int countGoodsByCategoryId(Long categoryId);
}



