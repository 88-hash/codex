package com.leyi.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Category {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Category> children;
}



