package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.UserSession;
import org.example.mapper.UserSessionMapper;
import org.example.service.UserSessionService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserSessionServiceImpl extends ServiceImpl<UserSessionMapper, UserSession> implements UserSessionService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_KEY_PREFIX = "session:token:";
    private static final long SESSION_TTL_HOURS = 2;

    @Override
    public UserSession createSession(Integer userId, String token, String refreshToken, HttpServletRequest request) {
        UserSession session = new UserSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setToken(token);
        session.setRefreshToken(refreshToken);
        session.setCreatedAt(LocalDateTime.now());
        
        // Access token expires in 2 hours, refresh token in 7 days
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        session.setLastActivityAt(LocalDateTime.now());
        
        // Extract IP and User-Agent from request
        session.setIpAddress(getClientIp(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        
        // Device ID can be extracted from custom header if provided
        session.setDeviceId(request.getHeader("X-Device-ID"));
        
        session.setIsActive(1); // 1=活跃
        
        this.save(session);
        // 写入 Redis，key=session:token:{token}，TTL=2小时
        redisTemplate.opsForValue().set(
            SESSION_KEY_PREFIX + token, session, SESSION_TTL_HOURS, TimeUnit.HOURS
        );
        return session;
    }

    @Override
    public void updateLastActivity(String sessionId) {
        // 只刷新 Redis TTL，不每次都写数据库，避免高频请求耗尽连接池
        // 数据库的 last_activity_at 低频更新（每5分钟一次）
        try {
            // 找到该 sessionId 对应的 Redis key 并续期
            // sessionId 不是 token，需要从数据库查 token，但这样又会查库
            // 改为：在拦截器里直接传 token 过来，这里暂时用异步低频写
            new Thread(() -> {
                try {
                    UserSession session = this.getById(sessionId);
                    if (session == null || session.getIsActive() != 1) return;
                    // 检查上次更新时间，5分钟内不重复写库
                    LocalDateTime lastActivity = session.getLastActivityAt();
                    if (lastActivity != null &&
                            lastActivity.isAfter(LocalDateTime.now().minusMinutes(5))) {
                        return; // 5分钟内已更新过，跳过
                    }
                    session.setLastActivityAt(LocalDateTime.now());
                    this.updateById(session);
                } catch (Exception e) {
                    System.err.println("updateLastActivity 失败: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("updateLastActivity 启动线程失败: " + e.getMessage());
        }
    }

    @Override
    public void invalidateSession(String token) {
        QueryWrapper<UserSession> wrapper = new QueryWrapper<>();
        wrapper.eq("token", token).eq("is_active", 1);
        
        UserSession session = this.getOne(wrapper);
        if (session != null) {
            session.setIsActive(0);
            this.updateById(session);
        }
        // 删除 Redis 缓存
        redisTemplate.delete(SESSION_KEY_PREFIX + token);
    }

    @Override
    public UserSession findByToken(String token) {
        // 先查 Redis
        try {
            Object cached = redisTemplate.opsForValue().get(SESSION_KEY_PREFIX + token);
            if (cached != null) {
                UserSession session = null;
                if (cached instanceof UserSession) {
                    session = (UserSession) cached;
                } else {
                    // Jackson 反序列化可能返回 LinkedHashMap，用 ObjectMapper 转换
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    session = mapper.convertValue(cached, UserSession.class);
                }
                if (session != null) {
                    // 刷新 Redis TTL，防止活跃用户被踢出
                    redisTemplate.expire(SESSION_KEY_PREFIX + token, SESSION_TTL_HOURS, TimeUnit.HOURS);
                    return session;
                }
            }
        } catch (Exception e) {
            System.err.println("Redis session 读取失败: " + e.getMessage());
        }
        // Redis 没有则查数据库
        QueryWrapper<UserSession> wrapper = new QueryWrapper<>();
        wrapper.eq("token", token).eq("is_active", 1);
        UserSession session = this.getOne(wrapper);
        if (session != null) {
            try {
                redisTemplate.opsForValue().set(
                    SESSION_KEY_PREFIX + token, session, SESSION_TTL_HOURS, TimeUnit.HOURS
                );
            } catch (Exception e) {
                System.err.println("Redis session 写入失败: " + e.getMessage());
            }
        }
        return session;
    }

    @Override
    public List<UserSession> getActiveSessions(Integer userId) {
        QueryWrapper<UserSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("is_active", 1)
               .orderByDesc("last_activity_at");
        return this.list(wrapper);
    }

    @Override
    public void forceLogout(String sessionId) {
        UserSession session = this.getById(sessionId);
        if (session != null) {
            session.setIsActive(0);
            this.updateById(session);
        }
    }

    @Override
    public void cleanExpiredSessions() {
        QueryWrapper<UserSession> wrapper = new QueryWrapper<>();
        wrapper.lt("expires_at", LocalDateTime.now())
               .eq("is_active", 1);
        
        List<UserSession> expiredSessions = this.list(wrapper);
        for (UserSession session : expiredSessions) {
            session.setIsActive(0);
            this.updateById(session);
        }
    }

    @Override
    public void invalidateAllUserSessions(Integer userId) {
        QueryWrapper<UserSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_active", 1);
        List<UserSession> sessions = this.list(wrapper);
        for (UserSession session : sessions) {
            session.setIsActive(0);
            this.updateById(session);
            // 同步删除 Redis 缓存
            redisTemplate.delete(SESSION_KEY_PREFIX + session.getToken());
        }
    }

    @Override
    public void refreshSessionByUserId(Integer userId, String newAccessToken, String newRefreshToken) {
        // 找到该用户最近的活跃 session
        QueryWrapper<UserSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_active", 1).orderByDesc("last_activity_at").last("LIMIT 1");
        UserSession session = this.getOne(wrapper);
        if (session == null) {
            // 没有活跃 session，说明已登出，不做任何处理
            return;
        }
        String oldToken = session.getToken();
        // 更新 session 的 token
        session.setToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        session.setLastActivityAt(LocalDateTime.now());
        this.updateById(session);
        // 删除旧 Redis key，写入新 key
        redisTemplate.delete(SESSION_KEY_PREFIX + oldToken);
        redisTemplate.opsForValue().set(
            SESSION_KEY_PREFIX + newAccessToken, session, SESSION_TTL_HOURS, TimeUnit.HOURS
        );
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Handle multiple IPs (take the first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
