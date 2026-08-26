package com.smartcampus.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.result.R;
import com.smartcampus.user.dto.NoticeDTO;
import com.smartcampus.user.entity.SysNotice;
import com.smartcampus.user.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统公告接口：全员可查看，管理员可维护。
 */
@RestController
@RequestMapping("/api/user/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<List<SysNotice>> list() {
        return R.ok(noticeService.listPublished());
    }

    @GetMapping("/admin/page")
    public R<Page<SysNotice>> page(@RequestParam(defaultValue = "1") long pageNum,
                                   @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(noticeService.page(pageNum, pageSize));
    }

    @PostMapping("/admin")
    public R<Void> create(@Valid @RequestBody NoticeDTO dto,
                          @RequestHeader(HeaderConstants.X_USERNAME) String publisher) {
        noticeService.create(dto, publisher);
        return R.ok();
    }

    @PutMapping("/admin/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeDTO dto) {
        noticeService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/admin/{id}")
    public R<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return R.ok();
    }
}
