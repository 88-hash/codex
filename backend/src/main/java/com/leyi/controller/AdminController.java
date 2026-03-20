package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Admin;
import com.leyi.exception.BusinessException;
import com.leyi.security.AuthGuard;
import com.leyi.service.AdminService;
import com.leyi.util.RequestValueParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        authGuard.requireManager(request);
        return Result.success(adminService.getAll());
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(HttpServletRequest request, @PathVariable Long id) {
        authGuard.requireManager(request);
        return Result.success(adminService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody Admin admin) {
        authGuard.requireManager(request);
        validateAdmin(admin, false);
        adminService.add(admin);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(HttpServletRequest request, @RequestBody Admin admin) {
        authGuard.requireManager(request);
        validateAdmin(admin, true);
        adminService.update(admin);
        return Result.success();
    }

    @PutMapping("/resetPassword/{id}")
    public Result<?> resetPassword(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, String> params) {
        authGuard.requireManager(request);
        String password = RequestValueParser.requireString(params, "password", "密码");
        adminService.resetPassword(id, password);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        authGuard.requireManager(request);
        adminService.delete(id);
        return Result.success();
    }

    private void validateAdmin(Admin admin, boolean requireId) {
        if (requireId && admin.getId() == null) {
            throw new BusinessException(400, "管理员ID不能为空");
        }
        if (admin.getName() == null || admin.getName().trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }
        if (admin.getPhone() == null || !PHONE_PATTERN.matcher(admin.getPhone().trim()).matches()) {
            throw new BusinessException(400, "请输入正确的手机号");
        }
        if (!"manager".equals(admin.getRole()) && !"clerk".equals(admin.getRole())) {
            throw new BusinessException(400, "角色参数非法");
        }
    }
}
