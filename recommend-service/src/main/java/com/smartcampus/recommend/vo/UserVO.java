package com.smartcampus.recommend.vo;

import lombok.Data;

/**
 * 用户信息（Feign 远程传输用）。
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
    private String interestTags;
}
