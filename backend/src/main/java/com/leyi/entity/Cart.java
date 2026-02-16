package com.leyi.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer quantity;
    private Integer isChecked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String goodsName;
    private BigDecimal goodsPrice;
    private String goodsImage;
    private Integer goodsStock;
    private Integer goodsIsOnSale;
}



