package com.smartcampus.selection.vo;

import lombok.Data;

/**
 * 课程信息（Feign 远程传输用）。
 */
@Data
public class CourseVO {

    private Long id;
    private String courseNo;
    private String name;
    private String teacherNo;
    private String teacherName;
    private Integer credits;
    private String classTime;
    private String location;
    private Integer capacity;
    private Integer selectedCount;
    private String prerequisite;
    private String college;
    private String courseType;
    private String description;
    private Integer status;
}
