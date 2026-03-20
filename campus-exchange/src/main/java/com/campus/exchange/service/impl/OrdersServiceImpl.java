package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.exchange.entity.ExchangeProof;
import com.campus.exchange.entity.Orders;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.ExchangeProofMapper;
import com.campus.exchange.mapper.OrdersMapper;
import com.campus.exchange.service.OrdersService;
import com.campus.exchange.util.RandomCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 订单服务实现类
 */
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ExchangeProofMapper exchangeProofMapper;

    @Override
    public List<Orders> getAllOrders() {
        return ordersMapper.selectList(null);
    }

    @Override
    public Orders getOrderById(Long id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    public List<Orders> getOrdersByStatus(String status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getStatus, status);
        return ordersMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Orders createOrder(Orders order) {
        // 生成订单号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus("pending");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.insert(order);
        return order;
    }

    @Override
    @Transactional
    public Orders updateOrder(Long id, Orders order) {
        Orders existingOrder = getOrderById(id);

        if (order.getRemark() != null) {
            existingOrder.setRemark(order.getRemark());
        }
        if (order.getStatus() != null) {
            existingOrder.setStatus(order.getStatus());
        }

        existingOrder.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(existingOrder);
        return existingOrder;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        ordersMapper.deleteById(id);
    }

    /** 合法的订单状态 */
    private static final Set<String> VALID_STATUSES = new HashSet<>(Arrays.asList(
            "pending", "processing", "completed", "cancelled"
    ));

    @Override
    @Transactional
    public Orders updateOrderStatus(Long id, String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("无效的订单状态: " + status);
        }
        Orders order = getOrderById(id);

        // 校验状态转换合法性
        String currentStatus = order.getStatus();
        if ("completed".equals(currentStatus) || "cancelled".equals(currentStatus)) {
            throw new BusinessException("订单已" + ("completed".equals(currentStatus) ? "完成" : "取消") + "，无法修改状态");
        }

        order.setStatus(status);

        if ("completed".equals(status)) {
            order.setCompleteTime(LocalDateTime.now());
            order.setInitiatorConfirmed(1);
            order.setReceiverConfirmed(1);
        } else if ("cancelled".equals(status)) {
            order.setCancelTime(LocalDateTime.now());
        }

        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return order;
    }

    @Override
    public List<Orders> getOrdersByUserId(Long userId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getBuyerId, userId)
                .or()
                .eq(Orders::getSellerId, userId);
        wrapper.orderByDesc(Orders::getCreateTime);
        return ordersMapper.selectList(wrapper);
    }

    @Override
    public List<Orders> getOrdersByUserIdAndStatus(Long userId, String status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Orders::getBuyerId, userId).or().eq(Orders::getSellerId, userId))
                .eq(Orders::getStatus, status);
        wrapper.orderByDesc(Orders::getCreateTime);
        return ordersMapper.selectList(wrapper);
    }

    /**
     * 生成订单号
     * 格式：ORD-{时间戳}-{随机码}
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = RandomCodeUtil.generate(4);
        return String.format("ORD-%s-%s", timestamp, randomCode);
    }

    @Override
    @Transactional
    public ExchangeProof uploadProof(Long orderId, Integer userId, String images, String comment) {
        Orders order = getOrderById(orderId);

        // 判断用户角色
        String userRole;
        if (userId.longValue() == order.getBuyerId()) {
            userRole = "initiator";
        } else if (userId.longValue() == order.getSellerId()) {
            userRole = "receiver";
        } else {
            throw new BusinessException("您不是该订单的参与方");
        }

        // 查找是否已有凭证记录
        QueryWrapper<ExchangeProof> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).eq("user_id", userId);
        ExchangeProof proof = exchangeProofMapper.selectOne(wrapper);

        if (proof == null) {
            proof = new ExchangeProof();
            proof.setOrderId(orderId.intValue());
            proof.setUserId(userId);
            proof.setUserRole(userRole);
            proof.setImages(images);
            proof.setComment(comment);
            proof.setStatus(1);
            proof.setUploadedAt(LocalDateTime.now());
            proof.setCreatedAt(LocalDateTime.now());
            proof.setUpdatedAt(LocalDateTime.now());
            exchangeProofMapper.insert(proof);
        } else {
            proof.setImages(images);
            proof.setComment(comment);
            proof.setStatus(1);
            proof.setUploadedAt(LocalDateTime.now());
            proof.setUpdatedAt(LocalDateTime.now());
            exchangeProofMapper.updateById(proof);
        }

        // 更新订单确认状态
        if ("initiator".equals(userRole)) {
            order.setInitiatorConfirmed(1);
        } else {
            order.setReceiverConfirmed(1);
        }
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        return proof;
    }
}
