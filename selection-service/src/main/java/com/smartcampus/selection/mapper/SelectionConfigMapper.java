package com.smartcampus.selection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.selection.entity.SelectionConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 选课参数配置数据访问层。
 */
@Mapper
public interface SelectionConfigMapper extends BaseMapper<SelectionConfig> {
}
