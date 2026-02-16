package com.leyi.mapper;

import com.leyi.entity.Cart;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CartMapper {
    
    @Select("SELECT c.*, g.name as goods_name, g.price as goods_price, g.image_url as goods_image, " +
            "g.stock as goods_stock, g.is_on_sale as goods_is_on_sale " +
            "FROM cart c LEFT JOIN goods g ON c.goods_id = g.id " +
            "WHERE c.user_id = #{userId} ORDER BY c.created_at DESC")
    List<Cart> findByUserId(Long userId);
    
    @Select("SELECT c.*, g.name as goods_name, g.price as goods_price, g.image_url as goods_image, " +
            "g.stock as goods_stock, g.is_on_sale as goods_is_on_sale " +
            "FROM cart c LEFT JOIN goods g ON c.goods_id = g.id " +
            "WHERE c.user_id = #{userId} AND c.is_checked = 1")
    List<Cart> findCheckedByUserId(Long userId);
    
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    Cart findByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    
    @Insert("INSERT INTO cart(user_id, goods_id, quantity, is_checked) VALUES(#{userId}, #{goodsId}, #{quantity}, #{isChecked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Cart cart);
    
    @Update("UPDATE cart SET quantity = #{quantity}, updated_at = NOW() WHERE id = #{id}")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    @Update("UPDATE cart SET is_checked = #{isChecked}, updated_at = NOW() WHERE id = #{id}")
    int updateChecked(@Param("id") Long id, @Param("isChecked") Integer isChecked);
    
    @Update("UPDATE cart SET is_checked = #{isChecked}, updated_at = NOW() WHERE user_id = #{userId}")
    int updateAllChecked(@Param("userId") Long userId, @Param("isChecked") Integer isChecked);
    
    @Delete("DELETE FROM cart WHERE id = #{id}")
    int delete(Long id);
    
    @Delete("DELETE FROM cart WHERE user_id = #{userId} AND goods_id IN (${goodsIds})")
    int deleteByGoodsIds(@Param("userId") Long userId, @Param("goodsIds") String goodsIds);
    
    @Delete("DELETE FROM cart WHERE user_id = #{userId} AND is_checked = 1")
    int deleteChecked(Long userId);
}



