package com.smartcampus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改个人资料请求。
 */
@Data
public class UpdateProfileDTO {

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String college;

    private String major;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private Integer gender;

    private String interestTags;
}
