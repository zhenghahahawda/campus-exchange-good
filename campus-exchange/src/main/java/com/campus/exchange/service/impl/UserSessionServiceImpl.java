package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.UserSession;
import com.campus.exchange.mapper.UserSessionMapper;
import com.campus.exchange.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户会话服务实现类
 */
@Service
public class UserSessionServiceImpl implements UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(UserSessionServiceImpl.class);
    private static final String SESSION_CACHE_PREFIX = "session:";
    private static final long SESSION_CACHE_TTL_HOURS = 2;

    @Autowired
    private UserSessionMapper sessionMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public UserSession createSession(UserSession session) {
        sessionMapper.insert(session);
        // 写入 Redis 缓存
        cacheSession(session);
        return session;
    }

    @Override
    public UserSession getSessionById(String sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public UserSession getSessionByToken(String token) {
        // 优先从 Redis 读取
        if (redisTemplate != null && token != null) {
            try {
                String key = SESSION_CACHE_PREFIX + token;
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached instanceof UserSession) {
                    return (UserSession) cached;
                }
            } catch (Exception e) {
                log.warn("从Redis读取会话缓存失败: {}", e.getMessage());
            }
        }

        // Redis miss，查数据库
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token", token);
        UserSession session = sessionMapper.selectOne(queryWrapper);

        // 回填缓存
        if (session != null) {
            cacheSession(session);
        }
        return session;
    }

    @Override
    public List<UserSession> getSessionsByUserId(Integer userId) {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created_at");
        return sessionMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserSession> getActiveSessionsByUserId(Integer userId) {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_active", 1);
        queryWrapper.orderByDesc("last_activity_at");
        return sessionMapper.selectList(queryWrapper);
    }

    @Override
    public PageResponse<UserSession> pageSessions(PageRequest pageRequest) {
        // 查询前自动将过期会话标记为失效
        deactivateExpiredSessions();

        Page<UserSession> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();

        // 状态筛选
        if (pageRequest.getIsActive() != null) {
            queryWrapper.eq("is_active", pageRequest.getIsActive());
        }

        // 关键词搜索
        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().trim().isEmpty()) {
            String kw = pageRequest.getKeyword().trim();
            queryWrapper.and(w -> w
                .like("session_id", kw)
                .or().like("user_id", kw)
                .or().like("ip_address", kw)
                .or().like("device_id", kw)
                .or().like("user_agent", kw)
            );
        }

        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        IPage<UserSession> result = sessionMapper.selectPage(page, queryWrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public PageResponse<UserSession> pageSessionsByUserId(Integer userId, PageRequest pageRequest) {
        Page<UserSession> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        IPage<UserSession> result = sessionMapper.selectPage(page, queryWrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public UserSession updateSession(String sessionId, UserSession session) {
        session.setSessionId(sessionId);
        sessionMapper.updateById(session);
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public void updateLastActivity(String sessionId) {
        UserSession session = new UserSession();
        session.setSessionId(sessionId);
        session.setLastActivityAt(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    @Override
    public void deleteSession(String sessionId) {
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public void batchDeleteSessions(List<String> sessionIds) {
        if (sessionIds != null && !sessionIds.isEmpty()) {
            sessionMapper.deleteBatchIds(sessionIds);
        }
    }

    @Override
    public void deleteSessionsByUserId(Integer userId) {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        sessionMapper.delete(queryWrapper);
    }

    @Override
    public void deactivateSession(String sessionId) {
        UserSession session = new UserSession();
        session.setSessionId(sessionId);
        session.setIsActive(0);
        sessionMapper.updateById(session);
    }

    @Override
    public void deactivateByToken(String token) {
        UserSession session = getSessionByToken(token);
        if (session != null) {
            session.setIsActive(0);
            sessionMapper.updateById(session);
            // 清除缓存
            removeSessionCache(token);
        }
    }

    @Override
    public void updateTokenByRefreshToken(String oldRefreshToken, String newToken, String newRefreshToken, LocalDateTime newExpiresAt) {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("refresh_token", oldRefreshToken);
        queryWrapper.eq("is_active", 1);
        UserSession session = sessionMapper.selectOne(queryWrapper);
        if (session != null) {
            // 清除旧 token 缓存
            removeSessionCache(session.getToken());
            session.setToken(newToken);
            session.setRefreshToken(newRefreshToken);
            session.setExpiresAt(newExpiresAt);
            session.setLastActivityAt(LocalDateTime.now());
            sessionMapper.updateById(session);
            // 写入新 token 缓存
            cacheSession(session);
        }
    }

    @Override
    public void deactivateSessionsByUserId(Integer userId) {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_active", 1);

        List<UserSession> sessions = sessionMapper.selectList(queryWrapper);
        for (UserSession session : sessions) {
            session.setIsActive(0);
            sessionMapper.updateById(session);
        }
    }

    @Override
    public int cleanExpiredSessions() {
        // 先将已过期但仍标记为活跃的会话标记为失效
        deactivateExpiredSessions();
        // 再删除所有已失效的会话
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", 0);
        long count = sessionMapper.selectCount(queryWrapper);
        sessionMapper.delete(queryWrapper);
        return (int) count;
    }

    @Override
    public void deactivateExpiredSessions() {
        QueryWrapper<UserSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", 1);
        queryWrapper.lt("expires_at", LocalDateTime.now());
        List<UserSession> expiredSessions = sessionMapper.selectList(queryWrapper);
        for (UserSession session : expiredSessions) {
            session.setIsActive(0);
            sessionMapper.updateById(session);
        }
    }

    @Override
    public void batchDeactivateSessions(List<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) return;
        for (String sessionId : sessionIds) {
            deactivateSession(sessionId);
        }
    }

    @Override
    public java.util.Map<String, Long> getStats() {
        deactivateExpiredSessions();
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        long total = sessionMapper.selectCount(null);
        QueryWrapper<UserSession> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("is_active", 1);
        long active = sessionMapper.selectCount(activeWrapper);
        stats.put("total", total);
        stats.put("active", active);
        stats.put("inactive", total - active);
        return stats;
    }

    /**
     * 将会话写入 Redis 缓存
     */
    private void cacheSession(UserSession session) {
        if (redisTemplate == null || session == null || session.getToken() == null) return;
        try {
            String key = SESSION_CACHE_PREFIX + session.getToken();
            // 计算 TTL：使用会话过期时间，默认 2 小时
            long ttlSeconds = SESSION_CACHE_TTL_HOURS * 3600;
            if (session.getExpiresAt() != null) {
                long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getExpiresAt());
                if (remaining > 0) {
                    ttlSeconds = remaining;
                }
            }
            redisTemplate.opsForValue().set(key, session, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("会话写入Redis缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 从 Redis 中删除会话缓存
     */
    private void removeSessionCache(String token) {
        if (redisTemplate == null || token == null) return;
        try {
            redisTemplate.delete(SESSION_CACHE_PREFIX + token);
        } catch (Exception e) {
            log.warn("删除Redis会话缓存失败: {}", e.getMessage());
        }
    }
}
