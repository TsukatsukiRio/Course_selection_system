package com.smartcampus.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.course.entity.StudentGrade;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生成绩数据访问层。
 */
@Mapper
public interface StudentGradeMapper extends BaseMapper<StudentGrade> {
}
