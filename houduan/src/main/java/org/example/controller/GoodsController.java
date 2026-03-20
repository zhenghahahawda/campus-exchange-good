package org.example.controller;

import org.example.common.Result;
import org.example.dto.GoodsListResponse;
import org.example.dto.PublishGoodRequest;
import org.example.dto.UpdateStatusRequest;
import org.example.entity.Good;
import org.example.entity.GoodsCategory;
import org.example.service.CosService;
import org.example.service.GoodService;
import org.example.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodService goodService;
    
    @Autowired
    private CosService cosService;

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    // 前端调用 /api/goods/categories，在这里也提供一个入口
    @GetMapping("/categories")
    public Result<List<GoodsCategory>> getCategories() {
        return goodsCategoryService.getActiveCategories();
    }

    @GetMapping
    public Result<GoodsListResponse> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String exchangeType,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Integer currentUserId = userId != null ? Integer.valueOf(userId) : null;
        return goodService.getGoodsList(page, limit, category, search, sort, exchangeType, currentUserId);
    }

    @GetMapping("/my")
    public Result<GoodsListResponse> myGoods(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer shelfStatus,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return goodService.getMyGoods(Integer.valueOf(userId), page, limit, status, shelfStatus);
    }

    @GetMapping("/{id}")
    public Result<Good> detail(@PathVariable Integer id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        return goodService.getGoodDetail(id, userId != null ? Integer.valueOf(userId) : null);
    }

    @PostMapping
    public Result<String> publish(@RequestBody PublishGoodRequest request, HttpServletRequest httpRequest) {
        try {
            System.out.println("=== 收到发布商品请求 ===");
            System.out.println("请求数据: " + request);
            
            String userId = (String) httpRequest.getAttribute("userId");
            if (userId == null) {
                System.out.println("错误: 未授权，userId为空");
                return Result.error(40101, "Unauthorized");
            }
            
            System.out.println("用户ID: " + userId);
            
            // 转换前端数据到数据库实体
            Good good = new Good();
            good.setGoodName(request.getGoodName());
            good.setDescription(request.getDescription());
            good.setExchangeFor(request.getExchangeFor());
            good.setImages(request.getImages());
            good.setUserId(Integer.valueOf(userId));
            
            // conditionLevel：优先用前端直传的数字，fallback 到字符串转换
            System.out.println("=== condition原始值: [" + request.getCondition() + "] conditionLevel直传: [" + request.getConditionLevel() + "] ===");
            Integer conditionLevel = request.getConditionLevel() != null
                    ? request.getConditionLevel()
                    : convertConditionToLevel(request.getCondition());
            if (conditionLevel == null) {
                return Result.error(40001, "Invalid condition value");
            }
            good.setConditionLevel(conditionLevel);

            // categoryId：优先用前端直传的数字，fallback 到字符串转换
            Integer categoryId = request.getCategoryId() != null
                    ? request.getCategoryId()
                    : convertCategoryToId(request.getCategory());
            if (categoryId == null) {
                return Result.error(40001, "Invalid category value");
            }
            good.setCategoryId(categoryId);
            
            System.out.println("转换后的商品对象: " + good);
            
            Result<String> result = goodService.publishGood(good);
            System.out.println("发布结果: " + result);
            
            return result;
        } catch (Exception e) {
            System.err.println("发布商品异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(50001, "发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 转换新旧程度字符串到数字
     * 全新=1, 九成新=2, 八成新=3, 七成及以下=4
     */
    private Integer convertConditionToLevel(String condition) {
        if (condition == null) return 1;
        switch (condition) {
            case "new":
            case "全新": return 1;
            case "90":
            case "九成新": return 2;
            case "80":
            case "八成新": return 3;
            case "70":
            case "七成新":
            case "七成及以下":
            case "七成以下": return 4;
            default:
                try { return Integer.parseInt(condition); } catch (Exception ignored) {}
                return 1;
        }
    }
    
    /**
     * 转换分类字符串到分类ID
     * 这里需要根据实际的分类映射关系
     */
    private Integer convertCategoryToId(String category) {
        if (category == null) return 1;
        // 根据数据库中的分类映射
        switch (category.toLowerCase()) {
            case "electronics":
            case "数码产品": return 1;
            case "books":
            case "图书文具": return 6;
            case "home":
            case "家居生活": return 11;
            case "clothing":
            case "服饰鞋包": return 16;
            case "sports":
            case "运动户外": return 21;
            case "beauty":
            case "美妆个护": return 26;
            case "toys":
            case "玩具手办": return 30;
            case "pets":
            case "宠物用品": return 31;
            case "other":
            case "其他闲置": return 32;
            default: return 1; // 默认数码产品
        }
    }

    @GetMapping("/{id}/similar")
    public Result<List<Good>> similarGoods(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer conditionLevel,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "10") Integer limit,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Integer currentUserId = userId != null ? Integer.valueOf(userId) : null;
        return goodService.getSimilarGoods(id, currentUserId, conditionLevel, sortBy, limit);
    }

    @PostMapping("/{id}/like")
    public Result<String> likeGood(@PathVariable Integer id, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) return Result.error(40101, "Unauthorized");
        return goodService.likeGood(id, Integer.valueOf(userId));
    }

    @DeleteMapping("/{id}/like")
    public Result<String> unlikeGood(@PathVariable Integer id, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) return Result.error(40101, "Unauthorized");
        return goodService.unlikeGood(id, Integer.valueOf(userId));
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Integer id, @RequestBody UpdateStatusRequest request, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return goodService.updateGoodStatus(id, request.getStatus(), Integer.valueOf(userId));
    }
    
    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(40001, "File is empty");
        }
        try {
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            String key = "goods/" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            cosService.uploadFile(tempFile.getAbsolutePath(), key);
            
            String url = "https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/" + key;
            
            if (tempFile.exists()) {
                tempFile.delete();
            }
            return Result.success("Upload successful", url);
        } catch (Exception e) {
            return Result.error(50001, "Upload failed: " + e.getMessage());
        }
    }
}
