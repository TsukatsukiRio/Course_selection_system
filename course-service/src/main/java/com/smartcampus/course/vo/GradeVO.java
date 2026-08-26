package com.smartcampus.course.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩视图对象。
 */
@Data
public class GradeVO {

    private Long id;
    private String studentNo;
    private String courseNo;
    private String courseType;
    private String semester;
    private BigDecimal score;
    private LocalDateTime updateTime;
}
