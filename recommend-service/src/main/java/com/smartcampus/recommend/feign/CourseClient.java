package com.smartcampus.recommend.feign;

import com.smartcampus.common.result.R;
import com.smartcampus.recommend.vo.CourseVO;
import com.smartcampus.recommend.vo.GradeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 课程服务 Feign 客户端。
 */
@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/course/internal/all")
    R<List<CourseVO>> allOpen();

    @GetMapping("/api/course/internal/grades/{studentNo}")
    R<List<GradeVO>> grades(@PathVariable("studentNo") String studentNo);

    @GetMapping("/api/course/internal/hot")
    R<List<CourseVO>> hot(@RequestParam(value = "limit", defaultValue = "10") int limit);
}
