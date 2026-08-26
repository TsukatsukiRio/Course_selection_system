package com.smartcampus.selection.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.selection.service.SelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 内部接口：供 recommend / statistics 服务通过 Feign 调用。
 * 网关已拦截外部访问（路径含 /internal/ 且无内部标记）。
 */
@RestController
@RequestMapping("/api/selection/internal")
@RequiredArgsConstructor
public class InternalSelectionController {

    private final SelectionService selectionService;

    /** 学生当前已选课程编号 */
    @GetMapping("/student/{studentNo}")
    public R<List<String>> studentCourseNos(@PathVariable String studentNo) {
        return R.ok(selectionService.studentCourseNos(studentNo));
    }

    /** 课程选课学生学号列表 */
    @GetMapping("/course/{courseNo}/students")
    public R<List<String>> courseStudentNos(@PathVariable String courseNo) {
        return R.ok(selectionService.courseStudents(courseNo).stream()
                .map(item -> String.valueOf(item.get("studentNo"))).toList());
    }

    /** 选课人数趋势（按天） */
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend(@RequestParam String start, @RequestParam String end) {
        return R.ok(selectionService.trend(start, end));
    }

    /** 课程热度排行 */
    @GetMapping("/top")
    public R<List<Map<String, Object>>> top(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(selectionService.top(limit));
    }

    /** 选课时段分布（按小时） */
    @GetMapping("/hours")
    public R<List<Map<String, Object>>> hours() {
        return R.ok(selectionService.hours());
    }

    /** 已选课学生学号去重列表 */
    @GetMapping("/distinct-students")
    public R<List<String>> distinctStudents() {
        return R.ok(selectionService.distinctStudents());
    }
}
