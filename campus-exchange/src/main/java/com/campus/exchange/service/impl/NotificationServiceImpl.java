package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.entity.Orders;
import com.campus.exchange.entity.User;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.mapper.OrdersMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 通知服务实现类
 * 直接查询业务表生成通知，不依赖独立通知表
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final String UNREAD_COUNT_KEY = "notification:unread_count";
    private static final long UNREAD_COUNT_TTL_SECONDS = 30;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Map<String, Object>> getNotifications(int limit) {
        List<Map<String, Object>> notifications = new ArrayList<>();

        // 1. 待审核商品（status=0）
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("status", 0).orderByDesc("created_at").last("LIMIT " + limit);
        List<Goods> pendingGoods = goodsMapper.selectList(goodsWrapper);
        for (Goods goods : pendingGoods) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("type", "goods_audit");
            notif.put("title", "物品待审核");
            notif.put("description", "商品「" + goods.getName() + "」等待审核");
            notif.put("createdAt", goods.getCreateTime() != null ? goods.getCreateTime().format(FMT) : "");
            notif.put("targetPath", "/Back-Page/good");
            notifications.add(notif);
        }

        // 2. 待处理订单（status=pending 或 to_process）
        QueryWrapper<Orders> ordersWrapper = new QueryWrapper<>();
        ordersWrapper.in("status", Arrays.asList("pending", "to_process"))
                     .orderByDesc("created_at").last("LIMIT " + limit);
        List<Orders> pendingOrders = ordersMapper.selectList(ordersWrapper);
        for (Orders order : pendingOrders) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("type", "order_process");
            notif.put("title", "订单待处理");
            notif.put("description", "订单 " + order.getOrderNo() + " 需要处理");
            notif.put("createdAt", order.getCreateTime() != null ? order.getCreateTime().format(FMT) : "");
            notif.put("targetPath", "/Back-Page/order");
            notifications.add(notif);
        }

        // 3. 今日新注册用户
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.ge("created_at", todayStart).isNull("deleted_at")
                   .orderByDesc("created_at").last("LIMIT " + limit);
        List<User> newUsers = userMapper.selectList(userWrapper);
        for (User user : newUsers) {
            Map<String, Object> notif = new HashMap<>();
            notif.put("type", "user_register");
            notif.put("title", "新用户注册");
            notif.put("description", "用户「" + user.getUsername() + "」(ID: " + user.getUserId() + ") 完成注册");
            notif.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().format(FMT) : "");
            notif.put("targetPath", "/Back-Page/usersmage");
            notifications.add(notif);
        }

        // 按时间倒序排序
        notifications.sort((a, b) -> {
            String timeA = (String) a.getOrDefault("createdAt", "");
            String timeB = (String) b.getOrDefault("createdAt", "");
            return timeB.compareTo(timeA);
        });

        // 限制总数
        if (notifications.size() > limit) {
            notifications = notifications.subList(0, limit);
        }

        return notifications;
    }

    @Override
    public int getUnreadCount() {
        // 尝试从 Redis 读取缓存
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(UNREAD_COUNT_KEY);
                if (cached != null) {
                    return ((Number) cached).intValue();
                }
            } catch (Exception e) {
                log.warn("从Redis读取未读通知数失败: {}", e.getMessage());
            }
        }

        long count = 0;

        // 待审核商品数
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("status", 0);
        count += goodsMapper.selectCount(goodsWrapper);

        // 待处理订单数
        QueryWrapper<Orders> ordersWrapper = new QueryWrapper<>();
        ordersWrapper.in("status", Arrays.asList("pending", "to_process"));
        count += ordersMapper.selectCount(ordersWrapper);

        // 今日新注册用户数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.ge("created_at", todayStart).isNull("deleted_at");
        count += userMapper.selectCount(userWrapper);

        // 写入 Redis 缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(UNREAD_COUNT_KEY, count, UNREAD_COUNT_TTL_SECONDS, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("写入Redis未读通知数缓存失败: {}", e.getMessage());
            }
        }

        return (int) count;
    }
}