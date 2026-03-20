package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.User;
import com.leyi.security.AuthGuard;
import com.leyi.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/info")
    public Result<?> getInfo(HttpServletRequest request) {
        Long userId = authGuard.requireUser(request);
        return Result.success(userService.getById(userId));
    }

    @PutMapping("/update")
    public Result<?> update(HttpServletRequest request, @RequestBody User user) {
        Long userId = authGuard.requireUser(request);
        user.setId(userId);
        return Result.success(userService.update(user));
    }
}
