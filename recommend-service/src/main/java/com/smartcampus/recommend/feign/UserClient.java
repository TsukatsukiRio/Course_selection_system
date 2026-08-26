package com.smartcampus.recommend.feign;

import com.smartcampus.common.result.R;
import com.smartcampus.recommend.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务 Feign 客户端。
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/user/internal/{username}")
    R<UserVO> getByUsername(@PathVariable("username") String username);
}
