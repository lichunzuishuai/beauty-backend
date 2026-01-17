package com.lcs.beautybackend.config;

import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.service.UserService;
import com.lcs.beautybackend.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 验证请求头中的 Token，并将用户信息存入 Request Attribute
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private UserService userService;

    /**
     * 用户信息在 Request 中的 Key
     */
    public static final String REQUEST_USER_KEY = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // OPTIONS 请求直接放行（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 没有 Token，允许继续访问（由具体接口判断是否需要登录）
            return true;
        }

        String token = authHeader.substring(7);

        // 验证 Token
        if (!JwtUtils.validateToken(token)) {
            // Token 无效，允许继续访问（由具体接口判断是否需要登录）
            return true;
        }

        // 从 Token 获取用户 ID
        Long userId = JwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            return true;
        }

        // 查询用户信息
        User user = userService.getById(userId);
        if (user != null) {
            // 将用户信息存入 Request Attribute
            request.setAttribute(REQUEST_USER_KEY, user);
        }

        return true;
    }
}
