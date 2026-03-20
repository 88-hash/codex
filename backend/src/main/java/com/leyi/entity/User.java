package com.leyi.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String phone;
    private String name;
    private String avatar;
    private String signature;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
