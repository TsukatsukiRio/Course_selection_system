package com.smartcampus.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 推荐服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.smartcampus")
@EnableFeignClients(basePackages = "com.smartcampus.recommend.feign")
public class RecommendServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendServiceApplication.class, args);
    }
}
