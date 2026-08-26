package com.smartcampus.selection.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 选课预检测结果。
 */
@Data
@AllArgsConstructor
public class ConflictCheckVO {

    /** 是否可正常选课 */
    private boolean ok;

    /** 提示信息 */
    private String message;
}
