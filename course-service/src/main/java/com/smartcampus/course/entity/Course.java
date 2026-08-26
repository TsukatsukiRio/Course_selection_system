package com.smartcampus.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体，对应表 course。
 */
@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程编号，唯一，如 CS101 */
    private String courseNo;

    private String name;

    /** 授课教师工号 */
    private String teacherNo;

    private String teacherName;

    /** 学分 1-6 */
    private Integer credits;

    /** 上课时间，如：周一 1-2节, 周三 3-4节 */
    private String classTime;

    private String location;

    private Integer capacity;

    private Integer selectedCount;

    /** 先修课程编号，逗号分隔 */
    private String prerequisite;

    private String college;

    /** REQUIRED必修 / ELECTIVE选修 / GENERAL通识 */
    private String courseType;

    private String description;

    /** 1开放 0停开 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
