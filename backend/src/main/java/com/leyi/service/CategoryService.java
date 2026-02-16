package com.leyi.service;

import com.leyi.entity.Category;
import com.leyi.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> getTree() {
        List<Category> firstLevel = categoryMapper.findFirstLevel();
        for (Category category : firstLevel) {
            category.setChildren(categoryMapper.findByParentId(category.getId()));
        }
        return firstLevel;
    }

    public List<Category> getAll() {
        return categoryMapper.findAll();
    }

    public Category getById(Long id) {
        return categoryMapper.findById(id);
    }

    public void add(Category category) {
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.update(category);
    }

    public void delete(Long id) {
        int count = categoryMapper.countGoodsByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("该分类下还有商品，无法删除");
        }
        List<Category> children = categoryMapper.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("请先删除子分类");
        }
        categoryMapper.delete(id);
    }
}



