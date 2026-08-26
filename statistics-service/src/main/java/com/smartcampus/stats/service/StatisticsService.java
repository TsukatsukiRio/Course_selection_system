package com.smartcampus.stats.service;

import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.result.R;
import com.smartcampus.stats.feign.CourseClient;
import com.smartcampus.stats.feign.SelectionClient;
import com.smartcampus.stats.feign.UserClient;
import com.smartcampus.stats.vo.CourseVO;
import com.smartcampus.stats.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 选课数据统计服务（UC-11）：聚合各微服务数据，产出统计图表所需结构。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final SelectionClient selectionClient;
    private final CourseClient courseClient;
    private final UserClient userClient;

    /** 统计总览卡片 */
    public Map<String, Object> overview(String start, String end) {
        long studentCount = safeStudents().size();
        long courseCount = safeTypeCounts().stream()
                .mapToLong(item -> toLong(item.get("count"))).sum();
        long selectionCount = safeTrend(start, end).stream()
                .mapToLong(item -> toLong(item.get("count"))).sum();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("studentCount", studentCount);
        result.put("courseCount", courseCount);
        result.put("selectionCount", selectionCount);
        result.put("avgSelectionPerStudent",
                studentCount == 0 ? 0 : Math.round(selectionCount * 100.0 / studentCount) / 100.0);
        return result;
    }

    /** 选课人数趋势（折线图） */
    public List<Map<String, Object>> trend(String start, String end) {
        return safeTrend(start, end);
    }

    /** 课程热度排行榜（柱状图，含课程名称） */
    public List<Map<String, Object>> top(int limit) {
        List<Map<String, Object>> top = safeTop(limit);
        Set<String> courseNos = new HashSet<>();
        for (Map<String, Object> item : top) {
            String courseNo = String.valueOf(item.get("courseNo"));
            if (!"null".equals(courseNo)) {
                courseNos.add(courseNo);
            }
        }
        Map<String, String> nameMap = new HashMap<>();
        if (!courseNos.isEmpty()) {
            for (CourseVO course : safeBatch(courseNos)) {
                nameMap.put(course.getCourseNo(), course.getName());
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : top) {
            String courseNo = String.valueOf(item.get("courseNo"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseNo", courseNo);
            row.put("name", nameMap.getOrDefault(courseNo, courseNo));
            row.put("count", item.get("count"));
            result.add(row);
        }
        return result;
    }

    /** 各院系选课率对比 */
    public List<Map<String, Object>> college() {
        List<UserVO> students = safeStudents();
        Set<String> selectedStudents = new HashSet<>(safeDistinctStudents());
        Map<String, long[]> stat = new LinkedHashMap<>();
        for (UserVO student : students) {
            String college = StringUtils.hasText(student.getCollege())
                    ? student.getCollege() : "未分配院系";
            long[] value = stat.computeIfAbsent(college, k -> new long[2]);
            value[0]++;
            if (selectedStudents.contains(student.getUsername())) {
                value[1]++;
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        stat.forEach((college, value) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("college", college);
            row.put("studentCount", value[0]);
            row.put("selectedCount", value[1]);
            row.put("rate", value[0] == 0 ? 0 : Math.round(value[1] * 1000.0 / value[0]) / 10.0);
            result.add(row);
        });
        return result;
    }

    /** 课程类型分布（饼图） */
    public List<Map<String, Object>> type() {
        return safeTypeCounts();
    }

    /** 选课时段分析（热力图数据，按小时） */
    public List<Map<String, Object>> hour() {
        try {
            R<List<Map<String, Object>>> r = selectionClient.hours();
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.error("选课时段统计失败", e);
            throw new BizException(503, "选课服务暂时不可用");
        }
    }

    /** 导出课程热度 CSV */
    public String exportTopCsv(int limit) {
        List<Map<String, Object>> top = top(limit);
        StringBuilder csv = new StringBuilder();
        csv.append("排名,课程编号,课程名称,选课人数\n");
        int rank = 1;
        for (Map<String, Object> row : top) {
            csv.append(rank++).append(',')
                    .append(row.get("courseNo")).append(',')
                    .append(row.get("name")).append(',')
                    .append(row.get("count")).append("\n");
        }
        return csv.toString();
    }

    /** 默认统计时间范围：本学期 */
    public String[] defaultRange() {
        LocalDate now = LocalDate.now();
        return new String[]{
                now.withMonth(8).withDayOfMonth(1).toString(),
                now.withMonth(12).withDayOfMonth(31).toString()
        };
    }

    // ---------------- 安全的 Feign 调用 ----------------

    private List<Map<String, Object>> safeTrend(String start, String end) {
        try {
            R<List<Map<String, Object>>> r = selectionClient.trend(start, end);
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.error("选课趋势统计失败", e);
            throw new BizException(503, "选课服务暂时不可用");
        }
    }

    private List<Map<String, Object>> safeTop(int limit) {
        try {
            R<List<Map<String, Object>>> r = selectionClient.top(limit);
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.error("课程热度统计失败", e);
            throw new BizException(503, "选课服务暂时不可用");
        }
    }

    private List<Map<String, Object>> safeTypeCounts() {
        try {
            R<List<Map<String, Object>>> r = courseClient.typeCounts();
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.error("课程类型统计失败", e);
            throw new BizException(503, "课程服务暂时不可用");
        }
    }

    private List<CourseVO> safeBatch(Set<String> courseNos) {
        try {
            R<List<CourseVO>> r = courseClient.batch(String.join(",", courseNos));
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.warn("课程批量查询失败", e);
            return List.of();
        }
    }

    private List<UserVO> safeStudents() {
        try {
            R<List<UserVO>> r = userClient.students();
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.error("学生列表查询失败", e);
            throw new BizException(503, "用户服务暂时不可用");
        }
    }

    private List<String> safeDistinctStudents() {
        try {
            R<List<String>> r = selectionClient.distinctStudents();
            return r != null && r.getData() != null ? r.getData() : List.of();
        } catch (Exception e) {
            log.error("已选课学生统计失败", e);
            throw new BizException(503, "选课服务暂时不可用");
        }
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return ((Number) value).longValue();
    }
}
