package com.smartcampus.selection.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.selection.service.SelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 教师接口：查看选课学生名单。
 */
@RestController
@RequestMapping("/api/selection/teacher")
@RequiredArgsConstructor
public class TeacherSelectionController {

    private final SelectionService selectionService;

    /** 选课学生名单（学号 + 姓名 + 选课时间） */
    @GetMapping("/course/{courseNo}/students")
    public R<List<Map<String, Object>>> students(@PathVariable String courseNo) {
        return R.ok(selectionService.courseStudents(courseNo));
    }
}
