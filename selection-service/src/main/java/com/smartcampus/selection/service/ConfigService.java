package com.smartcampus.selection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.selection.entity.SelectionConfig;
import com.smartcampus.selection.mapper.SelectionConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 选课参数配置服务：选课/退课窗口期、数量与学分上限等（管理员维护）。
 */
@Service
@RequiredArgsConstructor
public class ConfigService {

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SelectionConfigMapper configMapper;

    public Map<String, String> all() {
        List<SelectionConfig> list = configMapper.selectList(
                new LambdaQueryWrapper<SelectionConfig>().orderByAsc(SelectionConfig::getId));
        Map<String, String> result = new LinkedHashMap<>();
        for (SelectionConfig config : list) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    /** 更新配置（批量） */
    public void update(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SelectionConfig config = configMapper.selectOne(new LambdaQueryWrapper<SelectionConfig>()
                    .eq(SelectionConfig::getConfigKey, entry.getKey()));
            if (config == null) {
                throw new BizException("未知配置项：" + entry.getKey());
            }
            config.setConfigValue(entry.getValue());
            configMapper.updateById(config);
        }
    }

    public String get(String key) {
        SelectionConfig config = configMapper.selectOne(new LambdaQueryWrapper<SelectionConfig>()
                .eq(SelectionConfig::getConfigKey, key));
        return config == null ? null : config.getConfigValue();
    }

    public String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value == null ? defaultValue : value;
    }

    public LocalDateTime getDateTime(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getOrDefault(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
