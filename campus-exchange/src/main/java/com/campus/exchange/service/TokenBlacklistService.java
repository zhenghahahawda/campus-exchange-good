package com.campus.exchange.service;

/**
 * Token 黑名单服务接口
 * 用于在退出登录或封禁用户时使 Token 失效
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单
     * @param token JWT Token
     * @param expirationSeconds Token 剩余有效期（秒），用作 Redis TTL
     */
    void addToBlacklist(String token, long expirationSeconds);

    /**
     * 检查 Token 是否在黑名单中
     * @param token JWT Token
     * @return true 表示已在黑名单中（已失效）
     */
    boolean isBlacklisted(String token);
}
