package com.smartcampus.selection.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.result.R;
import com.smartcampus.common.result.ResultCode;
import com.smartcampus.selection.dto.SelectDTO;
import com.smartcampus.selection.service.ConfigService;
import com.smartcampus.selection.service.SelectionService;
import com.smartcampus.selection.vo.ConflictCheckVO;
import com.smartcampus.selection.vo.ScheduleItemVO;
import com.smartcampus.selection.vo.SelectionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生选退课接口（UC-07 / UC-08 / UC-09）。
 * 选课接口由 Sentinel 保护：限流时返回 429，异常时降级返回友好提示。
 */
@Slf4j
@RestController
@RequestMapping("/api/selection")
@RequiredArgsConstructor
public class SelectionController {

    private final SelectionService selectionService;
    private final ConfigService configService;

    /** 选课（Sentinel 限流资源 select-course） */
    @PostMapping("/select")
    @SentinelResource(value = "select-course", blockHandler = "selectBlocked", fallback = "selectFallback")
    public R<SelectionVO> select(@RequestHeader(HeaderConstants.X_USERNAME) String studentNo,
                                 @Valid @RequestBody SelectDTO dto) {
        return R.ok(selectionService.select(studentNo, dto));
    }

    /** Sentinel 限流处理：返回 429 */
    public R<SelectionVO> selectBlocked(String studentNo, SelectDTO dto, BlockException e) {
        log.warn("选课接口触发限流: {}", studentNo);
        return R.fail(ResultCode.TOO_MANY_REQUESTS.getCode(), "当前选课人数较多，请稍后重试");
    }

    /** Sentinel 降级处理：业务异常原样抛出，其余异常返回友好提示 */
    public R<SelectionVO> selectFallback(String studentNo, SelectDTO dto, Throwable e) {
        if (e instanceof BizException bizException) {
            throw bizException;
        }
        log.error("选课接口异常降级", e);
        return R.fail(ResultCode.SERVICE_UNAVAILABLE.getCode(), "选课服务繁忙，请稍后重试");
    }

    /** 退课 */
    @PostMapping("/drop/{recordId}")
    public R<Void> drop(@RequestHeader(HeaderConstants.X_USERNAME) String studentNo,
                        @PathVariable Long recordId) {
        selectionService.drop(studentNo, recordId);
        return R.ok();
    }

    /** 我的选课记录 */
    @GetMapping("/my")
    public R<List<SelectionVO>> my(@RequestHeader(HeaderConstants.X_USERNAME) String studentNo,
                                   @RequestParam(required = false) String semester,
                                   @RequestParam(defaultValue = "false") boolean onlyActive) {
        return R.ok(selectionService.mySelections(studentNo, semester, onlyActive));
    }

    /** 我的课表（周视图数据） */
    @GetMapping("/schedule")
    public R<List<ScheduleItemVO>> schedule(@RequestHeader(HeaderConstants.X_USERNAME) String studentNo,
                                            @RequestParam(required = false) String semester) {
        return R.ok(selectionService.schedule(studentNo, semester));
    }

    /** 选课预检测（冲突详情） */
    @GetMapping("/conflict-check")
    public R<ConflictCheckVO> conflictCheck(@RequestHeader(HeaderConstants.X_USERNAME) String studentNo,
                                            @RequestParam String courseNo) {
        return R.ok(selectionService.conflictCheck(studentNo, courseNo));
    }

    /** 选课/退课窗口状态（供前端按钮置灰） */
    @GetMapping("/open-status")
    public R<Map<String, Object>> openStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime selectStart = configService.getDateTime("select_start");
        LocalDateTime selectEnd = configService.getDateTime("select_end");
        LocalDateTime dropEnd = configService.getDateTime("drop_end");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("selectOpen", selectStart != null && selectEnd != null
                && !now.isBefore(selectStart) && !now.isAfter(selectEnd));
        result.put("dropOpen", dropEnd != null && !now.isAfter(dropEnd));
        result.put("selectStart", selectStart);
        result.put("selectEnd", selectEnd);
        result.put("dropEnd", dropEnd);
        result.put("maxCourseCount", configService.getInt("max_course_count", 5));
        result.put("maxCredits", configService.getInt("max_credits", 30));
        return R.ok(result);
    }
}
