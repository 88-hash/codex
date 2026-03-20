package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Comment;
import com.leyi.security.AuthContext;
import com.leyi.security.AuthGuard;
import com.leyi.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/goods/{goodsId}")
    public Result<?> getByGoodsId(@PathVariable Long goodsId) {
        return Result.success(commentService.getByGoodsId(goodsId));
    }

    @GetMapping("/my")
    public Result<?> getMyComments(HttpServletRequest request) {
        Long userId = authGuard.requireUser(request);
        return Result.success(commentService.getByUserId(userId));
    }

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody Comment comment) {
        Long userId = authGuard.requireUser(request);
        comment.setUserId(userId);
        comment.setUserPhone(AuthContext.getPhone(request));
        commentService.add(userId, comment);
        return Result.success();
    }
}
