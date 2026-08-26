package com.smartcampus.course.vo;

import com.smartcampus.course.entity.TeachingPlan;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程详情视图对象：课程信息 + 教学计划。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseDetailVO extends CourseVO {

    /** 教学计划（可能为空） */
    private TeachingPlan teachingPlan;
}
