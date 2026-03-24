package com.campus.exchange.controller;

import com.campus.exchange.dto.LoginResponse;
import com.campus.exchange.dto.SessionVerifyResponse;
import com.campus.exchange.entity.User;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.TokenBlacklistService;
import com.campus.exchange.service.UserService;
import com.campus.exchange.service.UserSessionService;
import com.campus.exchange.util.JwtUtil;
import com.campus.exchange.vo.BaseResult;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "认证管理")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserSessionService userSessionService;

    /**
     * 验证会话
     */
    @GetMapping("/verify-session")
    @Operation(summary = "验证会话", description = "验证当前用户会话是否有效")
    public BaseResult<SessionVerifyResponse> verifySession(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            SessionVerifyResponse response = userService.verifySession(userId, username);
            return BaseResult.ok("验证成功", response);
        } catch (Exception e) {
            return BaseResult.error(e.getMessage());
        }
    }

    /**
     * 使用刷新令牌获取新的访问令牌
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用refresh_token获取新的access_token")
    public BaseResult<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return BaseResult.error("refreshToken不能为空");
            }

            if (tokenBlacklistService.isBlacklisted(refreshToken)) {
                return BaseResult.error("会话已失效，请重新登录");
            }

            // 验证刷新令牌
            Claims claims = jwtUtil.validateRefreshToken(refreshToken);
            if (claims == null) {
                return BaseResult.error("刷新令牌无效或已过期");
            }

            Integer userId = (Integer) claims.get("userId");
            String username = claims.getSubject();

            var session = userSessionService.getSessionByRefreshToken(refreshToken);
            if (session == null || session.getIsActive() == null || session.getIsActive() != 1) {
                return BaseResult.error("会话已失效，请重新登录");
            }
            if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())) {
                return BaseResult.error("会话已过期，请重新登录");
            }
            if (!userId.equals(session.getUserId())) {
                return BaseResult.error("会话用户不匹配，请重新登录");
            }

            // 查询用户最新信息（确保用户仍然有效）
            User user = userMapper.selectById(userId);
            if (user == null || user.getDeletedAt() != null) {
                return BaseResult.error("用户不存在");
            }

            // 生成新的令牌对
            String newAccessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), user.getUserType());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

            LoginResponse response = LoginResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .userType(user.getUserType())
                    .status(user.getStatus())
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtUtil.getAccessTokenExpiration())
                    .build();

            // 更新会话记录中的令牌
            boolean updated = userSessionService.updateTokenByRefreshToken(
                    refreshToken, newAccessToken, newRefreshToken,
                    LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenExpiration()));
            if (!updated) {
                return BaseResult.error("会话已失效，请重新登录");
            }

            return BaseResult.ok("刷新成功", response);
        } catch (Exception e) {
            return BaseResult.error("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录（将当前 Token 加入黑名单）
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "将当前Token加入黑名单使其失效")
    public BaseResult<Void> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                long remaining = jwtUtil.getRemainingExpiration(token);
                if (remaining > 0) {
                    tokenBlacklistService.addToBlacklist(token, remaining);
                }
                // 使对应的会话记录失效
                userSessionService.deactivateByToken(token);
            }
            return BaseResult.ok("退出登录成功", null);
        } catch (Exception e) {
            return BaseResult.ok("退出登录成功", null);
        }
    }
}
