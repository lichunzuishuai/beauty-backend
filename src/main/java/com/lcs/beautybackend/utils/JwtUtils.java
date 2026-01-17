package com.lcs.beautybackend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 */
@Slf4j
public class JwtUtils {

    /**
     * 密钥（至少32字符）
     */
    private static final String SECRET = "beauty_backend_jwt_secret_key_2026_anyan";

    /**
     * Token 过期时间（7天）
     */
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成密钥
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT Token
     */
    public static String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 获取角色
     *
     * @param token JWT Token
     * @return 角色
     */
    public static Integer getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("role", Integer.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        return parseToken(token) != null;
    }
}
