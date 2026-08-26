package com.smartcampus.recommend.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成绩信息（Feign 远程传输用）。
 */
@Data
public class GradeVO {

    private Long id;
    private String studentNo;
    private String courseNo;
    private String courseType;
    private String semester;
    private BigDecimal score;
}
