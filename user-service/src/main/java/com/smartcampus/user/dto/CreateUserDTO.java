package com.smartcampus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员创建用户（教师/管理员账号）请求。
 */
@Data
public class CreateUserDTO {

    @NotBlank(message = "工号不能为空")
    @Pattern(regexp = "^\\d{6,12}$", message = "工号须为6-12位数字")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z0-9]{8,20}$",
            message = "密码须8-20位，包含大小写字母和数字")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(TEACHER|ADMIN)$", message = "角色只能为 TEACHER 或 ADMIN")
    private String role;

    private String college;

    private String major;

    private String email;

    private String phone;

    private Integer gender;
}
