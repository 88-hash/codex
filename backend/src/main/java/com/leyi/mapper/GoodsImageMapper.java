package com.leyi.mapper;

import com.leyi.entity.GoodsImage;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface GoodsImageMapper {
    
    @Select("SELECT * FROM goods_image WHERE goods_id = #{goodsId} ORDER BY sort ASC")
    List<GoodsImage> findByGoodsId(Long goodsId);
    
    @Insert("INSERT INTO goods_image(goods_id, image_url, sort) VALUES(#{goodsId}, #{imageUrl}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GoodsImage image);
    
    @Delete("DELETE FROM goods_image WHERE goods_id = #{goodsId}")
    int deleteByGoodsId(Long goodsId);
    
    @Delete("DELETE FROM goods_image WHERE id = #{id}")
    int delete(Long id);
}



