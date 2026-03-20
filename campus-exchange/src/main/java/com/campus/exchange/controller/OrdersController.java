package com.campus.exchange.controller;

import com.campus.exchange.entity.ExchangeProof;
import com.campus.exchange.entity.Orders;
import com.campus.exchange.service.OrdersService;
import com.campus.exchange.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器（普通用户）
 * 用户只能查看和操作自己参与的订单
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 获取当前用户的订单列表（作为发起方或接收方）
     */
    @GetMapping
    public Result<List<Orders>> getMyOrders(HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            List<Orders> orders = ordersService.getOrdersByUserId(currentUserId.longValue());
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取订单详情（仅订单参与方可查看）
     */
    @GetMapping("/{id}")
    public Result<Orders> getOrderById(@PathVariable Long id, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            Orders order = ordersService.getOrderById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (!isOrderParticipant(order, currentUserId.longValue())) {
                return Result.error("无权查看此订单");
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取当前用户的订单列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Orders>> getMyOrdersByStatus(@PathVariable String status, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            List<Orders> orders = ordersService.getOrdersByUserIdAndStatus(currentUserId.longValue(), status);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建订单（发起方ID从Token中获取，防止伪造）
     */
    @PostMapping
    public Result<Orders> createOrder(@RequestBody Orders order, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            // 从Token中获取发起方ID，防止伪造
            order.setBuyerId(currentUserId.longValue());
            Orders createdOrder = ordersService.createOrder(order);
            return Result.success(createdOrder);
        } catch (Exception e) {
            return Result.error("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单（仅订单参与方可操作）
     */
    @PutMapping("/{id}")
    public Result<Orders> updateOrder(@PathVariable Long id, @RequestBody Orders order, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            Orders existingOrder = ordersService.getOrderById(id);
            if (existingOrder == null) {
                return Result.error("订单不存在");
            }
            if (!isOrderParticipant(existingOrder, currentUserId.longValue())) {
                return Result.error("无权修改此订单");
            }
            Orders updatedOrder = ordersService.updateOrder(id, order);
            return Result.success(updatedOrder);
        } catch (Exception e) {
            return Result.error("更新订单失败: " + e.getMessage());
        }
    }

    /**
     * 删除订单（仅订单发起方可操作）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            Orders existingOrder = ordersService.getOrderById(id);
            if (existingOrder == null) {
                return Result.error("订单不存在");
            }
            if (!Long.valueOf(currentUserId).equals(existingOrder.getBuyerId())) {
                return Result.error("无权删除此订单");
            }
            ordersService.deleteOrder(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单状态（仅订单参与方可操作）
     */
    @PutMapping("/{id}/status")
    public Result<Orders> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody,
                                            HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            String status = requestBody.get("status");
            if (status == null || status.trim().isEmpty()) {
                return Result.error("状态不能为空");
            }
            Orders existingOrder = ordersService.getOrderById(id);
            if (existingOrder == null) {
                return Result.error("订单不存在");
            }
            if (!isOrderParticipant(existingOrder, currentUserId.longValue())) {
                return Result.error("无权修改此订单状态");
            }
            Orders order = ordersService.updateOrderStatus(id, status);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 判断用户是否为订单参与方（发起方或接收方）
     */
    private boolean isOrderParticipant(Orders order, Long userId) {
        return userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId());
    }

    /**
     * 上传交换凭证（实物照片最多3张 + 评价）
     */
    @PostMapping("/{id}/proof")
    public Result<ExchangeProof> uploadProof(@PathVariable Long id,
                                              @RequestBody Map<String, String> requestBody,
                                              HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            String images = requestBody.get("images");
            String comment = requestBody.get("comment");
            if (images == null || images.trim().isEmpty()) {
                return Result.error("请上传至少一张实物照片");
            }
            ExchangeProof proof = ordersService.uploadProof(id, currentUserId, images, comment);
            return Result.success(proof);
        } catch (Exception e) {
            return Result.error("上传凭证失败: " + e.getMessage());
        }
    }
}
