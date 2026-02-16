package com.leyi.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Long id;
    private Long userId;
    private String userPhone;
    private String orderNo;
    private BigDecimal totalPrice;
    private String status;
    private String verifyCode;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime verifyTime;
    private LocalDateTime updatedAt;
    private List<OrderItem> items;
}



