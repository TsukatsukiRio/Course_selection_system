package com.smartcampus.course;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 课程服务启动类。
 */
@SpringBootApplication(scanBasePackages = "com.smartcampus")
@MapperScan("com.smartcampus.course.mapper")
public class CourseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseServiceApplication.class, args);
    }
}
