package com.smartcampus.course.controller;

import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.result.R;
import com.smartcampus.course.dto.AuditDTO;
import com.smartcampus.course.dto.TeachingPlanDTO;
import com.smartcampus.course.service.TeachingPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教学计划接口（UC-06）：教师维护（/teacher），管理员审核（/admin）。
 */
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class TeachingPlanController {

    private final TeachingPlanService teachingPlanService;

    @PostMapping("/teacher/teaching-plan")
    public R<Void> saveByTeacher(@Valid @RequestBody TeachingPlanDTO dto,
                                 @RequestHeader(HeaderConstants.X_USERNAME) String teacherNo) {
        teachingPlanService.save(dto, teacherNo, teacherNo);
        return R.ok();
    }

    @PostMapping("/admin/teaching-plan")
    public R<Void> saveByAdmin(@Valid @RequestBody TeachingPlanDTO dto,
                               @RequestHeader(HeaderConstants.X_USERNAME) String admin) {
        teachingPlanService.save(dto, admin, null);
        return R.ok();
    }

    @PutMapping("/admin/teaching-plan/{planId}/audit")
    public R<Void> audit(@PathVariable Long planId, @Valid @RequestBody AuditDTO dto) {
        teachingPlanService.audit(planId, dto);
        return R.ok();
    }
}
