package com.smartcampus.course.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 教师录入成绩请求。
 */
@Data
public class GradeDTO {

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "课程编号不能为空")
    private String courseNo;

    private String semester;

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能小于0")
    @DecimalMax(value = "100", message = "成绩不能大于100")
    private BigDecimal score;
}
