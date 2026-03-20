package com.campus.exchange.controller;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import com.campus.exchange.service.impl.CosServiceImpl;

import com.campus.exchange.annotation.RequireAdmin;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CosServiceImpl cosService;

    /**
     * 上传商品图片
     */
    @PostMapping("/upload")
    public Result<String> uploadGoodsImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cosService.uploadGoodsImage(file);
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有商品列表
     */
    @GetMapping
    public Result<List<Goods>> getAllGoods() {
        try {
            List<Goods> goods = goodsService.getAllGoods();
            return Result.success(goods);
        } catch (Exception e) {
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public Result<PageResponse<Goods>> pageGoods(PageRequest pageRequest) {
        try {
            PageResponse<Goods> page = goodsService.pageGoods(pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询商品失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Goods> getGoodsById(@PathVariable Long id) {
        try {
            Goods goods = goodsService.getGoodsById(id);
            return Result.success(goods);
        } catch (Exception e) {
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取商品列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Goods>> getGoodsByStatus(@PathVariable String status) {
        try {
            List<Goods> goods = goodsService.getGoodsByStatus(status);
            return Result.success(goods);
        } catch (Exception e) {
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态分页查询商品列表
     */
    @GetMapping("/status/{status}/page")
    public Result<PageResponse<Goods>> pageGoodsByStatus(
            @PathVariable String status,
            PageRequest pageRequest) {
        try {
            PageResponse<Goods> page = goodsService.pageGoodsByStatus(status, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询商品失败: " + e.getMessage());
        }
    }

    /**
     * 创建商品
     */
    @PostMapping
    public Result<Goods> createGoods(@RequestBody Goods goods, HttpServletRequest request) {
        try {
            // 从 Token 中获取当前用户ID，覆盖前端传来的 sellerId，确保安全
            Integer currentUserId = (Integer) request.getAttribute("userId");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }
            // 将 Integer 转换为 Long
            goods.setSellerId(currentUserId.longValue());

            Goods createdGoods = goodsService.createGoods(goods);
            return Result.success(createdGoods);
        } catch (Exception e) {
            return Result.error("创建商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品（仅商品所有者或管理员可操作）
     */
    @PutMapping("/{id}")
    public Result<Goods> updateGoods(@PathVariable Long id, @RequestBody Goods goods, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            Integer userType = (Integer) request.getAttribute("userType");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }

            // 管理员（userType=1）可以修改任何商品，普通用户只能修改自己的商品
            if (userType != null && userType == 1) {
                // 管理员直接放行
                Goods updatedGoods = goodsService.updateGoods(id, goods);
                return Result.success(updatedGoods);
            } else {
                // 普通用户需要校验所有权
                Goods existingGoods = goodsService.getGoodsById(id);
                if (existingGoods == null) {
                    return Result.error("商品不存在");
                }
                if (!Long.valueOf(currentUserId).equals(existingGoods.getSellerId())) {
                    return Result.error("无权修改他人的商品");
                }
                Goods updatedGoods = goodsService.updateGoods(id, goods);
                return Result.success(updatedGoods);
            }
        } catch (Exception e) {
            return Result.error("更新商品失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品（仅商品所有者或管理员可操作）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id, HttpServletRequest request) {
        try {
            Integer currentUserId = (Integer) request.getAttribute("userId");
            Integer userType = (Integer) request.getAttribute("userType");
            if (currentUserId == null) {
                return Result.error("未登录或Token无效");
            }

            // 管理员（userType=1）可以删除任何商品，普通用户只能删除自己的商品
            if (userType != null && userType == 1) {
                // 管理员直接放行
                goodsService.deleteGoods(id);
                return Result.success(null);
            } else {
                // 普通用户需要校验所有权
                Goods existingGoods = goodsService.getGoodsById(id);
                if (existingGoods == null) {
                    return Result.error("商品不存在");
                }
                if (!Long.valueOf(currentUserId).equals(existingGoods.getSellerId())) {
                    return Result.error("无权删除他人的商品");
                }
                goodsService.deleteGoods(id);
                return Result.success(null);
            }
        } catch (Exception e) {
            return Result.error("删除商品失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过商品（管理员）
     */
    @PostMapping("/{id}/approve")
    @RequireAdmin("审核通过商品")
    public Result<Goods> approveGoods(@PathVariable Long id, HttpServletRequest request) {
        try {
            Integer auditorId = (Integer) request.getAttribute("userId");
            if (auditorId == null) {
                return Result.error("未登录或Token无效");
            }
            Goods goods = goodsService.approveGoods(id, auditorId);
            return Result.success(goods);
        } catch (Exception e) {
            return Result.error("审核通过失败: " + e.getMessage());
        }
    }

    /**
     * 驳回商品（管理员）
     */
    @PostMapping("/{id}/reject")
    @RequireAdmin("驳回商品")
    public Result<Goods> rejectGoods(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return Result.error("驳回原因不能为空");
            }
            Goods goods = goodsService.rejectGoods(id, reason);
            return Result.success(goods);
        } catch (Exception e) {
            return Result.error("驳回商品失败: " + e.getMessage());
        }
    }
}
