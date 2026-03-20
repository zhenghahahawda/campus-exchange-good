package org.example.service;

import org.example.common.Result;
import org.example.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    Result<Map<String, Object>> getUserNotifications(Integer userId, Integer page, Integer limit);

    Result<Integer> getUnreadCount(Integer userId);

    Result<String> markAsRead(Long notificationId, Integer userId);

    Result<String> markAllAsRead(Integer userId);

    void createNotification(Integer userId, String type, String title, String content, String relatedId, String relatedType);
}
