package com.smartcampus.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.course.dto.GradeDTO;
import com.smartcampus.course.entity.Course;
import com.smartcampus.course.entity.StudentGrade;
import com.smartcampus.course.mapper.CourseMapper;
import com.smartcampus.course.mapper.StudentGradeMapper;
import com.smartcampus.course.vo.GradeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 成绩服务：教师为选课学生录入成绩；推荐服务读取成绩用于偏好分析。
 */
@Service
@RequiredArgsConstructor
public class GradeService {

    private final StudentGradeMapper gradeMapper;
    private final CourseMapper courseMapper;

    /** 教师录入成绩：校验课程归属与分数范围 */
    public void save(GradeDTO dto, String teacherNo) {
        Course course = courseMapper.selectOne(new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseNo, dto.getCourseNo()));
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (!teacherNo.equals(course.getTeacherNo())) {
            throw new BizException(403, "只能为本人所授课程录入成绩");
        }
        StudentGrade grade = gradeMapper.selectOne(new LambdaQueryWrapper<StudentGrade>()
                .eq(StudentGrade::getStudentNo, dto.getStudentNo())
                .eq(StudentGrade::getCourseNo, dto.getCourseNo()));
        if (grade == null) {
            grade = new StudentGrade();
            grade.setStudentNo(dto.getStudentNo());
            grade.setCourseNo(dto.getCourseNo());
            grade.setCourseType(course.getCourseType());
        }
        grade.setScore(dto.getScore());
        grade.setSemester(StringUtils.hasText(dto.getSemester()) ? dto.getSemester() : "2026-2027-1");
        if (grade.getId() == null) {
            gradeMapper.insert(grade);
        } else {
            gradeMapper.updateById(grade);
        }
    }

    /** 教师：查询课程成绩列表 */
    public List<GradeVO> listByCourse(String courseNo, String semester) {
        return gradeMapper.selectList(new LambdaQueryWrapper<StudentGrade>()
                        .eq(StudentGrade::getCourseNo, courseNo)
                        .eq(StringUtils.hasText(semester), StudentGrade::getSemester, semester))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 内部：学生全部成绩 */
    public List<GradeVO> listByStudent(String studentNo) {
        return gradeMapper.selectList(new LambdaQueryWrapper<StudentGrade>()
                        .eq(StudentGrade::getStudentNo, studentNo))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 内部：学生已通过（成绩>=60）的课程编号列表，用于先修课程检测 */
    public List<String> passedCourseNos(String studentNo) {
        return gradeMapper.selectList(new LambdaQueryWrapper<StudentGrade>()
                        .eq(StudentGrade::getStudentNo, studentNo)
                        .ge(StudentGrade::getScore, 60))
                .stream().map(StudentGrade::getCourseNo).collect(Collectors.toList());
    }

    private GradeVO toVO(StudentGrade grade) {
        GradeVO vo = new GradeVO();
        BeanUtils.copyProperties(grade, vo);
        return vo;
    }
}
