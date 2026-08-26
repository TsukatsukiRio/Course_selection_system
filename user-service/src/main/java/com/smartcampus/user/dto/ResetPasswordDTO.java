package com.smartcampus.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员重置密码请求。
 */
@Data
public class ResetPasswordDTO {

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z0-9]{8,20}$",
            message = "密码须8-20位，包含大小写字母和数字")
    private String newPassword;
}
