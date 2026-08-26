package com.smartcampus.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 教学计划审核请求。
 */
@Data
public class AuditDTO {

    /** 1通过 2驳回 */
    @NotNull(message = "审核结果不能为空")
    private Integer auditStatus;

    private String auditRemark;
}
