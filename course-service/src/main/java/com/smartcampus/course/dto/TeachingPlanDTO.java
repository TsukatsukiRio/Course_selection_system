package com.smartcampus.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教学计划提交请求（UC-06）。
 */
@Data
public class TeachingPlanDTO {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    private String outline;

    private String schedule;

    private String textbook;
}
