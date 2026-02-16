package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.service.AdminService;
import com.leyi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @PostMapping("/sendCode")
    public Result<?> sendCode(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.length() != 11) {
            return Result.error("请输入正确的手机号");
        }
        userService.sendCode(phone);
        return Result.success("验证码已发送");
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String code = params.get("code");
        try {
            Map<String, Object> result = userService.login(phone, code);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/admin/login")
    public Result<?> adminLogin(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        String password = params.get("password");
        try {
            Map<String, Object> result = adminService.login(phone, password);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<?> getInfo(HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if ("admin".equals(userType)) {
            Long adminId = (Long) request.getAttribute("adminId");
            return Result.success(adminService.getById(adminId));
        } else {
            Long userId = (Long) request.getAttribute("userId");
            return Result.success(userService.getById(userId));
        }
    }
}



