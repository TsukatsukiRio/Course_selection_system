package com.smartcampus.stats.feign;

import com.smartcampus.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 选课服务 Feign 客户端（统计聚合数据）。
 */
@FeignClient(name = "selection-service")
public interface SelectionClient {

    @GetMapping("/api/selection/internal/trend")
    R<List<Map<String, Object>>> trend(@RequestParam("start") String start,
                                       @RequestParam("end") String end);

    @GetMapping("/api/selection/internal/top")
    R<List<Map<String, Object>>> top(@RequestParam(value = "limit", defaultValue = "10") int limit);

    @GetMapping("/api/selection/internal/hours")
    R<List<Map<String, Object>>> hours();

    @GetMapping("/api/selection/internal/distinct-students")
    R<List<String>> distinctStudents();
}
