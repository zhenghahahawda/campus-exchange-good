package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.UserSession;
import com.campus.exchange.service.UserSessionService;
import com.campus.exchange.util.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户会话控制器
 */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "用户会话管理")
public class UserSessionController {

    @Autowired
    private UserSessionService sessionService;

    /**
     * 获取会话统计
     */
    @GetMapping("/stats")
    @RequireAdmin
    @Operation(summary = "获取会话统计", description = "获取会话的总数、活跃数、失效数")
    public Result<java.util.Map<String, Long>> getStats() {
        try {
            return Result.success(sessionService.getStats());
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 创建会话
     */
    @PostMapping
    @RequireAdmin
    @Operation(summary = "创建会话", description = "管理员创建新的用户会话")
    public Result<UserSession> createSession(@RequestBody UserSession session) {
        try {
            UserSession created = sessionService.createSession(session);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据会话ID获取会话
     */
    @GetMapping("/{sessionId}")
    @RequireAdmin
    @Operation(summary = "获取会话详情", description = "根据会话ID获取会话详细信息")
    public Result<UserSession> getSessionById(@PathVariable String sessionId) {
        try {
            UserSession session = sessionService.getSessionById(sessionId);
            if (session == null) {
                return Result.error("会话不存在");
            }
            return Result.success(session);
        } catch (Exception e) {
            return Result.error("获取会话详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据Token获取会话
     */
    @GetMapping("/token/{token}")
    @RequireAdmin
    @Operation(summary = "根据Token获取会话", description = "通过访问令牌查询会话信息")
    public Result<UserSession> getSessionByToken(@PathVariable String token) {
        try {
            UserSession session = sessionService.getSessionByToken(token);
            if (session == null) {
                return Result.error("会话不存在");
            }
            return Result.success(session);
        } catch (Exception e) {
            return Result.error("获取会话失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有会话
     */
    @GetMapping("/page")
    @RequireAdmin
    @Operation(summary = "分页查询所有会话", description = "管理员分页查询所有用户会话")
    public Result<PageResponse<UserSession>> pageSessions(PageRequest pageRequest) {
        try {
            PageResponse<UserSession> page = sessionService.pageSessions(pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取所有会话
     */
    @GetMapping("/user/{userId}")
    @RequireAdmin
    @Operation(summary = "获取用户所有会话", description = "管理员查询指定用户的所有会话")
    public Result<List<UserSession>> getSessionsByUserId(@PathVariable Integer userId) {
        try {
            List<UserSession> sessions = sessionService.getSessionsByUserId(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取用户会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取活跃会话
     */
    @GetMapping("/user/{userId}/active")
    @RequireAdmin
    @Operation(summary = "获取用户活跃会话", description = "管理员查询指定用户的所有活跃会话")
    public Result<List<UserSession>> getActiveSessionsByUserId(@PathVariable Integer userId) {
        try {
            List<UserSession> sessions = sessionService.getActiveSessionsByUserId(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取用户活跃会话失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID分页查询会话
     */
    @GetMapping("/user/{userId}/page")
    @RequireAdmin
    @Operation(summary = "根据用户ID分页查询会话", description = "管理员分页查询指定用户的会话")
    public Result<PageResponse<UserSession>> pageSessionsByUserId(
            @PathVariable Integer userId,
            PageRequest pageRequest) {
        try {
            PageResponse<UserSession> page = sessionService.pageSessionsByUserId(userId, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询用户会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的所有会话
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的会话", description = "用户查询自己的所有会话")
    public Result<List<UserSession>> getMySessions(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录或Token无效");
            }
            List<UserSession> sessions = sessionService.getSessionsByUserId(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户的活跃会话
     */
    @GetMapping("/my/active")
    @Operation(summary = "获取我的活跃会话", description = "用户查询自己的所有活跃会话")
    public Result<List<UserSession>> getMyActiveSessions(HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录或Token无效");
            }
            List<UserSession> sessions = sessionService.getActiveSessionsByUserId(userId);
            return Result.success(sessions);
        } catch (Exception e) {
            return Result.error("获取活跃会话失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话
     */
    @PutMapping("/{sessionId}")
    @RequireAdmin
    @Operation(summary = "更新会话", description = "管理员更新会话信息")
    public Result<UserSession> updateSession(
            @PathVariable String sessionId,
            @RequestBody UserSession session) {
        try {
            UserSession updated = sessionService.updateSession(sessionId, session);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新会话失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    @RequireAdmin
    @Operation(summary = "删除会话", description = "管理员删除指定会话")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        try {
            sessionService.deleteSession(sessionId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除会话
     */
    @DeleteMapping("/batch")
    @RequireAdmin
    @Operation(summary = "批量删除会话", description = "管理员批量删除会话")
    public Result<Void> batchDeleteSessions(@RequestBody List<String> sessionIds) {
        try {
            sessionService.batchDeleteSessions(sessionIds);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("批量删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户所有会话
     */
    @DeleteMapping("/user/{userId}")
    @RequireAdmin
    @Operation(summary = "删除用户所有会话", description = "管理员删除指定用户的所有会话")
    public Result<Void> deleteSessionsByUserId(@PathVariable Integer userId) {
        try {
            sessionService.deleteSessionsByUserId(userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除用户会话失败: " + e.getMessage());
        }
    }

    /**
     * 使会话失效
     */
    @PostMapping("/{sessionId}/deactivate")
    @RequireAdmin
    @Operation(summary = "使会话失效", description = "管理员使指定会话失效")
    public Result<Void> deactivateSession(@PathVariable String sessionId) {
        try {
            sessionService.deactivateSession(sessionId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("使会话失效失败: " + e.getMessage());
        }
    }

    /**
     * 使用户所有会话失效
     */
    @PostMapping("/user/{userId}/deactivate")
    @RequireAdmin
    @Operation(summary = "使用户所有会话失效", description = "管理员使指定用户的所有会话失效")
    public Result<Void> deactivateSessionsByUserId(@PathVariable Integer userId) {
        try {
            sessionService.deactivateSessionsByUserId(userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("使用户会话失效失败: " + e.getMessage());
        }
    }

    /**
     * 批量使会话失效（一键下线）
     */
    @PostMapping("/batch/deactivate")
    @RequireAdmin
    @Operation(summary = "批量使会话失效", description = "管理员批量使选中的会话失效（一键下线）")
    public Result<Void> batchDeactivateSessions(@RequestBody List<String> sessionIds) {
        try {
            sessionService.batchDeactivateSessions(sessionIds);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("批量下线失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期会话
     */
    @PostMapping("/clean-expired")
    @RequireAdmin
    @Operation(summary = "清理过期会话", description = "管理员清理所有过期的会话")
    public Result<Integer> cleanExpiredSessions() {
        try {
            int count = sessionService.cleanExpiredSessions();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("清理过期会话失败: " + e.getMessage());
        }
    }
}
