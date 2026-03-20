package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.UserSession;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserSessionService extends IService<UserSession> {
    
    UserSession createSession(Integer userId, String token, String refreshToken, HttpServletRequest request);
    
    void updateLastActivity(String sessionId);
    
    void invalidateSession(String token);
    
    UserSession findByToken(String token);
    
    List<UserSession> getActiveSessions(Integer userId);
    
    void forceLogout(String sessionId);
    
    void cleanExpiredSessions();
    
    void invalidateAllUserSessions(Integer userId);

    /**
     * 通过 userId 找到活跃 session 并更新为新 token（refresh-token 场景）
     */
    void refreshSessionByUserId(Integer userId, String newAccessToken, String newRefreshToken);
}
