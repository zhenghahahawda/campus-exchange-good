package org.example.controller;

import org.example.common.Result;
import org.example.dto.LoginResponse;
import org.example.entity.User;
import org.example.service.UserService;
import org.example.service.UserSessionService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserSessionService userSessionService;

    /**
     * 诊断接口：查询当前 token 对应的 session 状态（调试用）
     */
    @GetMapping("/debug/session")
    public Result<Map<String, Object>> debugSession(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Map<String, Object> info = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            info.put("error", "no token");
            return Result.success("debug", info);
        }
        String token = authHeader.substring(7);
        info.put("tokenSuffix", "..." + token.substring(Math.max(0, token.length() - 20)));
        try {
            info.put("tokenExpired", jwtUtil.isTokenExpired(token));
            info.put("tokenType", jwtUtil.extractTokenType(token));
            info.put("userId", jwtUtil.extractUserId(token));
        } catch (Exception e) {
            info.put("tokenParseError", e.getMessage());
        }
        try {
            org.example.entity.UserSession session = userSessionService.findByToken(token);
            if (session == null) {
                info.put("session", "NOT FOUND in Redis or DB");
            } else {
                info.put("sessionId", session.getSessionId());
                info.put("isActive", session.getIsActive());
                info.put("expiresAt", session.getExpiresAt());
                info.put("lastActivityAt", session.getLastActivityAt());
            }
        } catch (Exception e) {
            info.put("sessionError", e.getMessage());
        }
        return Result.success("debug", info);
    }
    @GetMapping("/verify")
    public Result<Map<String, Object>> verifyToken(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        Integer userType = (Integer) request.getAttribute("userType");

        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("userType", userType);
        data.put("role", userType == 1 ? "admin" : "user");
        data.put("valid", true);

        return Result.success("Token is valid", data);
    }

    /**
     * 刷新access token
     */
    @PostMapping("/refresh-token")
    public Result<LoginResponse> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error(40001, "Refresh token is required");
        }

        try {
            // Validate refresh token
            String userId = jwtUtil.extractUserId(refreshToken);
            String username = jwtUtil.extractUsername(refreshToken);
            String tokenType = jwtUtil.extractTokenType(refreshToken);

            if (!"refresh".equals(tokenType)) {
                return Result.error(40001, "Invalid token type");
            }

            if (jwtUtil.isTokenExpired(refreshToken)) {
                return Result.error(40101, "Refresh token has expired");
            }

            if (!jwtUtil.validateToken(refreshToken, username)) {
                return Result.error(40101, "Invalid refresh token");
            }

            // Get user info
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error(40401, "User not found");
            }

            // Generate new tokens
            String newAccessToken = jwtUtil.generateToken(userId, username, user.getUserType());
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

            // 用旧 access token 找到 session 并更新为新 token
            // 注意：session 里存的是 access token，不是 refresh token
            // 先通过 userId 找到活跃 session，再更新
            userSessionService.refreshSessionByUserId(Integer.valueOf(userId), newAccessToken, newRefreshToken);

            LoginResponse response = new LoginResponse();
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setUserType(user.getUserType());
            response.setStatus(user.getStatus());
            response.setRole(user.getUserType() == 1 ? "admin" : "user");
            response.setToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setAvatar(user.getAvatar());
            response.setSchool(user.getSchool());

            return Result.success("Token refreshed successfully", response);

        } catch (Exception e) {
            return Result.error(40101, "Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * 登出（客户端应删除token）
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }

        // 从请求头获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 使会话失效
            userSessionService.invalidateSession(token);
        }
        
        return Result.success("Logout successful", null);
    }

    /**
     * 忘记密码 - 凭账号直接重置密码
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> body) {
        String account = body.get("account"); // 邮箱或手机号
        String newPassword = body.get("newPassword");

        if (account == null || account.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Result.error(40001, "账号和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error(40001, "密码长度不能少于6位");
        }
        return userService.resetPassword(account, newPassword);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error(40001, "Old password and new password are required");
        }

        // TODO: Implement password change logic in UserService
        
        return Result.success("Password changed successfully", null);
    }
}
