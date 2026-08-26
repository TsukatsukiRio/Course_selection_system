package com.smartcampus.selection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 选课服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.smartcampus")
@MapperScan("com.smartcampus.selection.mapper")
@EnableFeignClients(basePackages = "com.smartcampus.selection.feign")
public class SelectionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SelectionServiceApplication.class, args);
    }
}
