package com.smartcampus.selection.feign;

import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.result.R;
import com.smartcampus.selection.feign.fallback.CourseClientFallback;
import com.smartcampus.selection.vo.CourseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 课程服务 Feign 客户端（服务间声明式调用）。
 */
@FeignClient(name = "course-service", fallbackFactory = CourseClientFallback.class)
public interface CourseClient {

    @GetMapping("/api/course/internal/by-no/{courseNo}")
    R<CourseVO> getByCourseNo(@PathVariable("courseNo") String courseNo);

    @GetMapping("/api/course/internal/batch")
    R<List<CourseVO>> batch(@RequestParam("courseNos") String courseNos);

    @PostMapping("/api/course/internal/{courseNo}/decrement")
    R<Boolean> decrement(@PathVariable("courseNo") String courseNo,
                         @RequestHeader(HeaderConstants.X_INTERNAL_CALL) String internal);

    @PostMapping("/api/course/internal/{courseNo}/increment")
    R<Boolean> increment(@PathVariable("courseNo") String courseNo,
                         @RequestHeader(HeaderConstants.X_INTERNAL_CALL) String internal);

    @GetMapping("/api/course/internal/passed/{studentNo}")
    R<List<String>> passedCourseNos(@PathVariable("studentNo") String studentNo);

    @GetMapping("/api/course/internal/hot")
    R<List<CourseVO>> hot(@RequestParam(value = "limit", defaultValue = "10") int limit);
}
