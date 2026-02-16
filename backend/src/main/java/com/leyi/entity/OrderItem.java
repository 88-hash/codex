package com.leyi.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long goodsId;
    private String goodsName;
    private String goodsImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer isCommented;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



