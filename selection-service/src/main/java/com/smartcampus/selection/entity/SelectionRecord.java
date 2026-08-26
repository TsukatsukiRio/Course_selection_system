package com.smartcampus.selection.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课记录实体，对应表 selection_record。
 * 状态机：SELECTED(已选) -> DROPPED(已退)。
 */
@Data
@TableName("selection_record")
public class SelectionRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentNo;

    private String courseNo;

    private String semester;

    /** SELECTED已选 / DROPPED已退 */
    private String status;

    private LocalDateTime selectTime;

    private LocalDateTime dropTime;
}
