package com.leyi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.leyi.mapper")
public class LeyiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeyiApplication.class, args);
    }
}







