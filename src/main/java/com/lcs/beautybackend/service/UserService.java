package com.lcs.beautybackend.service;

import com.lcs.beautybackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.LoginUserVO;
import com.lcs.beautybackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author lcs
 * @description 针对表【user(用户表)】的数据库操作Service
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param username        用户名
     * @param password        密码
     * @param confirmPassword 确认密码
     * @param phone           手机号（可选）
     * @return 新用户ID
     */
    long userRegister(String username, String password, String confirmPassword, String phone);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @param request  请求
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO userLogin(String username, String password, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request 请求
     * @return 当前登录用户，未登录返回null
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的登录用户信息
     *
     * @param user 用户
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 是否为管理员
     *
     * @param user 用户
     * @return 是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 是否为化妆师
     *
     * @param user 用户
     * @return 是否为化妆师
     */
    boolean isArtist(User user);
}
