package com.leyi.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VerifyLog {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long adminId;
    private String adminName;
    private String action;
    private String remark;
    private LocalDateTime createdAt;
}



