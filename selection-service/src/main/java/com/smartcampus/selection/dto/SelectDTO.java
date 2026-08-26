package com.smartcampus.selection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 选课请求。
 */
@Data
public class SelectDTO {

    @NotBlank(message = "课程编号不能为空")
    private String courseNo;
}
