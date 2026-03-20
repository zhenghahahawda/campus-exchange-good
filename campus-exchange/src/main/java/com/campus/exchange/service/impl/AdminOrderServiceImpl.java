package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.ExchangeProof;
import com.campus.exchange.entity.Orders;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.ExchangeProofMapper;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.mapper.OrdersMapper;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.service.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final int DEFAULT_PAGE_SIZE = 4;
    private static final String CACHE_ALL_GOODS = "goods:all";

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ExchangeProofMapper exchangeProofMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private GoodsMapper goodsMapper;

    // ========== 订单基础管理 ==========

    @Override
    public PageResponse<Orders> pageOrders(PageRequest pageRequest, String status, String keyword) {
        int pageSize = (pageRequest.getPageSize() == null || pageRequest.getPageSize() == 10)
                ? DEFAULT_PAGE_SIZE : pageRequest.getPageSize();
        Page<Orders> page = new Page<>(pageRequest.getPageNum(), pageSize);

        // 使用自定义的分页查询方法，包含关联的用户和商品信息
        IPage<Orders> result = ordersMapper.selectOrdersWithDetails(page, status, keyword);

        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public Orders getOrderDetail(Long orderId) {
        Orders order = ordersMapper.selectOrderDetail(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 查询交换凭证
        QueryWrapper<ExchangeProof> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        order.setExchangeProofs(exchangeProofMapper.selectList(wrapper));
        return order;
    }

    @Override
    @Transactional
    public Orders updateOrderStatus(Long orderId, String status) {
        Orders order = getExistingOrder(orderId);
        order.setStatus(status);
        if ("completed".equals(status)) {
            order.setCompleteTime(LocalDateTime.now());
            // 订单完成时，双方都应该已上传凭证
            order.setInitiatorConfirmed(1);
            order.setReceiverConfirmed(1);
        } else if ("cancelled".equals(status)) {
            order.setCancelTime(LocalDateTime.now());
        }
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        // 订单完成时，将关联商品的上架状态改为已售出(2)
        if ("completed".equals(status)) {
            updateGoodsShelfStatus(order.getBuyerItemId(), 2);
            updateGoodsShelfStatus(order.getSellerItemId(), 2);
        }

        return order;
    }

    @Override
    @Transactional
    public Orders updateOrderRemark(Long orderId, String remark) {
        Orders order = getExistingOrder(orderId);
        order.setAdminNote(remark);
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Orders order = getExistingOrder(orderId);

        // 硬删除订单
        ordersMapper.deleteById(orderId);
    }

    // ========== 订单处理流程 ==========

    @Override
    @Transactional
    public Orders processOrder(Long orderId) {
        Orders order = getExistingOrder(orderId);
        if (!"to_process".equals(order.getStatus())) {
            throw new BusinessException("只有待处理状态的订单才能开始处理");
        }
        order.setStatus("processing");
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public Orders completeOrder(Long orderId) {
        Orders order = getExistingOrder(orderId);
        if (!"processing".equals(order.getStatus())) {
            throw new BusinessException("只有处理中的订单才能确认完成");
        }
        order.setStatus("completed");
        order.setCompleteTime(LocalDateTime.now());
        // 订单完成时，双方都应该已上传凭证
        order.setInitiatorConfirmed(1);
        order.setReceiverConfirmed(1);
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        // 订单完成时，将关联商品的上架状态改为已售出(2)
        updateGoodsShelfStatus(order.getBuyerItemId(), 2);
        updateGoodsShelfStatus(order.getSellerItemId(), 2);

        return order;
    }

    @Override
    @Transactional
    public Orders cancelOrder(Long orderId, Integer adminId, String cancelReason) {
        Orders order = getExistingOrder(orderId);
        if ("completed".equals(order.getStatus()) || "cancelled".equals(order.getStatus())) {
            throw new BusinessException("已完成或已取消的订单不能再取消");
        }
        order.setStatus("cancelled");
        order.setCancelReason(cancelReason);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        return order;
    }

    // ========== 交换凭证管理 ==========

    @Override
    public List<ExchangeProof> getOrderProofs(Long orderId) {
        QueryWrapper<ExchangeProof> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        wrapper.orderByAsc("user_role");
        return exchangeProofMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public ExchangeProof verifyProof(Integer proofId, Integer adminId) {
        ExchangeProof proof = exchangeProofMapper.selectById(proofId);
        if (proof == null) {
            throw new BusinessException("凭证不存在");
        }
        proof.setStatus(2); // 已验证
        proof.setVerifiedBy(adminId);
        proof.setVerifiedAt(LocalDateTime.now());
        proof.setUpdatedAt(LocalDateTime.now());
        exchangeProofMapper.updateById(proof);
        return proof;
    }

    // ========== 统计接口 ==========

    @Override
    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        String[] statuses = {"pending", "to_process", "processing", "completed", "cancelled"};
        long totalCount = 0;
        for (String s : statuses) {
            QueryWrapper<Orders> wrapper = new QueryWrapper<>();
            wrapper.eq("status", s);
            Long count = ordersMapper.selectCount(wrapper);
            long c = count != null ? count.longValue() : 0L;
            stats.put(s + "Count", c);
            totalCount += c;
        }
        stats.put("totalCount", totalCount);

        // 完成率
        long completedCount = (long) stats.get("completedCount");
        double completionRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
        stats.put("completionRate", Math.round(completionRate * 10) / 10.0);

        return stats;
    }

    // ========== 私有方法 ==========

    private Orders getExistingOrder(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void updateGoodsShelfStatus(Long goodsId, Integer shelfStatus) {
        if (goodsId == null) {
            return;
        }
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            goods.setShelfStatus(shelfStatus);
            goodsMapper.updateById(goods);
            // 清除商品列表缓存，确保前端获取最新数据
            try {
                if (redisTemplate != null) {
                    redisTemplate.delete(CACHE_ALL_GOODS);
                }
            } catch (Exception e) {
                // 缓存清除失败不影响主流程
            }
        }
    }
}
