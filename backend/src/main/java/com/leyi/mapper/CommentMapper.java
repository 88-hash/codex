package com.leyi.mapper;

import com.leyi.entity.Comment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CommentMapper {
    
    @Select("SELECT c.*, u.name as user_name, u.avatar as user_avatar " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.goods_id = #{goodsId} ORDER BY c.created_at DESC")
    List<Comment> findByGoodsId(Long goodsId);
    
    @Select("SELECT c.*, u.name as user_name, u.avatar as user_avatar " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} ORDER BY c.created_at DESC")
    List<Comment> findByUserId(Long userId);
    
    @Select("SELECT * FROM comment WHERE order_item_id = #{orderItemId}")
    Comment findByOrderItemId(Long orderItemId);
    
    @Insert("INSERT INTO comment(order_item_id, user_id, user_phone, goods_id, goods_name, rating, content) " +
            "VALUES(#{orderItemId}, #{userId}, #{userPhone}, #{goodsId}, #{goodsName}, #{rating}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Comment comment);
    
    @Select("SELECT AVG(rating) FROM comment WHERE goods_id = #{goodsId}")
    Double getAverageRating(Long goodsId);
    
    @Select("SELECT COUNT(*) FROM comment WHERE goods_id = #{goodsId}")
    int countByGoodsId(Long goodsId);
}



