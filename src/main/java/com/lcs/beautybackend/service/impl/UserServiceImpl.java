package com.lcs.beautybackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.constant.UserConstant;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.mapper.UserMapper;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.LoginUserVO;
import com.lcs.beautybackend.model.vo.UserVO;
import com.lcs.beautybackend.service.UserService;
import com.lcs.beautybackend.utils.DesensitizeUtils;
import com.lcs.beautybackend.utils.JwtUtils;
import com.lcs.beautybackend.config.JwtInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lcs
 * @description 针对表【user(用户表)】的数据库操作Service实现
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 盐值，用于密码加密
     */
    private static final String SALT = "beauty_salt_2026";

    @Override
    public long userRegister(String username, String password, String confirmPassword, String phone) {
        // 1. 参数校验
        if (StrUtil.hasBlank(username, password, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4 || username.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度需在4-20位之间");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度需在6-20位之间");
        }
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2. 校验用户名是否已存在
        synchronized (username.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
            }

            // 3. 密码加密
            String encryptPassword = DigestUtil.md5Hex(SALT + password);

            // 4. 创建用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(encryptPassword);
            user.setPhone(phone);
            user.setRole(UserConstant.DEFAULT_ROLE);
            user.setStatus(UserConstant.STATUS_NORMAL);

            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String username, String password, HttpServletRequest request) {
        // 1. 参数校验
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }

        // 2. 密码加密
        String encryptPassword = DigestUtil.md5Hex(SALT + password);

        // 3. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);

        if (user == null) {
            log.info("用户登录失败，用户名或密码错误");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }

        // 4. 检查用户状态
        if (user.getStatus() != UserConstant.STATUS_NORMAL) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }

        // 5. 生成 JWT Token
        String token = JwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 6. 返回脱敏用户信息（包含 Token）
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 优先从 JWT 拦截器设置的 Request Attribute 获取用户
        Object userObj = request.getAttribute(JwtInterceptor.REQUEST_USER_KEY);
        if (userObj != null) {
            return (User) userObj;
        }

        // 兼容 Session 方式（向后兼容）
        userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 从数据库查询最新信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 优先从 JWT 拦截器设置的 Request Attribute 获取用户
        Object userObj = request.getAttribute(JwtInterceptor.REQUEST_USER_KEY);
        if (userObj != null) {
            return (User) userObj;
        }

        // 兼容 Session 方式
        userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询最新信息
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        // 脱敏处理
        userVO.setPhone(DesensitizeUtils.desensitizePhone(user.getPhone()));
        userVO.setEmail(DesensitizeUtils.desensitizeEmail(user.getEmail()));
        userVO.setAvatar(user.getAvatar());
        userVO.setRole(user.getRole());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUsername(user.getUsername());
        loginUserVO.setNickname(user.getNickname());
        // 脱敏处理
        loginUserVO.setPhone(DesensitizeUtils.desensitizePhone(user.getPhone()));
        loginUserVO.setEmail(DesensitizeUtils.desensitizeEmail(user.getEmail()));
        loginUserVO.setAvatar(user.getAvatar());
        loginUserVO.setRole(user.getRole());
        loginUserVO.setCreateTime(user.getCreateTime());
        return loginUserVO;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE == user.getRole();
    }

    @Override
    public boolean isArtist(User user) {
        return user != null && UserConstant.ARTIST_ROLE == user.getRole();
    }
}
