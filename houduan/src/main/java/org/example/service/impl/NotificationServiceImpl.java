package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.Result;
import org.example.entity.Notification;
import org.example.mapper.NotificationMapper;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public Result<Map<String, Object>> getUserNotifications(Integer userId, Integer page, Integer limit) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        Page<Notification> pageParam = new Page<>(page, limit);
        IPage<Notification> resultPage = notificationMapper.selectPage(pageParam, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("limit", limit);

        return Result.success(result);
    }

    @Override
    public Result<Integer> getUnreadCount(Integer userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        Long count = notificationMapper.selectCount(wrapper);
        return Result.success(count.intValue());
    }

    @Override
    public Result<String> markAsRead(Long notificationId, Integer userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            return Result.error(40401, "Notification not found");
        }

        if (!notification.getUserId().equals(userId)) {
            return Result.error(40301, "Forbidden");
        }

        notification.setIsRead(1);
        notificationMapper.updateById(notification);

        return Result.success("Marked as read", null);
    }

    @Override
    public Result<String> markAllAsRead(Integer userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        for (Notification notification : notifications) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }

        return Result.success("All marked as read", null);
    }

    @Override
    public void createNotification(Integer userId, String type, String title, String content, String relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);

        notificationMapper.insert(notification);
    }
}
