package com.smartcampus.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教学计划实体，对应表 teaching_plan。
 */
@Data
@TableName("teaching_plan")
public class TeachingPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private String courseNo;

    /** 教学大纲 */
    private String outline;

    /** 教学进度表 */
    private String schedule;

    /** 参考教材 */
    private String textbook;

    /** 0待审核 1通过 2驳回 */
    private Integer auditStatus;

    private String auditRemark;

    private String updateBy;

    private LocalDateTime updateTime;
}
