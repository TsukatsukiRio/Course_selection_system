package com.smartcampus.course.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程视图对象。
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
    private LocalDateTime createTime;
}
