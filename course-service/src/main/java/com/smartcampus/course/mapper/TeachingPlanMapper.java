package com.smartcampus.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.course.entity.TeachingPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教学计划数据访问层。
 */
@Mapper
public interface TeachingPlanMapper extends BaseMapper<TeachingPlan> {
}
