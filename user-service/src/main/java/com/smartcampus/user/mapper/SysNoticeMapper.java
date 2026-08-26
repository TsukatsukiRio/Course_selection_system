package com.smartcampus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.user.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告数据访问层。
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {
}
