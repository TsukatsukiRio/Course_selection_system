package com.smartcampus.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.course.dto.AuditDTO;
import com.smartcampus.course.dto.TeachingPlanDTO;
import com.smartcampus.course.entity.Course;
import com.smartcampus.course.entity.TeachingPlan;
import com.smartcampus.course.mapper.CourseMapper;
import com.smartcampus.course.mapper.TeachingPlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 教学计划服务：教师上传/编辑，管理员审核（UC-06）。
 */
@Service
@RequiredArgsConstructor
public class TeachingPlanService {

    private final TeachingPlanMapper teachingPlanMapper;
    private final CourseMapper courseMapper;

    /** 教师/管理员保存教学计划：每门课程一份，存在则更新 */
    public void save(TeachingPlanDTO dto, String operator, String teacherNo) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (teacherNo != null && !teacherNo.equals(course.getTeacherNo())) {
            throw new BizException(403, "只能维护本人所授课程的教学计划");
        }
        TeachingPlan plan = teachingPlanMapper.selectOne(new LambdaQueryWrapper<TeachingPlan>()
                .eq(TeachingPlan::getCourseId, dto.getCourseId()));
        if (plan == null) {
            plan = new TeachingPlan();
            plan.setCourseId(dto.getCourseId());
            plan.setCourseNo(course.getCourseNo());
        }
        plan.setOutline(dto.getOutline());
        plan.setSchedule(dto.getSchedule());
        plan.setTextbook(dto.getTextbook());
        plan.setAuditStatus(0);
        plan.setAuditRemark(null);
        plan.setUpdateBy(operator);
        if (plan.getId() == null) {
            teachingPlanMapper.insert(plan);
        } else {
            teachingPlanMapper.updateById(plan);
        }
    }

    public TeachingPlan getByCourseId(Long courseId) {
        return teachingPlanMapper.selectOne(new LambdaQueryWrapper<TeachingPlan>()
                .eq(TeachingPlan::getCourseId, courseId));
    }

    /** 管理员审核教学计划 */
    public void audit(Long planId, AuditDTO dto) {
        TeachingPlan plan = teachingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(404, "教学计划不存在");
        }
        if (dto.getAuditStatus() != 1 && dto.getAuditStatus() != 2) {
            throw new BizException("审核结果只能为 1(通过) 或 2(驳回)");
        }
        plan.setAuditStatus(dto.getAuditStatus());
        plan.setAuditRemark(dto.getAuditRemark());
        teachingPlanMapper.updateById(plan);
    }
}
