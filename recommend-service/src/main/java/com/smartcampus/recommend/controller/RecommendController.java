package com.smartcampus.recommend.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.result.R;
import com.smartcampus.recommend.service.RecommendService;
import com.smartcampus.recommend.vo.RecommendItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 课程推荐接口（UC-10）。
 * Sentinel 保护：限流或异常时降级返回热门课程兜底列表，保证基本体验。
 */
@Slf4j
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/list")
    @SentinelResource(value = "recommend-list", blockHandler = "recommendBlocked", fallback = "recommendFallback")
    public R<List<RecommendItemVO>> list(@RequestHeader(HeaderConstants.X_USERNAME) String studentNo) {
        return R.ok(recommendService.recommend(studentNo));
    }

    /** Sentinel 限流降级：返回热门课程兜底 */
    public R<List<RecommendItemVO>> recommendBlocked(String studentNo, BlockException e) {
        log.warn("推荐接口触发限流，返回热门课程兜底");
        return R.ok(recommendService.hotFallback());
    }

    /** Sentinel 熔断降级：返回热门课程兜底 */
    public R<List<RecommendItemVO>> recommendFallback(String studentNo, Throwable e) {
        log.error("推荐接口异常降级，返回热门课程兜底", e);
        return R.ok(recommendService.hotFallback());
    }

    /** 热门课程（白名单放行） */
    @GetMapping("/hot")
    public R<List<RecommendItemVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(recommendService.hotFallback());
    }
}
