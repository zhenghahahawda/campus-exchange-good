package com.campus.exchange.service.impl;

import com.campus.exchange.service.UserStatusCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户状态缓存服务实现
 */
@Service
public class UserStatusCacheServiceImpl implements UserStatusCacheService {

    private static final String USER_STATUS_PREFIX = "user:status:";

    @Autowired(required = false)  // Redis 已禁用，设置为可选依赖
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void clearUserStatusCache(Integer userId) {
        if (userId == null || redisTemplate == null) {
            return;
        }
        try {
            String key = USER_STATUS_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Redis 异常不影响业务
        }
    }

    @Override
    public void clearUserStatusCacheBatch(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty() || redisTemplate == null) {
            return;
        }
        try {
            for (Integer userId : userIds) {
                clearUserStatusCache(userId);
            }
        } catch (Exception e) {
            // Redis 异常不影响业务
        }
    }
}
