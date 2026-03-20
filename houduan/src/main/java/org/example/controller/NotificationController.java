package org.example.controller;

import org.example.common.Result;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public Result<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return notificationService.getUserNotifications(Integer.valueOf(userId), page, limit);
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return notificationService.getUnreadCount(Integer.valueOf(userId));
    }

    @PutMapping("/{id}/read")
    public Result<String> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return notificationService.markAsRead(id, Integer.valueOf(userId));
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return notificationService.markAllAsRead(Integer.valueOf(userId));
    }
}
