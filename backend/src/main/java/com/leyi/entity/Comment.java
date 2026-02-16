package com.leyi.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long orderItemId;
    private Long userId;
    private String userPhone;
    private Long goodsId;
    private String goodsName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName;
    private String userAvatar;
}



