package com.smartcampus.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.selection.entity.SelectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 选课记录数据访问层：包含统计聚合查询。
 */
@Mapper
public interface SelectionRecordMapper extends BaseMapper<SelectionRecord> {

    /** 选课人数趋势：按天统计 */
    @Select("SELECT DATE_FORMAT(select_time, '%Y-%m-%d') AS date, COUNT(*) AS count "
            + "FROM selection_record WHERE status = 'SELECTED' "
            + "AND select_time >= #{start} AND select_time <= #{end} "
            + "GROUP BY DATE_FORMAT(select_time, '%Y-%m-%d') ORDER BY date")
    List<Map<String, Object>> trend(@Param("start") String start, @Param("end") String end);

    /** 课程热度排行：按选课人数 */
    @Select("SELECT course_no AS courseNo, COUNT(*) AS count FROM selection_record "
            + "WHERE status = 'SELECTED' GROUP BY course_no ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> top(@Param("limit") int limit);

    /** 选课时段分析：按小时统计 */
    @Select("SELECT HOUR(select_time) AS hour, COUNT(*) AS count FROM selection_record "
            + "WHERE status = 'SELECTED' GROUP BY HOUR(select_time) ORDER BY hour")
    List<Map<String, Object>> hours();

    /** 已选课的学生学号去重列表 */
    @Select("SELECT DISTINCT student_no AS studentNo FROM selection_record WHERE status = 'SELECTED'")
    List<String> distinctStudents();
}
