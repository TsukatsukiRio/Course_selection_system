package com.smartcampus.user.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.user.service.UserService;
import com.smartcampus.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 内部接口：供 selection / recommend / statistics 服务通过 Feign 调用。
 * 网关已拦截外部访问（路径含 /internal/ 且无内部标记）。
 */
@RestController
@RequestMapping("/api/user/internal")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public R<UserVO> getByUsername(@PathVariable String username) {
        return R.ok(userService.getByUsername(username));
    }

    @GetMapping("/batch")
    public R<List<UserVO>> batch(@RequestParam String usernames) {
        return R.ok(userService.batchByUsernames(Arrays.asList(usernames.split(","))));
    }

    @GetMapping("/students")
    public R<List<UserVO>> students() {
        return R.ok(userService.listStudents());
    }
}
