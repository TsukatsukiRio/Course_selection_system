package com.smartcampus.selection.feign.fallback;

import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.result.R;
import com.smartcampus.common.result.ResultCode;
import com.smartcampus.selection.feign.UserClient;
import com.smartcampus.selection.vo.UserVO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务 Feign 降级工厂。
 */
@Component
public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return usernames -> {
            throw new BizException(ResultCode.SERVICE_UNAVAILABLE, "用户服务暂时不可用，请稍后重试");
        };
    }
}
