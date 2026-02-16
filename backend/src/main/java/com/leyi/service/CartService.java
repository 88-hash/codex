package com.leyi.service;

import com.leyi.entity.Cart;
import com.leyi.entity.Goods;
import com.leyi.mapper.CartMapper;
import com.leyi.mapper.GoodsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    public List<Cart> getByUserId(Long userId) {
        return cartMapper.findByUserId(userId);
    }

    public List<Cart> getCheckedByUserId(Long userId) {
        return cartMapper.findCheckedByUserId(userId);
    }

    public void add(Long userId, Long goodsId, Integer quantity) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods == null || goods.getIsOnSale() != 1) {
            throw new RuntimeException("商品不存在或已下架");
        }

        Cart existing = cartMapper.findByUserIdAndGoodsId(userId, goodsId);
        if (existing != null) {
            int newQuantity = existing.getQuantity() + quantity;
            if (newQuantity > goods.getStock()) {
                newQuantity = goods.getStock();
            }
            cartMapper.updateQuantity(existing.getId(), newQuantity);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setQuantity(Math.min(quantity, goods.getStock()));
            cart.setIsChecked(1);
            cartMapper.insert(cart);
        }
    }

    public void updateQuantity(Long id, Integer quantity) {
        cartMapper.updateQuantity(id, quantity);
    }

    public void updateChecked(Long id, Integer isChecked) {
        cartMapper.updateChecked(id, isChecked);
    }

    public void updateAllChecked(Long userId, Integer isChecked) {
        cartMapper.updateAllChecked(userId, isChecked);
    }

    public void delete(Long id) {
        cartMapper.delete(id);
    }

    public void deleteChecked(Long userId) {
        cartMapper.deleteChecked(userId);
    }
}



