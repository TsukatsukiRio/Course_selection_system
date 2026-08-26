package com.smartcampus.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 sys_user。
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学号/工号 */
    private String username;

    /** BCrypt 加密密码 */
    private String password;

    private String name;

    /** STUDENT / TEACHER / ADMIN */
    private String role;

    private String college;

    private String major;

    private String email;

    private String phone;

    /** 0未知 1男 2女 */
    private Integer gender;

    /** 兴趣标签，逗号分隔 */
    private String interestTags;

    /** 1正常 0禁用 */
    private Integer status;

    /** 连续登录失败次数 */
    private Integer failCount;

    /** 账号锁定截止时间 */
    private LocalDateTime lockedUntil;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
