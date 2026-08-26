package com.smartcampus.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体，对应表 sys_notice。
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String publisher;

    /** 1发布 0下架 */
    private Integer status;

    private LocalDateTime publishTime;
}
