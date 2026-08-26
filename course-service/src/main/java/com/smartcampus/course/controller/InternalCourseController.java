package com.smartcampus.course.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.course.service.CourseService;
import com.smartcampus.course.service.GradeService;
import com.smartcampus.course.vo.CourseVO;
import com.smartcampus.course.vo.GradeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 内部接口：供 selection / recommend / statistics 服务通过 Feign 调用。
 * 网关已拦截外部访问（路径含 /internal/ 且无内部标记）。
 */
@RestController
@RequestMapping("/api/course/internal")
@RequiredArgsConstructor
public class InternalCourseController {

    private final CourseService courseService;
    private final GradeService gradeService;

    @GetMapping("/by-no/{courseNo}")
    public R<CourseVO> byNo(@PathVariable String courseNo) {
        return R.ok(courseService.getByCourseNo(courseNo));
    }

    @GetMapping("/batch")
    public R<List<CourseVO>> batch(@RequestParam String courseNos) {
        return R.ok(courseService.batchByNos(Arrays.asList(courseNos.split(","))));
    }

    @GetMapping("/all")
    public R<List<CourseVO>> allOpen() {
        return R.ok(courseService.listAllOpen());
    }

    @PostMapping("/{courseNo}/decrement")
    public R<Boolean> decrement(@PathVariable String courseNo) {
        return R.ok(courseService.decrement(courseNo));
    }

    @PostMapping("/{courseNo}/increment")
    public R<Boolean> increment(@PathVariable String courseNo) {
        return R.ok(courseService.increment(courseNo));
    }

    @GetMapping("/grades/{studentNo}")
    public R<List<GradeVO>> grades(@PathVariable String studentNo) {
        return R.ok(gradeService.listByStudent(studentNo));
    }

    @GetMapping("/passed/{studentNo}")
    public R<List<String>> passed(@PathVariable String studentNo) {
        return R.ok(gradeService.passedCourseNos(studentNo));
    }

    @GetMapping("/type-counts")
    public R<List<Map<String, Object>>> typeCounts() {
        return R.ok(courseService.typeCounts());
    }

    @GetMapping("/hot")
    public R<List<CourseVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(courseService.hot(limit));
    }
}
