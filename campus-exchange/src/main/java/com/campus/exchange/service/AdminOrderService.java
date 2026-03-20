package com.campus.exchange.service;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.ExchangeProof;
import com.campus.exchange.entity.Orders;

import java.util.List;
import java.util.Map;

/**
 * 管理员订单服务接口
 */
public interface AdminOrderService {

    // ========== 订单基础管理 ==========

    PageResponse<Orders> pageOrders(PageRequest pageRequest, String status, String keyword);

    Orders getOrderDetail(Long orderId);

    Orders updateOrderStatus(Long orderId, String status);

    Orders updateOrderRemark(Long orderId, String remark);

    void deleteOrder(Long orderId);

    // ========== 订单处理流程 ==========

    Orders processOrder(Long orderId);

    Orders completeOrder(Long orderId);

    Orders cancelOrder(Long orderId, Integer adminId, String cancelReason);

    // ========== 交换凭证管理 ==========

    List<ExchangeProof> getOrderProofs(Long orderId);

    ExchangeProof verifyProof(Integer proofId, Integer adminId);

    // ========== 统计接口 ==========

    Map<String, Object> getOrderStatistics();
}
