package com.campus.exchange.service;

import com.campus.exchange.entity.ExchangeProof;
import com.campus.exchange.entity.Orders;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrdersService {

    /**
     * 获取所有订单列表
     */
    List<Orders> getAllOrders();

    /**
     * 根据ID获取订单详情
     */
    Orders getOrderById(Long id);

    /**
     * 根据状态获取订单列表
     */
    List<Orders> getOrdersByStatus(String status);

    /**
     * 创建订单
     */
    Orders createOrder(Orders order);

    /**
     * 更新订单
     */
    Orders updateOrder(Long id, Orders order);

    /**
     * 删除订单
     */
    void deleteOrder(Long id);

    /**
     * 更新订单状态
     */
    Orders updateOrderStatus(Long id, String status);

    /**
     * 根据用户ID获取订单列表（作为发起方或接收方）
     */
    List<Orders> getOrdersByUserId(Long userId);

    /**
     * 根据用户ID和状态获取订单列表
     */
    List<Orders> getOrdersByUserIdAndStatus(Long userId, String status);

    /**
     * 上传交换凭证（实物照片+评价）
     */
    ExchangeProof uploadProof(Long orderId, Integer userId, String images, String comment);
}
