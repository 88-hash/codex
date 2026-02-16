package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(cartService.getByUserId(userId));
    }

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long goodsId = Long.valueOf(params.get("goodsId").toString());
        Integer quantity = params.get("quantity") != null ? Integer.valueOf(params.get("quantity").toString()) : 1;
        try {
            cartService.add(userId, goodsId, quantity);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/quantity/{id}")
    public Result<?> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity);
        return Result.success();
    }

    @PutMapping("/checked/{id}")
    public Result<?> updateChecked(@PathVariable Long id, @RequestParam Integer isChecked) {
        cartService.updateChecked(id, isChecked);
        return Result.success();
    }

    @PutMapping("/checkedAll")
    public Result<?> updateAllChecked(HttpServletRequest request, @RequestParam Integer isChecked) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.updateAllChecked(userId, isChecked);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        cartService.delete(id);
        return Result.success();
    }
}



