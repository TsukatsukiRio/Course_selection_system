package com.smartcampus.user.controller;

import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.result.R;
import com.smartcampus.user.dto.ChangePasswordDTO;
import com.smartcampus.user.dto.UpdateProfileDTO;
import com.smartcampus.user.service.UserService;
import com.smartcampus.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人信息接口：查看/修改资料与密码。
 */
@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public R<UserVO> profile(@RequestHeader(HeaderConstants.X_USERNAME) String username) {
        return R.ok(userService.getByUsername(username));
    }

    @PutMapping
    public R<UserVO> update(@RequestHeader(HeaderConstants.X_USERNAME) String username,
                            @Valid @RequestBody UpdateProfileDTO dto) {
        return R.ok(userService.updateProfile(username, dto));
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestHeader(HeaderConstants.X_USERNAME) String username,
                                  @Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(username, dto);
        return R.ok();
    }
}
