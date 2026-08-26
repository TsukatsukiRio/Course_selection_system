package com.smartcampus.course.controller;

import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.result.R;
import com.smartcampus.course.dto.CourseDTO;
import com.smartcampus.course.dto.GradeDTO;
import com.smartcampus.course.service.CourseService;
import com.smartcampus.course.service.GradeService;
import com.smartcampus.course.vo.CourseVO;
import com.smartcampus.course.vo.GradeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教师课程管理接口：我的课程、编辑课程、成绩录入与查询。
 */
@RestController
@RequestMapping("/api/course/teacher")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseService courseService;
    private final GradeService gradeService;

    @GetMapping("/mine")
    public R<List<CourseVO>> mine(@RequestHeader(HeaderConstants.X_USERNAME) String teacherNo) {
        return R.ok(courseService.teacherMine(teacherNo));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CourseDTO dto,
                          @RequestHeader(HeaderConstants.X_USERNAME) String teacherNo) {
        courseService.teacherUpdate(id, dto, teacherNo);
        return R.ok();
    }

    /** 录入学生成绩 */
    @PostMapping("/grade")
    public R<Void> saveGrade(@Valid @RequestBody GradeDTO dto,
                             @RequestHeader(HeaderConstants.X_USERNAME) String teacherNo) {
        gradeService.save(dto, teacherNo);
        return R.ok();
    }

    /** 查询课程成绩 */
    @GetMapping("/grades")
    public R<List<GradeVO>> grades(@RequestParam String courseNo,
                                   @RequestParam(required = false) String semester) {
        return R.ok(gradeService.listByCourse(courseNo, semester));
    }
}
