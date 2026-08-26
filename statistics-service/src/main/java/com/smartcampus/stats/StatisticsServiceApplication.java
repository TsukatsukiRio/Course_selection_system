package com.smartcampus.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 统计服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.smartcampus")
@EnableFeignClients(basePackages = "com.smartcampus.stats.feign")
public class StatisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsServiceApplication.class, args);
    }
}
