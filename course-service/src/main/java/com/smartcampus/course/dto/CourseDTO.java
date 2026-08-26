package com.smartcampus.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 课程新增/编辑请求（UC-04）。
 */
@Data
public class CourseDTO {

    @NotBlank(message = "课程编号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9-]{3,16}$", message = "课程编号须为3-16位字母数字，如 CS101")
    private String courseNo;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称最长100字符")
    private String name;

    @NotBlank(message = "授课教师工号不能为空")
    private String teacherNo;

    private String teacherName;

    @NotNull(message = "学分不能为空")
    @Min(value = 1, message = "学分最小为1")
    @Max(value = 6, message = "学分最大为6")
    private Integer credits;

    @NotBlank(message = "上课时间不能为空")
    private String classTime;

    @NotBlank(message = "上课地点不能为空")
    private String location;

    @NotNull(message = "容量上限不能为空")
    @Min(value = 1, message = "容量上限必须大于0")
    private Integer capacity;

    /** 先修课程编号，多个用逗号分隔 */
    private String prerequisite;

    @NotBlank(message = "所属院系不能为空")
    private String college;

    @NotBlank(message = "课程类型不能为空")
    @Pattern(regexp = "^(REQUIRED|ELECTIVE|GENERAL)$", message = "课程类型须为 必修/选修/通识 之一")
    private String courseType;

    @Size(max = 500, message = "课程描述最长500字符")
    private String description;
}
