package com.leyi.service;

import com.leyi.entity.Cart;
import com.leyi.entity.Goods;
import com.leyi.exception.BusinessException;
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
        requirePositive(quantity, "商品数量");

        Goods goods = getAvailableGoods(goodsId);
        if (quantity > goods.getStock()) {
            throw new BusinessException(400, "商品库存不足");
        }

        Cart existing = cartMapper.findByUserIdAndGoodsId(userId, goodsId);
        if (existing != null) {
            int newQuantity = existing.getQuantity() + quantity;
            if (newQuantity > goods.getStock()) {
                throw new BusinessException(400, "商品库存不足");
            }
            cartMapper.updateQuantity(existing.getId(), newQuantity);
            return;
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setGoodsId(goodsId);
        cart.setQuantity(quantity);
        cart.setIsChecked(1);
        cartMapper.insert(cart);
    }

    public void updateQuantity(Long userId, Long id, Integer quantity) {
        requirePositive(quantity, "商品数量");
        Cart cart = getOwnedCart(id, userId);
        Goods goods = getAvailableGoods(cart.getGoodsId());
        if (quantity > goods.getStock()) {
            throw new BusinessException(400, "商品库存不足");
        }
        cartMapper.updateQuantity(cart.getId(), quantity);
    }

    public void updateChecked(Long userId, Long id, Integer isChecked) {
        requireBinaryFlag(isChecked, "选中状态");
        Cart cart = getOwnedCart(id, userId);
        cartMapper.updateChecked(cart.getId(), isChecked);
    }

    public void updateAllChecked(Long userId, Integer isChecked) {
        requireBinaryFlag(isChecked, "选中状态");
        cartMapper.updateAllChecked(userId, isChecked);
    }

    public void delete(Long userId, Long id) {
        Cart cart = getOwnedCart(id, userId);
        cartMapper.delete(cart.getId());
    }

    public void deleteChecked(Long userId) {
        cartMapper.deleteChecked(userId);
    }

    private Cart getOwnedCart(Long id, Long userId) {
        Cart cart = cartMapper.findByIdAndUserId(id, userId);
        if (cart == null) {
            throw new BusinessException(403, "无权操作该购物车记录");
        }
        return cart;
    }

    private Goods getAvailableGoods(Long goodsId) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods == null || goods.getIsOnSale() != 1) {
            throw new BusinessException(400, "商品不存在或已下架");
        }
        if (goods.getStock() == null || goods.getStock() <= 0) {
            throw new BusinessException(400, "商品库存不足");
        }
        return goods;
    }

    private void requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new BusinessException(400, fieldName + "必须为正整数");
        }
    }

    private void requireBinaryFlag(Integer value, String fieldName) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BusinessException(400, fieldName + "参数非法");
        }
    }
}
