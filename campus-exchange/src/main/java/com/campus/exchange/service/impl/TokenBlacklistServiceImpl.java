package com.campus.exchange.service.impl;

import com.campus.exchange.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现
 * 使用 Redis 存储已失效的 Token，实现退出登录功能
 */
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);
    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addToBlacklist(String token, long expirationSeconds) {
        if (redisTemplate == null || token == null || expirationSeconds <= 0) {
            log.debug("Redis不可用或参数无效，跳过黑名单添加");
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", expirationSeconds, TimeUnit.SECONDS);
            log.info("Token已加入黑名单，TTL={}秒", expirationSeconds);
        } catch (Exception e) {
            log.warn("Token加入黑名单失败: {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (redisTemplate == null || token == null) {
            return false;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("检查Token黑名单失败: {}", e.getMessage());
            return false;
        }
    }
}
