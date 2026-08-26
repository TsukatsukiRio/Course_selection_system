package com.smartcampus.selection.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课记录视图：记录 + 课程信息。
 */
@Data
public class SelectionVO {

    private Long id;
    private String studentNo;
    private String courseNo;
    private String semester;
    private String status;
    private LocalDateTime selectTime;
    private LocalDateTime dropTime;

    /** 课程信息（Feign 查询补充） */
    private CourseVO course;
}
