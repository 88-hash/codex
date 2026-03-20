package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Order;
import com.leyi.security.AuthContext;
import com.leyi.security.AuthGuard;
import com.leyi.service.OrderService;
import com.leyi.util.RequestValueParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/verify")
public class VerifyController {

    private static final Pattern VERIFY_CODE_PATTERN = Pattern.compile("^\\d{6}$");

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/search")
    public Result<?> search(HttpServletRequest request, @RequestParam String verifyCode) {
        authGuard.requireAdmin(request);
        if (!VERIFY_CODE_PATTERN.matcher(verifyCode == null ? "" : verifyCode.trim()).matches()) {
            return Result.error(400, "请输入6位取货码");
        }
        Order order = orderService.getByVerifyCode(verifyCode.trim());
        if (order == null) {
            return Result.error("未找到待核销的订单");
        }
        return Result.success(order);
    }

    @GetMapping("/orders")
    public Result<?> getOrders(
            HttpServletRequest request,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        authGuard.requireAdmin(request);
        return Result.success(orderService.getList(status, keyword, pageNum, pageSize));
    }

    @PostMapping("/confirm/{orderId}")
    public Result<?> confirm(HttpServletRequest request, @PathVariable Long orderId) {
        Long adminId = authGuard.requireAdmin(request);
        orderService.verify(orderId, adminId, AuthContext.getPhone(request));
        return Result.success("核销成功");
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Long orderId, @RequestBody Map<String, String> params) {
        Long adminId = authGuard.requireAdmin(request);
        String reason = RequestValueParser.getTrimmedString(params, "reason");
        orderService.adminCancel(orderId, adminId, AuthContext.getPhone(request), reason);
        return Result.success("取消成功");
    }
}
