package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.security.AuthGuard;
import com.leyi.service.OrderService;
import com.leyi.util.RequestValueParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request, @RequestParam(required = false) String status) {
        Long userId = authGuard.requireUser(request);
        return Result.success(orderService.getByUserId(userId, status));
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = authGuard.requireUser(request);
        return Result.success(orderService.getByIdForUser(id, userId));
    }

    @PostMapping("/create")
    public Result<?> create(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = authGuard.requireUser(request);
        String remark = RequestValueParser.getTrimmedString(params, "remark");
        return Result.success(orderService.create(userId, remark));
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Long id) {
        Long userId = authGuard.requireUser(request);
        orderService.cancelByUser(id, userId);
        return Result.success();
    }
}
