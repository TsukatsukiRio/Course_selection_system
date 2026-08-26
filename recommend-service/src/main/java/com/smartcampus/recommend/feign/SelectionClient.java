package com.smartcampus.recommend.feign;

import com.smartcampus.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 选课服务 Feign 客户端。
 */
@FeignClient(name = "selection-service")
public interface SelectionClient {

    /** 学生当前已选课程编号 */
    @GetMapping("/api/selection/internal/student/{studentNo}")
    R<List<String>> studentCourseNos(@PathVariable("studentNo") String studentNo);

    /** 某课程的全部选课学生学号（用于协同过滤） */
    @GetMapping("/api/selection/internal/course/{courseNo}/students")
    R<List<String>> courseStudentNos(@PathVariable("courseNo") String courseNo);
}
