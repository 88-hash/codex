package com.leyi.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsImage {
    private Long id;
    private Long goodsId;
    private String imageUrl;
    private Integer sort;
    private LocalDateTime createdAt;
}



