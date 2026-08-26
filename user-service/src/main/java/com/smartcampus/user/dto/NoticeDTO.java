package com.smartcampus.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 公告新增/编辑请求。
 */
@Data
public class NoticeDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private Integer status;
}
