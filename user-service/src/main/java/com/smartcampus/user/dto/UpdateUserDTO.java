package com.smartcampus.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员编辑用户请求。
 */
@Data
public class UpdateUserDTO {

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String college;

    private String major;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private Integer gender;

    private String interestTags;

    /** 1正常 0禁用 */
    private Integer status;
}
