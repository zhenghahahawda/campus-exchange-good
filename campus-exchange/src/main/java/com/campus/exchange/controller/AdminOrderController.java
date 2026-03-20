package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.ExchangeProof;
import com.campus.exchange.entity.Orders;
import com.campus.exchange.service.AdminOrderService;
import com.campus.exchange.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员订单管理控制器
 */
@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    // ========== 订单基础管理 ==========

    @GetMapping
    @RequireAdmin
    public Result<PageResponse<Orders>> listOrders(
            PageRequest pageRequest,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        try {
            PageResponse<Orders> page = adminOrderService.pageOrders(pageRequest, status, keyword);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取订单列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    @RequireAdmin
    public Result<Orders> getOrderDetail(@PathVariable Long orderId) {
        try {
            Orders order = adminOrderService.getOrderDetail(orderId);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("获取订单详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/status")
    @RequireAdmin
    public Result<Orders> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null || status.trim().isEmpty()) {
                return Result.error("状态不能为空");
            }
            Orders order = adminOrderService.updateOrderStatus(orderId, status);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("修改订单状态失败: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/remark")
    @RequireAdmin
    public Result<Orders> updateOrderRemark(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            String remark = request.get("remark");
            Orders order = adminOrderService.updateOrderRemark(orderId, remark);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("编辑订单备注失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{orderId}")
    @RequireAdmin
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        try {
            adminOrderService.deleteOrder(orderId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除订单失败: " + e.getMessage());
        }
    }

    // ========== 订单处理流程 ==========

    @PutMapping("/{orderId}/process")
    @RequireAdmin
    public Result<Orders> processOrder(@PathVariable Long orderId) {
        try {
            Orders order = adminOrderService.processOrder(orderId);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("处理订单失败: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/complete")
    @RequireAdmin
    public Result<Orders> completeOrder(@PathVariable Long orderId) {
        try {
            Orders order = adminOrderService.completeOrder(orderId);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("完成订单失败: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/cancel")
    @RequireAdmin
    public Result<Orders> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String cancelReason = request.get("cancelReason");
            if (cancelReason == null || cancelReason.trim().isEmpty()) {
                return Result.error("取消原因不能为空");
            }
            Integer adminId = (Integer) httpRequest.getAttribute("userId");
            if (adminId == null) {
                return Result.error("未登录或Token无效");
            }
            Orders order = adminOrderService.cancelOrder(orderId, adminId, cancelReason);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("取消订单失败: " + e.getMessage());
        }
    }

    // ========== 交换凭证管理 ==========

    @GetMapping("/{orderId}/proofs")
    @RequireAdmin
    public Result<List<ExchangeProof>> getOrderProofs(@PathVariable Long orderId) {
        try {
            List<ExchangeProof> proofs = adminOrderService.getOrderProofs(orderId);
            return Result.success(proofs);
        } catch (Exception e) {
            return Result.error("获取交换凭证失败: " + e.getMessage());
        }
    }

    @PutMapping("/proofs/{proofId}/verify")
    @RequireAdmin
    public Result<ExchangeProof> verifyProof(
            @PathVariable Integer proofId,
            HttpServletRequest httpRequest) {
        try {
            Integer adminId = (Integer) httpRequest.getAttribute("userId");
            if (adminId == null) {
                return Result.error("未登录或Token无效");
            }
            ExchangeProof proof = adminOrderService.verifyProof(proofId, adminId);
            return Result.success(proof);
        } catch (Exception e) {
            return Result.error("审核凭证失败: " + e.getMessage());
        }
    }

    // ========== 统计接口 ==========

    @GetMapping("/statistics")
    @RequireAdmin
    public Result<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> stats = adminOrderService.getOrderStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取订单统计失败: " + e.getMessage());
        }
    }
}
