package com.smartcampus.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.result.R;
import com.smartcampus.user.dto.CreateUserDTO;
import com.smartcampus.user.dto.ResetPasswordDTO;
import com.smartcampus.user.dto.UpdateUserDTO;
import com.smartcampus.user.service.UserService;
import com.smartcampus.user.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理员用户管理接口（UC-03）。
 */
@RestController
@RequestMapping("/api/user/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/page")
    public R<Page<UserVO>> page(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String role,
                                @RequestParam(required = false) String college,
                                @RequestParam(required = false) Integer status,
                                @RequestParam(defaultValue = "1") long pageNum,
                                @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(userService.pageUsers(keyword, role, college, status, pageNum, pageSize));
    }

    @PostMapping
    public R<Void> create(@Valid @RequestBody CreateUserDTO dto) {
        userService.createUser(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO dto) {
        userService.updateUser(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/reset-password")
    public R<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(id, dto.getNewPassword());
        return R.ok();
    }

    /** 批量导入学生：CSV 文本（表头：学号,姓名,院系,专业,邮箱） */
    @PostMapping("/import")
    public R<Map<String, Integer>> importStudents(@RequestBody Map<String, String> body) {
        int count = userService.importStudents(body.get("csv"));
        return R.ok(Map.of("imported", count));
    }
}
