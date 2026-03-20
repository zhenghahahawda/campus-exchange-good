package com.campus.exchange.service;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 直接从业务表（goods/orders/users）查询生成通知
 */
public interface NotificationService {

    /**
     * 获取通知列表（聚合待审核商品、新订单、新用户）
     */
    List<Map<String, Object>> getNotifications(int limit);

    /**
     * 获取未读通知总数（待审核商品数 + 待处理订单数 + 今日新注册用户数）
     */
    int getUnreadCount();
}
