package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Category;
import com.leyi.exception.BusinessException;
import com.leyi.security.AuthGuard;
import com.leyi.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthGuard authGuard;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(categoryService.getTree());
    }

    @GetMapping("/all")
    public Result<?> all(HttpServletRequest request) {
        authGuard.requireManager(request);
        return Result.success(categoryService.getAll());
    }

    @PostMapping("/add")
    public Result<?> add(HttpServletRequest request, @RequestBody Category category) {
        authGuard.requireManager(request);
        validateCategory(category, false);
        categoryService.add(category);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(HttpServletRequest request, @RequestBody Category category) {
        authGuard.requireManager(request);
        validateCategory(category, true);
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        authGuard.requireManager(request);
        categoryService.delete(id);
        return Result.success();
    }

    private void validateCategory(Category category, boolean requireId) {
        if (requireId && category.getId() == null) {
            throw new BusinessException(400, "分类ID不能为空");
        }
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new BusinessException(400, "分类名称不能为空");
        }
    }
}
