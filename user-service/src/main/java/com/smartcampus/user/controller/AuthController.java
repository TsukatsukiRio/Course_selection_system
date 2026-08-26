package com.smartcampus.user.controller;

import com.smartcampus.common.result.R;
import com.smartcampus.user.dto.LoginDTO;
import com.smartcampus.user.dto.RegisterDTO;
import com.smartcampus.user.service.UserService;
import com.smartcampus.user.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册与登录（UC-01 / UC-02）。
 */
@RestController
@RequestMapping("/api/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 学生注册 */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return R.ok();
    }

    /** 登录，返回 JWT Token 与用户信息 */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }
}
