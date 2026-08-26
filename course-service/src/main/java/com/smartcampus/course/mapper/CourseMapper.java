package com.smartcampus.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程数据访问层：包含名额原子扣减/恢复 SQL。
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /** 原子扣减名额：仅当仍有剩余名额时成功，防止并发超选 */
    @Update("UPDATE course SET selected_count = selected_count + 1 "
            + "WHERE course_no = #{courseNo} AND status = 1 AND selected_count < capacity")
    int decrement(@Param("courseNo") String courseNo);

    /** 原子恢复名额 */
    @Update("UPDATE course SET selected_count = selected_count - 1 "
            + "WHERE course_no = #{courseNo} AND selected_count > 0")
    int increment(@Param("courseNo") String courseNo);
}
