package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.UserLoginLog;
import com.campus.exchange.service.LoginLogService;
import com.campus.exchange.util.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志控制器
 */
@RestController
@RequestMapping("/api/login-logs")
@Tag(name = "登录日志管理")
public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    /**
     * 获取登录日志统计
     */
    @GetMapping("/stats")
    @RequireAdmin
    @Operation(summary = "获取登录日志统计", description = "获取登录日志的总数、成功数、失败数")
    public Result<java.util.Map<String, Long>> getStats() {
        try {
            return Result.success(loginLogService.getStats());
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询登录日志
     */
    @GetMapping("/page")
    @RequireAdmin
    @Operation(summary = "分页查询登录日志", description = "管理员查询所有登录日志（分页，默认20条/页）")
    public Result<PageResponse<UserLoginLog>> pageLoginLogs(PageRequest pageRequest) {
        try {
            // 设置默认每页20条
            if (pageRequest.getPageSize() == null || pageRequest.getPageSize() == 10) {
                pageRequest.setPageSize(20);
            }
            PageResponse<UserLoginLog> page = loginLogService.pageLoginLogs(pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询登录日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID分页查询登录日志
     */
    @GetMapping("/user/{userId}/page")
    @RequireAdmin
    @Operation(summary = "根据用户ID分页查询登录日志", description = "管理员查询指定用户的登录日志（分页，默认20条/页）")
    public Result<PageResponse<UserLoginLog>> pageLoginLogsByUserId(
            @PathVariable Integer userId,
            PageRequest pageRequest) {
        try {
            // 设置默认每页20条
            if (pageRequest.getPageSize() == null || pageRequest.getPageSize() == 10) {
                pageRequest.setPageSize(20);
            }
            PageResponse<UserLoginLog> page = loginLogService.pageLoginLogsByUserId(userId, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询用户登录日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取登录日志详情
     */
    @GetMapping("/{logId}")
    @RequireAdmin
    @Operation(summary = "获取登录日志详情", description = "根据日志ID获取登录日志详细信息")
    public Result<UserLoginLog> getLoginLogById(@PathVariable Long logId) {
        try {
            UserLoginLog log = loginLogService.getLoginLogById(logId);
            if (log == null) {
                return Result.error("登录日志不存在");
            }
            return Result.success(log);
        } catch (Exception e) {
            return Result.error("获取登录日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取所有登录日志
     */
    @GetMapping("/user/{userId}")
    @RequireAdmin
    @Operation(summary = "根据用户ID获取所有登录日志", description = "管理员查询指定用户的所有登录日志")
    public Result<List<UserLoginLog>> getLoginLogsByUserId(@PathVariable Integer userId) {
        try {
            List<UserLoginLog> logs = loginLogService.getLoginLogsByUserId(userId);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.error("获取用户登录日志失败: " + e.getMessage());
        }
    }

    /**
     * 删除登录日志
     */
    @DeleteMapping("/{logId}")
    @RequireAdmin
    @Operation(summary = "删除登录日志", description = "管理员删除指定的登录日志")
    public Result<Void> deleteLoginLog(@PathVariable Long logId) {
        try {
            loginLogService.deleteLoginLog(logId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除登录日志失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除登录日志
     */
    @DeleteMapping("/batch")
    @RequireAdmin
    @Operation(summary = "批量删除登录日志", description = "管理员批量删除登录日志")
    public Result<Void> batchDeleteLoginLogs(@RequestBody List<Long> logIds) {
        try {
            loginLogService.batchDeleteLoginLogs(logIds);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("批量删除登录日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID删除所有登录日志
     */
    @DeleteMapping("/user/{userId}")
    @RequireAdmin
    @Operation(summary = "删除用户所有登录日志", description = "管理员删除指定用户的所有登录日志")
    public Result<Void> deleteLoginLogsByUserId(@PathVariable Integer userId) {
        try {
            loginLogService.deleteLoginLogsByUserId(userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除用户登录日志失败: " + e.getMessage());
        }
    }
}
