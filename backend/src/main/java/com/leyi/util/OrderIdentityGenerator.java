package com.leyi.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OrderIdentityGenerator {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final int MAX_SEQUENCE = 9999;

    private long lastEpochSecond = -1L;
    private int sequence = -1;

    public synchronized String nextOrderNo() {
        long currentSecond = currentEpochSecond();
        if (currentSecond != lastEpochSecond) {
            lastEpochSecond = currentSecond;
            sequence = 0;
        } else {
            sequence++;
            if (sequence > MAX_SEQUENCE) {
                currentSecond = waitNextSecond(currentSecond);
                lastEpochSecond = currentSecond;
                sequence = 0;
            }
        }
        return "LY" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + String.format("%04d", sequence);
    }

    public String nextVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private long waitNextSecond(long currentSecond) {
        long nextSecond = currentSecond;
        while (nextSecond <= currentSecond) {
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("等待生成订单号时被中断", e);
            }
            nextSecond = currentEpochSecond();
        }
        return nextSecond;
    }

    private long currentEpochSecond() {
        return System.currentTimeMillis() / 1000;
    }
}
