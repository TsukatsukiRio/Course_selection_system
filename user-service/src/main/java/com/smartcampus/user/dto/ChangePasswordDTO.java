package com.smartcampus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码请求。
 */
@Data
public class ChangePasswordDTO {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z0-9]{8,20}$",
            message = "密码须8-20位，包含大小写字母和数字")
    private String newPassword;
}
