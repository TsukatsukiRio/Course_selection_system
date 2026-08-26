package com.smartcampus.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.user.dto.NoticeDTO;
import com.smartcampus.user.entity.SysNotice;
import com.smartcampus.user.mapper.SysNoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统公告服务。
 */
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final SysNoticeMapper noticeMapper;

    /** 门户展示：最新 10 条已发布公告 */
    public List<SysNotice> listPublished() {
        return noticeMapper.selectList(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1)
                .orderByDesc(SysNotice::getPublishTime)
                .last("LIMIT 10"));
    }

    /** 管理端分页查询 */
    public Page<SysNotice> page(long pageNum, long pageSize) {
        return noticeMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysNotice>().orderByDesc(SysNotice::getPublishTime));
    }

    public void create(NoticeDTO dto, String publisher) {
        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        notice.setPublisher(publisher);
        noticeMapper.insert(notice);
    }

    public void update(Long id, NoticeDTO dto) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BizException(404, "公告不存在");
        }
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        if (dto.getStatus() != null) {
            notice.setStatus(dto.getStatus());
        }
        noticeMapper.updateById(notice);
    }

    public void delete(Long id) {
        noticeMapper.deleteById(id);
    }
}
