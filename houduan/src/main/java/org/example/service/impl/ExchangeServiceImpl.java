package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.Result;
import org.example.dto.ExchangeDetailResponse;
import org.example.entity.Exchange;
import org.example.entity.Good;
import org.example.entity.User;
import org.example.mapper.ExchangeMapper;
import org.example.mapper.GoodMapper;
import org.example.mapper.UserMapper;
import org.example.service.ChatService;
import org.example.service.ExchangeService;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Result<List<Exchange>> getUserExchanges(Integer userId, String status, String type) {
        LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();

        if ("sent".equals(type)) {
            wrapper.eq(Exchange::getInitiatorId, userId);
        } else if ("received".equals(type)) {
            wrapper.eq(Exchange::getReceiverId, userId);
        } else {
            wrapper.and(w -> w.eq(Exchange::getInitiatorId, userId)
                    .or().eq(Exchange::getReceiverId, userId));
        }

        if (StringUtils.hasText(status)) {
            wrapper.eq(Exchange::getStatus, status);
        }

        wrapper.orderByDesc(Exchange::getCreatedAt);
        List<Exchange> exchanges = exchangeMapper.selectList(wrapper);

        System.out.println("=== getUserExchanges 调试信息 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("状态筛选: " + status);
        System.out.println("类型筛选: " + type);
        System.out.println("查询到的交换记录数量: " + exchanges.size());
        for (Exchange exchange : exchanges) {
            System.out.println("交换记录: ID=" + exchange.getId() + 
                             ", OrderId=" + exchange.getOrderId() +
                             ", 发起方=" + exchange.getRequesterId() + 
                             ", 接收方=" + exchange.getSellerId() + 
                             ", 商品ID=" + exchange.getGoodsId() +
                             ", 状态=" + exchange.getStatus() +
                             ", 消息=" + exchange.getMessage());
        }

        return Result.success(exchanges);
    }

    @Override
    public Result<String> createExchange(Integer requesterId, Integer goodsId, String message, List<String> offerItems) {
        try {
            Good good = goodMapper.selectById(goodsId);
            if (good == null) {
                return Result.error(40401, "Good not found");
            }

            if (good.getUserId().equals(requesterId)) {
                return Result.error(40001, "Cannot exchange with yourself");
            }

            // 查询发起方的第一个商品作为交换商品
            LambdaQueryWrapper<Good> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Good::getUserId, requesterId)
                   .eq(Good::getStatus, 1) // 已通过审核
                   .eq(Good::getShelfStatus, 1) // 已上架
                   .orderByAsc(Good::getCreatedAt)
                   .last("LIMIT 1");
            
            Good initiatorGood = goodMapper.selectOne(wrapper);
            if (initiatorGood == null) {
                return Result.error(40002, "You need to have at least one published good to make an exchange request");
            }

            Exchange exchange = new Exchange();
            // 手动设置订单号
            exchange.setOrderNumber("ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
            exchange.setStatus("pending");
            exchange.setInitiatorId(requesterId);
            // 使用发起方的第一个商品
            exchange.setInitiatorGoodId(initiatorGood.getGoodId());
            exchange.setReceiverId(good.getUserId());
            // receiver_good_id 设置为发起方想要的商品ID
            exchange.setReceiverGoodId(goodsId);
            exchange.setRemark(message); // 将message存储在remark字段中
            exchange.setCreatedAt(java.time.LocalDateTime.now());
            exchange.setUpdatedAt(java.time.LocalDateTime.now());

            System.out.println("准备插入Exchange: " + exchange);
            System.out.println("OrderNumber: " + exchange.getOrderNumber());
            System.out.println("Status: " + exchange.getStatus());
            System.out.println("InitiatorId: " + exchange.getInitiatorId());
            
            // 使用自定义的插入方法
            exchangeMapper.insertExchange(exchange);
            
            System.out.println("插入成功，生成的ID: " + exchange.getOrderId());

            // 创建或加入该商品的群聊（发起方 + 接收方）
            chatService.createGroupConversation(
                requesterId, good.getUserId(),
                String.valueOf(exchange.getOrderId()), exchange.getOrderNumber(),
                goodsId
            );

            // 通知卖家收到交换申请
            User requester = userMapper.selectById(requesterId);
            String requesterName = requester != null ? requester.getUsername() : "有人";
            notificationService.createNotification(
                good.getUserId(), "exchange_request",
                "收到新的交换申请",
                requesterName + " 想用商品与你交换「" + good.getGoodName() + "」",
                String.valueOf(exchange.getOrderId()), "exchange"
            );

            return Result.success("Exchange request created", String.valueOf(exchange.getOrderId()));
        } catch (Exception e) {
            System.err.println("创建交换申请失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(50001, "Failed to create exchange: " + e.getMessage());
        }
    }

    @Override
    public Result<String> updateExchangeStatus(String exchangeId, String status, Integer userId) {
        Exchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            return Result.error(40401, "Exchange not found");
        }

        if (!exchange.getReceiverId().equals(userId) && !exchange.getInitiatorId().equals(userId)) {
            return Result.error(40301, "Forbidden");
        }

        exchange.setStatus(status);
        exchangeMapper.updateById(exchange);

        // Update good status if exchange is completed
        if ("completed".equals(status)) {
            Good good = goodMapper.selectById(exchange.getInitiatorGoodId());
            if (good != null) {
                good.setStatus(1); // 1=已通过/已完成
                good.setShelfStatus(0); // 0=已下架
                goodMapper.updateById(good);
            }
        }

        return Result.success("Exchange status updated", null);
    }

    @Override
    public Result<List<ExchangeDetailResponse>> getUserExchangesWithDetails(Integer userId, String status, String type) {
        LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
        if ("sent".equals(type)) {
            wrapper.eq(Exchange::getInitiatorId, userId);
        } else if ("received".equals(type)) {
            wrapper.eq(Exchange::getReceiverId, userId);
        } else {
            wrapper.and(w -> w.eq(Exchange::getInitiatorId, userId).or().eq(Exchange::getReceiverId, userId));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Exchange::getStatus, status);
        }
        wrapper.orderByDesc(Exchange::getCreatedAt);
        List<Exchange> exchanges = exchangeMapper.selectList(wrapper);

        List<ExchangeDetailResponse> result = new ArrayList<>();
        for (Exchange exchange : exchanges) {
            ExchangeDetailResponse detail = buildDetailResponse(exchange, userId);
            result.add(detail);
        }
        return Result.success(result);
    }

    @Override
    public Result<String> acceptExchange(String exchangeId, Integer userId) {
        Exchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            return Result.error(40401, "Exchange not found");
        }
        if (!exchange.getReceiverId().equals(userId)) {
            return Result.error(40301, "Only the receiver can accept the exchange");
        }
        if (!"pending".equals(exchange.getStatus())) {
            return Result.error(40001, "Exchange is not in pending status");
        }
        exchange.setStatus("to_process");
        exchange.setUpdatedAt(LocalDateTime.now());
        exchangeMapper.updateById(exchange);
        // 通知发起方申请被接受
        notificationService.createNotification(
            exchange.getInitiatorId(), "exchange_accepted",
            "交换申请已被接受",
            "你的交换申请已被对方接受，请尽快完成交换",
            String.valueOf(exchange.getOrderId()), "exchange"
        );
        return Result.success("Exchange accepted", null);
    }

    @Override
    public Result<String> rejectExchange(String exchangeId, Integer userId, String reason) {
        Exchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            return Result.error(40401, "Exchange not found");
        }
        if (!exchange.getReceiverId().equals(userId)) {
            return Result.error(40301, "Only the receiver can reject the exchange");
        }
        if (!"pending".equals(exchange.getStatus())) {
            return Result.error(40001, "Exchange is not in pending status");
        }
        exchange.setStatus("cancelled");
        exchange.setCancelReason(reason);
        exchange.setCancelledAt(LocalDateTime.now());
        exchange.setUpdatedAt(LocalDateTime.now());
        exchangeMapper.updateById(exchange);
        // 通知发起方申请被拒绝
        notificationService.createNotification(
            exchange.getInitiatorId(), "exchange_rejected",
            "交换申请被拒绝",
            "你的交换申请已被对方拒绝" + (reason != null ? "：" + reason : ""),
            String.valueOf(exchange.getOrderId()), "exchange"
        );
        return Result.success("Exchange rejected", null);
    }

    private ExchangeDetailResponse buildDetailResponse(Exchange exchange, Integer currentUserId) {
        ExchangeDetailResponse detail = new ExchangeDetailResponse();
        detail.setId(exchange.getId());
        detail.setOrderId(exchange.getOrderId());
        detail.setOrderNumber(exchange.getOrderNumber());
        detail.setStatus(exchange.getStatus());
        detail.setInitiatorId(exchange.getInitiatorId());
        detail.setReceiverId(exchange.getReceiverId());
        detail.setInitiatorGoodId(exchange.getInitiatorGoodId());
        detail.setReceiverGoodId(exchange.getReceiverGoodId());
        detail.setMessage(exchange.getMessage());
        detail.setRemark(exchange.getRemark());
        detail.setCreatedAt(exchange.getCreatedAt());
        detail.setUpdatedAt(exchange.getUpdatedAt());
        detail.setUserRole(exchange.getInitiatorId().equals(currentUserId) ? "initiator" : "receiver");
        detail.setCompatibleFields();

        // 填充发起方信息
        User initiator = userMapper.selectById(exchange.getInitiatorId());
        if (initiator != null) {
            detail.setInitiatorName(initiator.getUsername());
            detail.setInitiatorAvatar(initiator.getAvatar());
        }
        // 填充接收方信息
        User receiver = userMapper.selectById(exchange.getReceiverId());
        if (receiver != null) {
            detail.setReceiverName(receiver.getUsername());
            detail.setReceiverAvatar(receiver.getAvatar());
        }
        // 填充商品信息
        if (exchange.getInitiatorGoodId() != null) {
            Good iGood = goodMapper.selectById(exchange.getInitiatorGoodId());
            if (iGood != null) {
                detail.setInitiatorGoodName(iGood.getGoodName());
                if (iGood.getImages() != null && !iGood.getImages().isEmpty()) {
                    detail.setInitiatorGoodImage(iGood.getImages().get(0));
                }
            }
        }
        if (exchange.getReceiverGoodId() != null) {
            Good rGood = goodMapper.selectById(exchange.getReceiverGoodId());
            if (rGood != null) {
                detail.setReceiverGoodName(rGood.getGoodName());
                if (rGood.getImages() != null && !rGood.getImages().isEmpty()) {
                    detail.setReceiverGoodImage(rGood.getImages().get(0));
                }
            }
        }
        return detail;
    }

    @Override
    public Result<String> submitExchangeCode(String exchangeId, Integer userId, String exchangeCode) {
        Exchange exchange = exchangeMapper.selectById(exchangeId);
        if (exchange == null) {
            return Result.error(40401, "Exchange not found");
        }

        // 只有 to_process 或 processing 状态才能提交凭证码
        if (!"to_process".equals(exchange.getStatus()) && !"processing".equals(exchange.getStatus())) {
            return Result.error(40001, "订单状态不允许提交凭证码");
        }

        boolean isInitiator = exchange.getInitiatorId().equals(userId);
        boolean isReceiver = exchange.getReceiverId().equals(userId);
        if (!isInitiator && !isReceiver) {
            return Result.error(40301, "无权操作此订单");
        }

        // 验证凭证码：发起方提交对方商品的凭证码，接收方提交对方商品的凭证码
        // 发起方需要提交 receiver_good 的 exchange_code（证明收到了对方的商品）
        // 接收方需要提交 initiator_good 的 exchange_code（证明收到了对方的商品）
        Integer targetGoodId = isInitiator ? exchange.getReceiverGoodId() : exchange.getInitiatorGoodId();
        Good targetGood = goodMapper.selectById(targetGoodId);
        if (targetGood == null || !exchangeCode.equals(targetGood.getExchangeCode())) {
            return Result.error(40001, "凭证码不正确");
        }

        // 标记已确认
        if (isInitiator) {
            exchange.setInitiatorConfirmed(1);
        } else {
            exchange.setReceiverConfirmed(1);
        }

        // 更新状态为 processing
        if (!"processing".equals(exchange.getStatus())) {
            exchange.setStatus("processing");
        }

        // 双方都确认后，自动完成订单
        if (Integer.valueOf(1).equals(exchange.getInitiatorConfirmed())
                && Integer.valueOf(1).equals(exchange.getReceiverConfirmed())) {
            exchange.setStatus("completed");
            exchange.setCompletedAt(LocalDateTime.now());

            // 将双方商品标记为已售出（shelf_status=2）
            Good initiatorGood = goodMapper.selectById(exchange.getInitiatorGoodId());
            if (initiatorGood != null) {
                initiatorGood.setShelfStatus(2);
                initiatorGood.setSoldAt(LocalDateTime.now());
                goodMapper.updateById(initiatorGood);
            }
            Good receiverGood = goodMapper.selectById(exchange.getReceiverGoodId());
            if (receiverGood != null) {
                receiverGood.setShelfStatus(2);
                receiverGood.setSoldAt(LocalDateTime.now());
                goodMapper.updateById(receiverGood);
            }

            exchange.setUpdatedAt(LocalDateTime.now());
            exchangeMapper.updateById(exchange);
            // 通知双方交换完成
            notificationService.createNotification(
                exchange.getInitiatorId(), "exchange_completed",
                "交换已完成", "恭喜！你的交换订单已成功完成",
                String.valueOf(exchange.getOrderId()), "exchange"
            );
            notificationService.createNotification(
                exchange.getReceiverId(), "exchange_completed",
                "交换已完成", "恭喜！你的交换订单已成功完成",
                String.valueOf(exchange.getOrderId()), "exchange"
            );
            return Result.success("交换完成，订单已关闭", "completed");
        }

        exchange.setUpdatedAt(LocalDateTime.now());
        exchangeMapper.updateById(exchange);
        return Result.success("凭证码验证成功，等待对方确认", "processing");
    }

    @Override
    public Result<Exchange> getExchangeByConversationId(String conversationId) {
        try {
            System.out.println("查询conversation_id: " + conversationId);
            
            LambdaQueryWrapper<Exchange> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Exchange::getConversationId, conversationId)
                    .orderByDesc(Exchange::getCreatedAt)
                    .last("LIMIT 1");
            
            Exchange exchange = exchangeMapper.selectOne(wrapper);
            
            System.out.println("查询结果: " + (exchange != null ? exchange.getId() : "null"));
            
            if (exchange == null) {
                // 返回空数据而不是报错，避免前端误判为需要跳转登录
                return Result.success(null);
            }
            
            return Result.success(exchange);
        } catch (Exception e) {
            System.err.println("查询交换信息失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(50001, "Failed to query exchange: " + e.getMessage());
        }
    }
}
