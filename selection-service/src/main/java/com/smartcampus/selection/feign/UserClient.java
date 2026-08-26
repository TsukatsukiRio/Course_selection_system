package com.smartcampus.selection.feign;

import com.smartcampus.common.result.R;
import com.smartcampus.selection.feign.fallback.UserClientFallback;
import com.smartcampus.selection.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务 Feign 客户端。
 */
@FeignClient(name = "user-service", fallbackFactory = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/user/internal/batch")
    R<List<UserVO>> batch(@RequestParam("usernames") String usernames);
}
