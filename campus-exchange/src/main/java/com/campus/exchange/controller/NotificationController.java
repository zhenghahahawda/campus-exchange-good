package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员通知控制器
 * 直接从业务表聚合通知数据
 */
@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping
    @RequireAdmin
    public Result<List<Map<String, Object>>> getNotifications(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> list = notificationService.getNotifications(limit);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取通知列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @RequireAdmin
    public Result<Integer> getUnreadCount() {
        try {
            int count = notificationService.getUnreadCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取未读数量失败: " + e.getMessage());
        }
    }
}
