package com.smartcampus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 学生注册请求。
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{6,12}$", message = "学号须为6-12位数字")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z0-9]{8,20}$",
            message = "密码须8-20位，包含大小写字母和数字")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,20}$", message = "姓名须为2-20个中文字符")
    private String name;

    @NotBlank(message = "院系不能为空")
    private String college;

    private String major;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    /** 0未知 1男 2女 */
    private Integer gender;

    /** 兴趣标签，逗号分隔 */
    private String interestTags;
}
