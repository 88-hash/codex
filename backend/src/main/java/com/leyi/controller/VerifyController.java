package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Order;
import com.leyi.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/verify")
public class VerifyController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/search")
    public Result<?> search(@RequestParam String verifyCode) {
        Order order = orderService.getByVerifyCode(verifyCode);
        if (order == null) {
            return Result.error("未找到待核销的订单");
        }
        return Result.success(order);
    }

    @GetMapping("/orders")
    public Result<?> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.getList(status, keyword, pageNum, pageSize));
    }

    @PostMapping("/confirm/{orderId}")
    public Result<?> confirm(HttpServletRequest request, @PathVariable Long orderId) {
        // 安全获取 adminId，防止 ClassCastException (Integer cannot be cast to Long)
        Object adminIdObj = request.getAttribute("adminId");
        Long adminId = null;
        if (adminIdObj != null) {
            if (adminIdObj instanceof Integer) {
                adminId = ((Integer) adminIdObj).longValue();
            } else if (adminIdObj instanceof Long) {
                adminId = (Long) adminIdObj;
            } else {
                try {
                    adminId = Long.valueOf(adminIdObj.toString());
                } catch (NumberFormatException e) {
                    System.err.println("adminId 转换失败: " + adminIdObj);
                }
            }
        }
        
        String adminName = (String) request.getAttribute("phone");
        System.out.println("尝试核销订单: orderId=" + orderId + ", adminId=" + adminId + ", adminName=" + adminName);
        
        try {
            orderService.verify(orderId, adminId, adminName);
            return Result.success("核销成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Long orderId, @RequestBody Map<String, String> params) {
        Long adminId = (Long) request.getAttribute("adminId");
        String adminName = (String) request.getAttribute("phone");
        String reason = params.get("reason");
        try {
            orderService.adminCancel(orderId, adminId, adminName, reason);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}



