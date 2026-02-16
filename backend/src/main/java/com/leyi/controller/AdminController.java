package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Admin;
import com.leyi.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(adminService.getAll());
    }

    @GetMapping("/detail/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(adminService.getById(id));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Admin admin) {
        try {
            adminService.add(admin);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Admin admin) {
        adminService.update(admin);
        return Result.success();
    }

    @PutMapping("/resetPassword/{id}")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String password = params.get("password");
        adminService.resetPassword(id, password);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.delete(id);
        return Result.success();
    }
}



