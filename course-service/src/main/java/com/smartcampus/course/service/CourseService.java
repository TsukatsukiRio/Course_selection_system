package com.smartcampus.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.util.ClassTimeParser;
import com.smartcampus.course.dto.CourseDTO;
import com.smartcampus.course.entity.Course;
import com.smartcampus.course.mapper.CourseMapper;
import com.smartcampus.course.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务：课程检索、增删改查、教室时间冲突检测与名额原子扣减。
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;

    /** 多条件组合检索（UC-05）：课程名称模糊、编号精确、教师模糊、院系、类型、学分范围 */
    public Page<CourseVO> pageQuery(String name, String courseNo, String teacherName, String college,
                                    String courseType, Integer minCredit, Integer maxCredit,
                                    long pageNum, long pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Course::getName, name)
                .eq(StringUtils.hasText(courseNo), Course::getCourseNo, courseNo)
                .like(StringUtils.hasText(teacherName), Course::getTeacherName, teacherName)
                .eq(StringUtils.hasText(college), Course::getCollege, college)
                .eq(StringUtils.hasText(courseType), Course::getCourseType, courseType)
                .ge(minCredit != null, Course::getCredits, minCredit)
                .le(maxCredit != null, Course::getCredits, maxCredit)
                .orderByAsc(Course::getCourseNo);
        Page<Course> page = courseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<CourseVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    /** 下拉选项：开放课程列表 */
    public List<CourseVO> options(String keyword) {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, 1)
                        .like(StringUtils.hasText(keyword), Course::getName, keyword)
                        .orderByAsc(Course::getCourseNo))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 热门课程：按已选人数倒序 */
    public List<CourseVO> hot(int limit) {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, 1)
                        .orderByDesc(Course::getSelectedCount)
                        .last("LIMIT " + limit))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    public CourseVO getById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        return toVO(course);
    }

    public CourseVO getByCourseNo(String courseNo) {
        Course course = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseNo, courseNo));
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        return toVO(course);
    }

    /** 新增课程：编号唯一校验 + 同教室同时段冲突检测（UC-04） */
    public void create(CourseDTO dto) {
        checkCourseNoUnique(dto.getCourseNo(), null);
        checkLocationConflict(dto.getLocation(), dto.getClassTime(), null);
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        course.setSelectedCount(0);
        course.setStatus(1);
        courseMapper.insert(course);
    }

    /** 编辑课程：唯一性与教室冲突检测排除自身 */
    public void update(Long id, CourseDTO dto) {
        Course exists = requireCourse(id);
        checkCourseNoUnique(dto.getCourseNo(), id);
        checkLocationConflict(dto.getLocation(), dto.getClassTime(), id);
        BeanUtils.copyProperties(dto, exists);
        courseMapper.updateById(exists);
    }

    /** 删除课程：已有学生选课则禁止删除，可停开 */
    public void delete(Long id) {
        Course course = requireCourse(id);
        if (course.getSelectedCount() != null && course.getSelectedCount() > 0) {
            throw new BizException("该课程已有学生选课，不能删除，可将其停开");
        }
        courseMapper.deleteById(id);
    }

    /** 课程状态管理：1开放 0停开 */
    public void updateStatus(Long id, Integer status) {
        Course course = requireCourse(id);
        course.setStatus(status);
        courseMapper.updateById(course);
    }

    /** 教师：查询本人所授课程 */
    public List<CourseVO> teacherMine(String teacherNo) {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherNo, teacherNo)
                        .orderByAsc(Course::getCourseNo))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 教师：编辑本人课程基本信息 */
    public void teacherUpdate(Long id, CourseDTO dto, String teacherNo) {
        Course course = requireCourse(id);
        if (!teacherNo.equals(course.getTeacherNo())) {
            throw new BizException(403, "只能编辑本人所授课程");
        }
        checkLocationConflict(dto.getLocation(), dto.getClassTime(), id);
        BeanUtils.copyProperties(dto, course);
        courseMapper.updateById(course);
    }

    /** 内部：按编号批量查询 */
    public List<CourseVO> batchByNos(List<String> courseNos) {
        if (courseNos == null || courseNos.isEmpty()) {
            return List.of();
        }
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .in(Course::getCourseNo, courseNos))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 内部：全部开放课程（推荐服务使用） */
    public List<CourseVO> listAllOpen() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, 1))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 内部：名额原子扣减，返回是否成功（防止并发超选） */
    public boolean decrement(String courseNo) {
        return courseMapper.decrement(courseNo) > 0;
    }

    /** 内部：名额恢复 */
    public boolean increment(String courseNo) {
        return courseMapper.increment(courseNo) > 0;
    }

    /** 内部：课程类型分布统计 */
    public List<Map<String, Object>> typeCounts() {
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<Course>());
        Map<String, Long> counts = courses.stream().collect(
                Collectors.groupingBy(Course::getCourseType, Collectors.counting()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (String type : new String[]{"REQUIRED", "ELECTIVE", "GENERAL"}) {
            Map<String, Object> item = new HashMap<>();
            item.put("courseType", type);
            item.put("count", counts.getOrDefault(type, 0L));
            result.add(item);
        }
        return result;
    }

    private void checkCourseNoUnique(String courseNo, Long excludeId) {
        Long count = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseNo, courseNo)
                .ne(excludeId != null, Course::getId, excludeId));
        if (count > 0) {
            throw new BizException("该课程编号已存在");
        }
    }

    /** 同教室 + 时间重叠即判定冲突 */
    private void checkLocationConflict(String location, String classTime, Long excludeId) {
        List<Course> sameLocation = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getLocation, location)
                .eq(Course::getStatus, 1)
                .ne(excludeId != null, Course::getId, excludeId));
        for (Course course : sameLocation) {
            if (ClassTimeParser.overlaps(classTime, course.getClassTime())) {
                throw new BizException("上课时间与教室冲突：" + course.getName()
                        + "（" + course.getClassTime() + "）已占用 " + location);
            }
        }
    }

    private Course requireCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        return course;
    }

    public CourseVO toVO(Course course) {
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        return vo;
    }
}
