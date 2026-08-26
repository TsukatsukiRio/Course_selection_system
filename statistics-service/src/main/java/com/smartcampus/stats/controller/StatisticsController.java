package com.smartcampus.stats.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.stats.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 选课数据统计与可视化接口（UC-11），仅管理员可访问（网关鉴权）。
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    public R<Map<String, Object>> overview(@RequestParam(required = false) String start,
                                           @RequestParam(required = false) String end) {
        String[] range = resolveRange(start, end);
        return R.ok(statisticsService.overview(range[0], range[1]));
    }

    /** 选课人数趋势（折线图） */
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend(@RequestParam(required = false) String start,
                                              @RequestParam(required = false) String end) {
        String[] range = resolveRange(start, end);
        return R.ok(statisticsService.trend(range[0], range[1]));
    }

    /** 课程热度排行榜 Top10（柱状图） */
    @GetMapping("/top")
    public R<List<Map<String, Object>>> top(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(statisticsService.top(limit));
    }

    /** 各院系选课率对比（柱状图） */
    @GetMapping("/college")
    public R<List<Map<String, Object>>> college() {
        return R.ok(statisticsService.college());
    }

    /** 课程类型分布（饼图） */
    @GetMapping("/type")
    public R<List<Map<String, Object>>> type() {
        return R.ok(statisticsService.type());
    }

    /** 选课时段分析（热力图，按小时） */
    @GetMapping("/hour")
    public R<List<Map<String, Object>>> hour() {
        return R.ok(statisticsService.hour());
    }

    /** 导出课程热度 CSV */
    @GetMapping("/export/top")
    public ResponseEntity<byte[]> exportTop(@RequestParam(defaultValue = "10") int limit) {
        String csv = statisticsService.exportTopCsv(limit);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "course-top.csv");
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''course-top.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String[] resolveRange(String start, String end) {
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            return statisticsService.defaultRange();
        }
        return new String[]{start, end + " 23:59:59"};
    }
}
