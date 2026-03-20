package com.campus.exchange.service;

/**
 * 用户状态缓存服务
 * 用于管理用户状态的 Redis 缓存
 */
public interface UserStatusCacheService {

    /**
     * 清除指定用户的状态缓存
     * @param userId 用户ID
     */
    void clearUserStatusCache(Integer userId);

    /**
     * 批量清除用户状态缓存
     * @param userIds 用户ID列表
     */
    void clearUserStatusCacheBatch(java.util.List<Integer> userIds);
}
