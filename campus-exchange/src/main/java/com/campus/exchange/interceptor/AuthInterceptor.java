package com.campus.exchange.interceptor;

import com.campus.exchange.entity.UserSession;
import com.campus.exchange.service.TokenBlacklistService;
import com.campus.exchange.service.UserSessionService;
import com.campus.exchange.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证拦截器
 * 验证 Token 和用户 ID 的一致性，检查 Token 黑名单，验证会话记录
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserSessionService userSessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("=== JWT拦截器开始 === 请求路径: {}", request.getRequestURI());

        // 跨域预检请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            log.info("OPTIONS请求，直接放行");
            return true;
        }

        // 获取 Authorization 头
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader != null ? "存在" : "不存在");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("未提供认证令牌或格式错误");
            sendErrorResponse(response, 50014, "未提供认证令牌");
            return false;
        }

        // 提取 token
        String token = authHeader.substring(7);
        log.info("Token提取成功，长度: {}", token.length());

        // 验证 token 有效性
        boolean isValid = jwtUtil.validateToken(token);
        log.info("Token有效性验证: {}", isValid);

        if (!isValid) {
            log.warn("令牌无效或已过期");
            sendErrorResponse(response, 50014, "令牌无效或已过期");
            return false;
        }

        // 检查 Token 是否在黑名单中（已退出登录或被封禁）
        log.info("开始检查Token黑名单");
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
        log.info("Token黑名单检查结果: {}", isBlacklisted);

        if (isBlacklisted) {
            log.warn("令牌已在黑名单中");
            sendErrorResponse(response, 50014, "令牌已失效，请重新登录");
            return false;
        }

        // 检查会话记录是否存在且有效
        log.info("开始检查会话记录");
        UserSession session = userSessionService.getSessionByToken(token);
        if (session == null) {
            log.warn("会话记录不存在");
            sendErrorResponse(response, 50014, "会话不存在，请重新登录");
            return false;
        }

        if (session.getIsActive() == null || session.getIsActive() != 1) {
            log.warn("会话已失效: sessionId={}", session.getSessionId());
            sendErrorResponse(response, 50014, "会话已失效，请重新登录");
            return false;
        }

        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("会话已过期: sessionId={}, expiresAt={}", session.getSessionId(), session.getExpiresAt());
            sendErrorResponse(response, 50014, "会话已过期，请重新登录");
            return false;
        }

        log.info("会话记录验证通过: sessionId={}, userId={}", session.getSessionId(), session.getUserId());

        // 从 token 中获取用户 ID
        Integer tokenUserId = jwtUtil.getUserIdFromToken(token);
        log.info("从Token中获取的用户ID: {}", tokenUserId);

        if (tokenUserId == null) {
            log.warn("令牌中缺少用户信息");
            sendErrorResponse(response, 50008, "令牌中缺少用户信息");
            return false;
        }

        // 获取请求头中的用户 ID
        String headerUserId = request.getHeader("X-User-ID");
        log.info("请求头中的X-User-ID: {}", headerUserId);

        if (headerUserId != null) {
            try {
                Integer requestUserId = Integer.parseInt(headerUserId);
                // 验证 token 中的用户 ID 与请求头中的用户 ID 是否一致
                if (!tokenUserId.equals(requestUserId)) {
                    log.warn("用户身份不匹配: Token中={}, 请求头中={}", tokenUserId, requestUserId);
                    sendErrorResponse(response, 50012, "用户身份不匹配");
                    return false;
                }
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", headerUserId);
                sendErrorResponse(response, 50008, "用户ID格式错误");
                return false;
            }
        }

        // 将用户信息存储到请求属性中，供后续使用
        request.setAttribute("userId", tokenUserId);
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        request.setAttribute("userType", jwtUtil.getUserTypeFromToken(token));

        log.info("=== JWT拦截器通过 === 用户ID: {}", tokenUserId);
        return true;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", null);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));

        log.info("发送401错误响应: code={}, message={}", code, message);
    }
}
