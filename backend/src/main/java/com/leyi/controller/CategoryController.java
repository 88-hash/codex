package com.leyi.controller;

import com.leyi.common.Result;
import com.leyi.entity.Category;
import com.leyi.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(categoryService.getTree());
    }

    @GetMapping("/all")
    public Result<?> all() {
        return Result.success(categoryService.getAll());
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}



