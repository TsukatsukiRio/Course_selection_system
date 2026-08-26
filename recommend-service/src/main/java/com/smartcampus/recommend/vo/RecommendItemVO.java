package com.smartcampus.recommend.vo;

import lombok.Data;

/**
 * 推荐结果项：课程 + 推荐得分 + 推荐理由。
 */
@Data
public class RecommendItemVO {

    private Long id;
    private String courseNo;
    private String name;
    private String teacherName;
    private Integer credits;
    private String classTime;
    private String location;
    private Integer capacity;
    private Integer selectedCount;
    private String prerequisite;
    private String college;
    private String courseType;
    private String description;

    /** 推荐综合得分（用于排序，前端可不展示） */
    private double score;

    /** 推荐理由 */
    private String reason;
}
