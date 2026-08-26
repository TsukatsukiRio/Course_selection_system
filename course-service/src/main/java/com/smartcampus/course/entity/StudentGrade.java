package com.smartcampus.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生成绩实体，对应表 student_grade。
 */
@Data
@TableName("student_grade")
public class StudentGrade {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;

    private String courseNo;

    private String courseType;

    private String semester;

    private BigDecimal score;

    private LocalDateTime updateTime;
}
