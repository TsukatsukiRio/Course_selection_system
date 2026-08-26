package com.smartcampus.selection.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.result.R;
import com.smartcampus.selection.service.ConfigService;
import com.smartcampus.selection.service.SelectionService;
import com.smartcampus.selection.vo.SelectionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员接口：选课参数配置与选课记录管理。
 */
@RestController
@RequestMapping("/api/selection/admin")
@RequiredArgsConstructor
public class AdminSelectionController {

    private final ConfigService configService;
    private final SelectionService selectionService;

    /** 查询全部选课参数 */
    @GetMapping("/config")
    public R<Map<String, String>> config() {
        return R.ok(configService.all());
    }

    /** 批量更新选课参数 */
    @PutMapping("/config")
    public R<Void> updateConfig(@RequestBody Map<String, String> configs) {
        configService.update(configs);
        return R.ok();
    }

    /** 全部选课记录分页 */
    @GetMapping("/records")
    public R<Page<SelectionVO>> records(@RequestParam(required = false) String studentNo,
                                        @RequestParam(required = false) String courseNo,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(selectionService.pageRecords(studentNo, courseNo, status, pageNum, pageSize));
    }
}
