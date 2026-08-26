package com.smartcampus.stats.feign;

import com.smartcampus.common.result.R;
import com.smartcampus.stats.vo.CourseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 课程服务 Feign 客户端。
 */
@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/course/internal/type-counts")
    R<List<Map<String, Object>>> typeCounts();

    @GetMapping("/api/course/internal/batch")
    R<List<CourseVO>> batch(@RequestParam("courseNos") String courseNos);
}
