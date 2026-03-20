package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.security.AuthGuard;
import com.leyi.service.CartService;
import com.leyi.util.RequestValueParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = authGuard.requireUser(request);
        return Result.success(cartService.getByUserId(userId));
    }

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = authGuard.requireUser(request);
        Long goodsId = RequestValueParser.requireLong(params, "goodsId", "商品ID");
        Integer quantity = RequestValueParser.getInteger(params, "quantity", 1, "商品数量");
        cartService.add(userId, goodsId, quantity);
        return Result.success();
    }

    @PutMapping("/quantity/{id}")
    public Result<?> updateQuantity(HttpServletRequest request, @PathVariable Long id, @RequestParam Integer quantity) {
        Long userId = authGuard.requireUser(request);
        cartService.updateQuantity(userId, id, quantity);
        return Result.success();
    }

    @PutMapping("/checked/{id}")
    public Result<?> updateChecked(HttpServletRequest request, @PathVariable Long id, @RequestParam Integer isChecked) {
        Long userId = authGuard.requireUser(request);
        cartService.updateChecked(userId, id, isChecked);
        return Result.success();
    }

    @PutMapping("/checkedAll")
    public Result<?> updateAllChecked(HttpServletRequest request, @RequestParam Integer isChecked) {
        Long userId = authGuard.requireUser(request);
        cartService.updateAllChecked(userId, isChecked);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = authGuard.requireUser(request);
        cartService.delete(userId, id);
        return Result.success();
    }
}
