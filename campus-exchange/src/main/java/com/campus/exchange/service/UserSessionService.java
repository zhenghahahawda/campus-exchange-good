package com.campus.exchange.service;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.UserSession;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 用户会话服务接口
 */
public interface UserSessionService {

    /**
     * 创建会话
     */
    UserSession createSession(UserSession session);

    /**
     * 根据会话ID获取会话
     */
    UserSession getSessionById(String sessionId);

    /**
     * 根据Token获取会话
     */
    UserSession getSessionByToken(String token);

    /**
     * 根据刷新令牌获取会话
     */
    UserSession getSessionByRefreshToken(String refreshToken);

    /**
     * 根据用户ID获取所有会话
     */
    List<UserSession> getSessionsByUserId(Integer userId);

    /**
     * 根据用户ID获取活跃会话
     */
    List<UserSession> getActiveSessionsByUserId(Integer userId);

    /**
     * 分页查询所有会话
     */
    PageResponse<UserSession> pageSessions(PageRequest pageRequest);

    /**
     * 根据用户ID分页查询会话
     */
    PageResponse<UserSession> pageSessionsByUserId(Integer userId, PageRequest pageRequest);

    /**
     * 更新会话
     */
    UserSession updateSession(String sessionId, UserSession session);

    /**
     * 更新最后活动时间
     */
    void updateLastActivity(String sessionId);

    /**
     * 删除会话
     */
    void deleteSession(String sessionId);

    /**
     * 批量删除会话
     */
    void batchDeleteSessions(List<String> sessionIds);

    /**
     * 删除用户所有会话
     */
    void deleteSessionsByUserId(Integer userId);

    /**
     * 使会话失效
     */
    void deactivateSession(String sessionId);

    /**
     * 根据Token使会话失效
     */
    void deactivateByToken(String token);

    /**
     * 根据旧的refreshToken更新会话的令牌
     */
    boolean updateTokenByRefreshToken(String oldRefreshToken, String newToken, String newRefreshToken, LocalDateTime newExpiresAt);

    /**
     * 使用户所有会话失效
     */
    void deactivateSessionsByUserId(Integer userId);

    /**
     * 清理过期会话
     * @return 清理的会话数量
     */
    int cleanExpiredSessions();

    /**
     * 将已过期的活跃会话标记为失效
     */
    void deactivateExpiredSessions();

    /**
     * 批量使会话失效
     */
    void batchDeactivateSessions(List<String> sessionIds);

    /**
     * 获取会话统计（总数、活跃数、失效数）
     */
    java.util.Map<String, Long> getStats();
}
