package com.smartcampus.stats.feign;

import com.smartcampus.common.result.R;
import com.smartcampus.stats.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 用户服务 Feign 客户端。
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/user/internal/students")
    R<List<UserVO>> students();
}
