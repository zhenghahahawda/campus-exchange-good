package org.example.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.Result;
import org.example.entity.User;
import org.example.entity.UserSession;
import org.example.service.UserService;
import org.example.service.UserSessionService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserSessionService userSessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Handle OPTIONS request for CORS
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // If not a method handler (e.g. static resource), pass
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 公开接口（GET /api/user/{数字id}、GET /api/goods 等）无 token 也放行
            String uri = request.getRequestURI();
            String method = request.getMethod();
            if ("GET".equals(method) && uri.matches("/api/user/\\d+")) {
                return true;
            }
            if ("GET".equals(method) && (uri.equals("/api/goods") || uri.equals("/api/goods/categories") || uri.startsWith("/api/goods/") && uri.matches("/api/goods/\\d+"))) {
                return true;
            }
            if ("POST".equals(method) && uri.equals("/api/auth/reset-password")) {
                return true;
            }
            return unauthorized(response, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String tokenSuffix = token.length() > 20 ? token.substring(token.length() - 20) : token;
        String uri = request.getRequestURI();

        try {
            // Extract and validate token
            String userId = jwtUtil.extractUserId(token);
            String username = jwtUtil.extractUsername(token);
            Integer userType = jwtUtil.extractUserType(token);
            String tokenType = jwtUtil.extractTokenType(token);

            System.out.printf("[AUTH] %s %s | user=%s | token=...%s%n",
                    request.getMethod(), uri, userId, tokenSuffix);

            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                System.err.printf("[AUTH-FAIL] token已过期 | user=%s | uri=%s%n", userId, uri);
                return unauthorized(response, "Token has expired");
            }
            
            // Validate token signature and username
            if (!jwtUtil.validateToken(token, username)) {
                System.err.printf("[AUTH-FAIL] token签名无效 | user=%s | uri=%s%n", userId, uri);
                return unauthorized(response, "Invalid token");
            }
            
            // Check token type (should be access token for API requests)
            if (!"access".equals(tokenType)) {
                System.err.printf("[AUTH-FAIL] token类型错误=%s | user=%s | uri=%s%n", tokenType, userId, uri);
                return unauthorized(response, "Invalid token type");
            }
            
            // 验证 session 是否存在且有效
            UserSession session = null;
            try {
                session = userSessionService.findByToken(token);
            } catch (Exception e) {
                System.err.printf("[AUTH-FAIL] findByToken异常 | user=%s | uri=%s | err=%s%n", userId, uri, e.getMessage());
            }
            if (session == null) {
                System.err.printf("[AUTH-FAIL] session不存在(Redis+DB均无) | user=%s | token=...%s | uri=%s%n", userId, tokenSuffix, uri);
                return unauthorized(response, "会话已失效，请重新登录");
            }
            if (session.getIsActive() != 1) {
                System.err.printf("[AUTH-FAIL] session已失效(isActive=%d) | user=%s | uri=%s%n", session.getIsActive(), userId, uri);
                return unauthorized(response, "会话已失效，请重新登录");
            }
            // session 过期检查
            if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(java.time.LocalDateTime.now().minusDays(7))) {
                System.err.printf("[AUTH-FAIL] session超过7天未续期 | user=%s | uri=%s%n", userId, uri);
                return unauthorized(response, "会话已过期，请重新登录");
            }
            
            // 查询用户实时状态
            User user = userService.getById(userId);
            if (user == null) {
                System.err.printf("[AUTH-FAIL] 用户不存在 | userId=%s | uri=%s%n", userId, uri);
                return unauthorized(response, "User not found");
            }
            
            // 检查用户封禁状态
            if (user.getIsBanned() != null && user.getIsBanned() == 1) {
                return forbidden(response, "账户已被封禁，无法访问");
            }
            
            // 检查用户状态：0=禁用
            if (user.getStatus() != null && user.getStatus() == 0) {
                return forbidden(response, "账户已被禁用，无法访问");
            }
            
            // 更新最后活动时间
            userSessionService.updateLastActivity(session.getSessionId());
            
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("userType", userType);
            request.setAttribute("userStatus", user.getStatus());
            
            return true;
            
        } catch (Exception e) {
            System.err.printf("[AUTH-FAIL] 异常 | uri=%s | token=...%s | err=%s%n", uri, tokenSuffix, e.getMessage());
            return unauthorized(response, "Token validation failed: " + e.getMessage());
        }
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.error(40101, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
    
    private boolean forbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> result = Result.error(40301, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }
}
