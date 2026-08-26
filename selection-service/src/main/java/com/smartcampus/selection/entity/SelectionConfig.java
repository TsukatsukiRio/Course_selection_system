package com.smartcampus.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 选课参数配置实体，对应表 selection_config。
 */
@Data
@TableName("selection_config")
public class SelectionConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey;

    private String configValue;

    private String description;
}
