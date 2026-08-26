package com.smartcampus.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图对象（不含密码）。
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String name;
    private String role;
    private String college;
    private String major;
    private String email;
    private String phone;
    private Integer gender;
    private String interestTags;
    private Integer status;
    private LocalDateTime createTime;
}
