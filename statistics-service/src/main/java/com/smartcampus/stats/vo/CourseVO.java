package com.smartcampus.stats.vo;

import lombok.Data;

/**
 * 课程信息（Feign 远程传输用）。
 */
@Data
public class CourseVO {

    private Long id;
    private String courseNo;
    private String name;
    private String teacherName;
    private Integer credits;
    private String college;
    private String courseType;
    private Integer capacity;
    private Integer selectedCount;
}
