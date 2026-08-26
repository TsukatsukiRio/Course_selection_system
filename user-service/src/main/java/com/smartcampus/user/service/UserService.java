package com.smartcampus.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.jwt.JwtUtils;
import com.smartcampus.user.dto.ChangePasswordDTO;
import com.smartcampus.user.dto.CreateUserDTO;
import com.smartcampus.user.dto.LoginDTO;
import com.smartcampus.user.dto.RegisterDTO;
import com.smartcampus.user.dto.UpdateProfileDTO;
import com.smartcampus.user.dto.UpdateUserDTO;
import com.smartcampus.user.entity.SysUser;
import com.smartcampus.user.mapper.SysUserMapper;
import com.smartcampus.user.vo.LoginVO;
import com.smartcampus.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务：注册、登录（含锁定策略）、个人信息与管理员用户管理。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 30;

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 学生注册：默认角色 STUDENT */
    public void register(RegisterDTO dto) {
        if (existsUsername(dto.getUsername())) {
            throw new BizException("该学号已注册");
        }
        if (StringUtils.hasText(dto.getEmail()) && existsEmail(dto.getEmail())) {
            throw new BizException("该邮箱已注册");
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("STUDENT");
        user.setStatus(1);
        user.setFailCount(0);
        userMapper.insert(user);
    }

    /** 登录：连续失败 5 次锁定 30 分钟，成功后重置计数并签发 JWT */
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            if (user != null) {
                int failCount = (user.getFailCount() == null ? 0 : user.getFailCount()) + 1;
                LocalDateTime lockedUntil = failCount >= MAX_FAIL_COUNT
                        ? LocalDateTime.now().plusMinutes(LOCK_MINUTES) : null;
                userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, user.getId())
                        .set(SysUser::getFailCount, failCount)
                        .set(SysUser::getLockedUntil, lockedUntil));
                if (lockedUntil != null) {
                    throw new BizException("连续错误5次，账号已锁定30分钟");
                }
            }
            throw new BizException("账号或密码不正确");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new BizException("账号已锁定，请30分钟后重试");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException("账号已被禁用，请联系管理员");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getFailCount, 0)
                .set(SysUser::getLockedUntil, null));
        String token = jwtUtils.createToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, toVO(user));
    }

    public UserVO getByUsername(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return toVO(user);
    }

    public UserVO updateProfile(String username, UpdateProfileDTO dto) {
        SysUser user = requireUser(username);
        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(user.getEmail())
                && existsEmail(dto.getEmail())) {
            throw new BizException("该邮箱已被注册");
        }
        BeanUtils.copyProperties(dto, user);
        userMapper.updateById(user);
        return getByUsername(username);
    }

    public void changePassword(String username, ChangePasswordDTO dto) {
        SysUser user = requireUser(username);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getPassword, passwordEncoder.encode(dto.getNewPassword())));
    }

    /** 管理员：分页查询用户 */
    public Page<UserVO> pageUsers(String keyword, String role, String college, Integer status,
                                  long pageNum, long pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getName, keyword));
        }
        wrapper.eq(StringUtils.hasText(role), SysUser::getRole, role)
                .like(StringUtils.hasText(college), SysUser::getCollege, college)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<UserVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    /** 管理员：创建教师/管理员账号 */
    public void createUser(CreateUserDTO dto) {
        if (existsUsername(dto.getUsername())) {
            throw new BizException("该工号已存在");
        }
        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setFailCount(0);
        userMapper.insert(user);
    }

    /** 管理员：编辑用户信息与启用/禁用 */
    public void updateUser(Long id, UpdateUserDTO dto) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        BeanUtils.copyProperties(dto, user);
        userMapper.updateById(user);
    }

    /** 管理员：重置密码 */
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, id)
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword)));
    }

    /** 管理员：CSV 批量导入学生（表头：学号,姓名,院系,专业,邮箱） */
    public int importStudents(String csv) {
        if (!StringUtils.hasText(csv)) {
            throw new BizException("导入内容不能为空");
        }
        String[] lines = csv.replace("\r", "").split("\n");
        int imported = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] cols = line.split(",", -1);
            if (cols.length < 3) {
                throw new BizException("第" + (i + 1) + "行格式错误，至少需要学号,姓名,院系");
            }
            RegisterDTO dto = new RegisterDTO();
            dto.setUsername(cols[0].trim());
            dto.setName(cols[1].trim());
            dto.setCollege(cols[2].trim());
            dto.setMajor(cols.length > 3 ? cols[3].trim() : null);
            dto.setEmail(cols.length > 4 && !cols[4].trim().isEmpty() ? cols[4].trim() : null);
            dto.setPassword("Pass1234");
            if (existsUsername(dto.getUsername())) {
                continue;
            }
            register(dto);
            imported++;
        }
        return imported;
    }

    /** 内部接口：按学号批量查询 */
    public List<UserVO> batchByUsernames(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getUsername, usernames))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 内部接口：所有学生列表（用于统计院系选课率） */
    public List<UserVO> listStudents() {
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, "STUDENT"))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    private boolean existsUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0;
    }

    private boolean existsEmail(String email) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email)) > 0;
    }

    private SysUser requireUser(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    public UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
