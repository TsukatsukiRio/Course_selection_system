package com.smartcampus.selection.vo;

import com.smartcampus.common.util.ClassTimeParser.Segment;
import lombok.Data;

import java.util.List;

/**
 * 课表项视图：课程安排 + 解析后的时间段（供前端周视图渲染）。
 */
@Data
public class ScheduleItemVO {

    private Long recordId;
    private String courseNo;
    private String name;
    private String teacherName;
    private String location;
    private String classTime;
    private Integer credits;
    private List<Segment> segments;
}
