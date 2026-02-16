package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Comment;
import com.leyi.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/goods/{goodsId}")
    public Result<?> getByGoodsId(@PathVariable Long goodsId) {
        return Result.success(commentService.getByGoodsId(goodsId));
    }

    @GetMapping("/my")
    public Result<?> getMyComments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(commentService.getByUserId(userId));
    }

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody Comment comment) {
        Long userId = (Long) request.getAttribute("userId");
        String phone = (String) request.getAttribute("phone");
        comment.setUserId(userId);
        comment.setUserPhone(phone);
        try {
            commentService.add(comment);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}



