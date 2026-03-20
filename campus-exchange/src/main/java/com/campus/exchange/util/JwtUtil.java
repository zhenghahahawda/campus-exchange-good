package com.campus.exchange.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类 (JJWT 0.12.x)
 */
@Component
public class JwtUtil {

    /**
     * 密钥（至少32字节以满足HS256要求）
     */
    private static final String SECRET_KEY_STRING = "campus_exchange_secret_key_2026_secure_enough_for_hs256";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    /**
     * 访问令牌有效期（2小时）
     */
    private static final long ACCESS_TOKEN_EXPIRATION = 2 * 60 * 60 * 1000;

    /**
     * 刷新令牌有效期（1天）
     */
    private static final long REFRESH_TOKEN_EXPIRATION = 1 * 24 * 60 * 60 * 1000;

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Integer userId, String username, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);
        claims.put("tokenType", "access");
        return createToken(claims, username, ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Integer userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("tokenType", "refresh");
        return createToken(claims, username, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (Integer) claims.get("userId") : null;
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从令牌中获取用户类型
     */
    public Integer getUserTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (Integer) claims.get("userType") : null;
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断令牌是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取访问令牌有效期（秒）
     */
    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION / 1000;
    }

    /**
     * 获取令牌剩余有效期（秒）
     * @return 剩余秒数，如果令牌无效则返回0
     */
    public long getRemainingExpiration(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return 0;
        }
        long expMillis = claims.getExpiration().getTime();
        long remaining = (expMillis - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0);
    }

    /**
     * 验证刷新令牌是否有效
     * @return 有效则返回Claims，无效返回null
     */
    public Claims validateRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null || isTokenExpired(claims)) {
            return null;
        }
        String tokenType = (String) claims.get("tokenType");
        if (!"refresh".equals(tokenType)) {
            return null;
        }
        return claims;
    }
}
