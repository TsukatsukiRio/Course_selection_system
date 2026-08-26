package com.smartcampus.selection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.result.R;
import com.smartcampus.common.util.ClassTimeParser;
import com.smartcampus.selection.dto.SelectDTO;
import com.smartcampus.selection.entity.SelectionRecord;
import com.smartcampus.selection.feign.CourseClient;
import com.smartcampus.selection.feign.UserClient;
import com.smartcampus.selection.mapper.SelectionRecordMapper;
import com.smartcampus.selection.vo.ConflictCheckVO;
import com.smartcampus.selection.vo.CourseVO;
import com.smartcampus.selection.vo.ScheduleItemVO;
import com.smartcampus.selection.vo.SelectionVO;
import com.smartcampus.selection.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 选退课核心服务（UC-07 / UC-08 / UC-09）：
 * 选课流程：窗口期 -> 名额 -> 数量/学分上限 -> 重复选课 -> 先修课程 -> 时间冲突 -> 原子扣减名额 -> 落库。
 * 退课流程：窗口期 -> 记录归属 -> 必修课保护 -> 状态流转 -> 恢复名额。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SelectionService {

    private static final String DEFAULT_SEMESTER = "2026-2027-1";

    private final SelectionRecordMapper recordMapper;
    private final ConfigService configService;
    private final CourseClient courseClient;
    private final UserClient userClient;

    /** 选课：全部校验通过后原子扣减名额并落库 */
    public SelectionVO select(String studentNo, SelectDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime selectStart = configService.getDateTime("select_start");
        LocalDateTime selectEnd = configService.getDateTime("select_end");
        if (selectStart != null && selectEnd != null
                && (now.isBefore(selectStart) || now.isAfter(selectEnd))) {
            throw new BizException("当前不在选课开放时间段");
        }

        CourseVO course = getCourse(dto.getCourseNo());
        if (course.getStatus() == null || course.getStatus() != 1) {
            throw new BizException("该课程已停开");
        }

        // 1. 重复选课检测
        if (countActiveRecord(studentNo, dto.getCourseNo()) > 0) {
            throw new BizException("您已选择该课程，请勿重复选课");
        }

        List<SelectionRecord> myRecords = activeRecords(studentNo);
        Map<String, CourseVO> courseMap = courseMapByNos(myRecords.stream()
                .map(SelectionRecord::getCourseNo).collect(Collectors.toList()));

        // 2. 选课数量上限检测（默认 5 门）
        int maxCount = configService.getInt("max_course_count", 5);
        if (myRecords.size() >= maxCount) {
            throw new BizException("选课数量已达上限（" + maxCount + "门）");
        }

        // 3. 学分上限检测
        int maxCredits = configService.getInt("max_credits", 30);
        int currentCredits = myRecords.stream()
                .map(SelectionRecord::getCourseNo)
                .map(courseMap::get)
                .filter(Objects::nonNull)
                .mapToInt(CourseVO::getCredits)
                .sum();
        if (currentCredits + course.getCredits() > maxCredits) {
            throw new BizException("学分超限：当前已选 " + currentCredits + " 学分，上限 "
                    + maxCredits + " 学分");
        }

        // 4. 先修课程检测
        checkPrerequisite(studentNo, course);

        // 5. 时间冲突检测
        for (SelectionRecord record : myRecords) {
            CourseVO selected = courseMap.get(record.getCourseNo());
            if (selected != null && ClassTimeParser.overlaps(course.getClassTime(), selected.getClassTime())) {
                throw new BizException("时间冲突：与已选课程《" + selected.getName()
                        + "》（" + selected.getClassTime() + "）上课时间重叠");
            }
        }

        // 6. 名额原子扣减（防并发超选）
        boolean decremented;
        try {
            decremented = Boolean.TRUE.equals(
                    courseClient.decrement(dto.getCourseNo(), "true").getData());
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("课程服务名额扣减调用失败", e);
            throw new BizException(503, "课程服务暂时不可用，请稍后重试");
        }
        if (!decremented) {
            throw new BizException("该课程名额已满");
        }

        // 7. 生成选课记录
        SelectionRecord record = new SelectionRecord();
        record.setStudentNo(studentNo);
        record.setCourseNo(dto.getCourseNo());
        record.setSemester(DEFAULT_SEMESTER);
        record.setStatus("SELECTED");
        record.setSelectTime(now);
        try {
            recordMapper.insert(record);
        } catch (Exception e) {
            log.error("选课记录写入失败，回补名额", e);
            safeIncrement(dto.getCourseNo());
            throw new BizException("选课失败，请稍后重试");
        }
        log.info("选课成功: 学生={}, 课程={}", studentNo, dto.getCourseNo());
        return buildVO(record, course);
    }

    /** 退课：窗口期校验 + 必修课保护 + 名额恢复 */
    public void drop(String studentNo, Long recordId) {
        SelectionRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BizException(404, "选课记录不存在");
        }
        if (!studentNo.equals(record.getStudentNo())) {
            throw new BizException(403, "只能操作本人的选课记录");
        }
        if (!"SELECTED".equals(record.getStatus())) {
            throw new BizException("该记录已退课，请勿重复操作");
        }
        LocalDateTime dropEnd = configService.getDateTime("drop_end");
        if (dropEnd != null && LocalDateTime.now().isAfter(dropEnd)) {
            throw new BizException("已超出退课时间，无法退课");
        }
        CourseVO course = getCourse(record.getCourseNo());
        boolean allowDropRequired = "true".equalsIgnoreCase(
                configService.getOrDefault("allow_drop_required", "false"));
        if ("REQUIRED".equals(course.getCourseType()) && !allowDropRequired) {
            throw new BizException("必修课程不可退选");
        }
        record.setStatus("DROPPED");
        record.setDropTime(LocalDateTime.now());
        recordMapper.updateById(record);
        safeIncrement(record.getCourseNo());
        log.info("退课成功: 学生={}, 课程={}", studentNo, record.getCourseNo());
    }

    /** 我的选课（含课程信息） */
    public List<SelectionVO> mySelections(String studentNo, String semester, boolean onlyActive) {
        List<SelectionRecord> records = recordMapper.selectList(new LambdaQueryWrapper<SelectionRecord>()
                .eq(SelectionRecord::getStudentNo, studentNo)
                .eq(StringUtils.hasText(semester), SelectionRecord::getSemester, semester)
                .eq(onlyActive, SelectionRecord::getStatus, "SELECTED")
                .orderByDesc(SelectionRecord::getSelectTime));
        if (records.isEmpty()) {
            return List.of();
        }
        Map<String, CourseVO> courseMap = courseMapByNos(records.stream()
                .map(SelectionRecord::getCourseNo).distinct().collect(Collectors.toList()));
        return records.stream().map(r -> buildVO(r, courseMap.get(r.getCourseNo())))
                .collect(Collectors.toList());
    }

    /** 我的课表（周视图数据） */
    public List<ScheduleItemVO> schedule(String studentNo, String semester) {
        List<SelectionVO> selections = mySelections(studentNo, semester, true);
        List<ScheduleItemVO> items = new ArrayList<>();
        for (SelectionVO selection : selections) {
            CourseVO course = selection.getCourse();
            if (course == null) {
                continue;
            }
            ScheduleItemVO item = new ScheduleItemVO();
            item.setRecordId(selection.getId());
            item.setCourseNo(course.getCourseNo());
            item.setName(course.getName());
            item.setTeacherName(course.getTeacherName());
            item.setLocation(course.getLocation());
            item.setClassTime(course.getClassTime());
            item.setCredits(course.getCredits());
            item.setSegments(ClassTimeParser.parse(course.getClassTime()));
            items.add(item);
        }
        return items;
    }

    /** 选课预检测：返回冲突详情而非直接抛异常 */
    public ConflictCheckVO conflictCheck(String studentNo, String courseNo) {
        try {
            CourseVO course = getCourse(courseNo);
            if (course.getStatus() == null || course.getStatus() != 1) {
                return new ConflictCheckVO(false, "该课程已停开");
            }
            if (countActiveRecord(studentNo, courseNo) > 0) {
                return new ConflictCheckVO(false, "您已选择该课程");
            }
            if (course.getSelectedCount() != null && course.getCapacity() != null
                    && course.getSelectedCount() >= course.getCapacity()) {
                return new ConflictCheckVO(false, "该课程名额已满");
            }
            checkPrerequisite(studentNo, course);
            List<SelectionRecord> myRecords = activeRecords(studentNo);
            Map<String, CourseVO> courseMap = courseMapByNos(myRecords.stream()
                    .map(SelectionRecord::getCourseNo).collect(Collectors.toList()));
            for (SelectionRecord record : myRecords) {
                CourseVO selected = courseMap.get(record.getCourseNo());
                if (selected != null
                        && ClassTimeParser.overlaps(course.getClassTime(), selected.getClassTime())) {
                    return new ConflictCheckVO(false, "时间冲突：与已选课程《" + selected.getName()
                            + "》（" + selected.getClassTime() + "）上课时间重叠");
                }
            }
            return new ConflictCheckVO(true, "可以选课");
        } catch (BizException e) {
            return new ConflictCheckVO(false, e.getMessage());
        }
    }

    /** 管理员：分页查询全部选课记录 */
    public Page<SelectionVO> pageRecords(String studentNo, String courseNo, String status,
                                         long pageNum, long pageSize) {
        Page<SelectionRecord> page = recordMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SelectionRecord>()
                        .eq(StringUtils.hasText(studentNo), SelectionRecord::getStudentNo, studentNo)
                        .eq(StringUtils.hasText(courseNo), SelectionRecord::getCourseNo, courseNo)
                        .eq(StringUtils.hasText(status), SelectionRecord::getStatus, status)
                        .orderByDesc(SelectionRecord::getSelectTime));
        List<SelectionRecord> records = page.getRecords();
        Map<String, CourseVO> courseMap = records.isEmpty() ? Map.of()
                : courseMapByNos(records.stream().map(SelectionRecord::getCourseNo).distinct()
                .collect(Collectors.toList()));
        Page<SelectionVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records.stream().map(r -> buildVO(r, courseMap.get(r.getCourseNo())))
                .collect(Collectors.toList()));
        return result;
    }

    /** 教师：查看选课学生名单（含姓名） */
    public List<Map<String, Object>> courseStudents(String courseNo) {
        List<SelectionRecord> records = recordMapper.selectList(new LambdaQueryWrapper<SelectionRecord>()
                .eq(SelectionRecord::getCourseNo, courseNo)
                .eq(SelectionRecord::getStatus, "SELECTED")
                .orderByAsc(SelectionRecord::getSelectTime));
        if (records.isEmpty()) {
            return List.of();
        }
        List<String> usernames = records.stream().map(SelectionRecord::getStudentNo)
                .distinct().collect(Collectors.toList());
        Map<String, String> nameMap = new HashMap<>();
        try {
            R<List<UserVO>> r = userClient.batch(String.join(",", usernames));
            if (r != null && r.getData() != null) {
                for (UserVO user : r.getData()) {
                    nameMap.put(user.getUsername(), user.getName());
                }
            }
        } catch (Exception e) {
            log.warn("用户服务查询失败，学生名单仅返回学号", e);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (SelectionRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("studentNo", record.getStudentNo());
            item.put("name", nameMap.getOrDefault(record.getStudentNo(), ""));
            item.put("selectTime", record.getSelectTime());
            result.add(item);
        }
        return result;
    }

    /** 内部：学生已选课程编号 */
    public List<String> studentCourseNos(String studentNo) {
        return activeRecords(studentNo).stream()
                .map(SelectionRecord::getCourseNo).collect(Collectors.toList());
    }

    /** 内部：统计聚合 */
    public List<Map<String, Object>> trend(String start, String end) {
        return recordMapper.trend(start, end);
    }

    public List<Map<String, Object>> top(int limit) {
        return recordMapper.top(limit);
    }

    public List<Map<String, Object>> hours() {
        return recordMapper.hours();
    }

    public List<String> distinctStudents() {
        return recordMapper.distinctStudents();
    }

    // ---------------- 私有方法 ----------------

    private void checkPrerequisite(String studentNo, CourseVO course) {
        if (!StringUtils.hasText(course.getPrerequisite())) {
            return;
        }
        List<String> passed;
        try {
            passed = courseClient.passedCourseNos(studentNo).getData();
        } catch (Exception e) {
            log.warn("先修课程成绩查询失败", e);
            passed = Collections.emptyList();
        }
        if (passed == null) {
            passed = Collections.emptyList();
        }
        for (String required : course.getPrerequisite().split(",")) {
            String requiredNo = required.trim();
            if (!requiredNo.isEmpty() && !passed.contains(requiredNo)) {
                throw new BizException("先修课程未满足：需先修 " + requiredNo);
            }
        }
    }

    private CourseVO getCourse(String courseNo) {
        try {
            R<CourseVO> r = courseClient.getByCourseNo(courseNo);
            if (r == null || r.getData() == null) {
                throw new BizException(404, "课程不存在");
            }
            return r.getData();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("课程服务调用失败", e);
            throw new BizException(503, "课程服务暂时不可用，请稍后重试");
        }
    }

    private Map<String, CourseVO> courseMapByNos(List<String> courseNos) {
        if (courseNos == null || courseNos.isEmpty()) {
            return Map.of();
        }
        try {
            R<List<CourseVO>> r = courseClient.batch(String.join(",", courseNos));
            if (r == null || r.getData() == null) {
                return Map.of();
            }
            return r.getData().stream()
                    .collect(Collectors.toMap(CourseVO::getCourseNo, c -> c, (a, b) -> a));
        } catch (Exception e) {
            log.warn("课程批量查询失败", e);
            return Map.of();
        }
    }

    private List<SelectionRecord> activeRecords(String studentNo) {
        return recordMapper.selectList(new LambdaQueryWrapper<SelectionRecord>()
                .eq(SelectionRecord::getStudentNo, studentNo)
                .eq(SelectionRecord::getStatus, "SELECTED"));
    }

    private long countActiveRecord(String studentNo, String courseNo) {
        return recordMapper.selectCount(new LambdaQueryWrapper<SelectionRecord>()
                .eq(SelectionRecord::getStudentNo, studentNo)
                .eq(SelectionRecord::getCourseNo, courseNo)
                .eq(SelectionRecord::getStatus, "SELECTED"));
    }

    private void safeIncrement(String courseNo) {
        try {
            courseClient.increment(courseNo, "true");
        } catch (Exception e) {
            log.error("课程名额恢复失败: {}", courseNo, e);
        }
    }

    private SelectionVO buildVO(SelectionRecord record, CourseVO course) {
        SelectionVO vo = new SelectionVO();
        vo.setId(record.getId());
        vo.setStudentNo(record.getStudentNo());
        vo.setCourseNo(record.getCourseNo());
        vo.setSemester(record.getSemester());
        vo.setStatus(record.getStatus());
        vo.setSelectTime(record.getSelectTime());
        vo.setDropTime(record.getDropTime());
        vo.setCourse(course);
        return vo;
    }
}
