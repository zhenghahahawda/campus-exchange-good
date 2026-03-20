package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.GoodsCategory;
import com.campus.exchange.service.GoodsCategoryService;
import com.campus.exchange.util.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "商品分类管理")
public class GoodsCategoryController {

    @Autowired
    private GoodsCategoryService categoryService;

    /**
     * 创建商品分类
     */
    @PostMapping
    @RequireAdmin
    @Operation(summary = "创建商品分类", description = "管理员创建新的商品分类")
    public Result<GoodsCategory> createCategory(@RequestBody GoodsCategory category) {
        try {
            // 检查分类代码是否已存在
            GoodsCategory existing = categoryService.getCategoryByCode(category.getCategoryCode());
            if (existing != null) {
                return Result.error("分类代码已存在");
            }
            GoodsCategory created = categoryService.createCategory(category);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品分类
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "获取商品分类详情", description = "根据分类ID获取商品分类详细信息")
    public Result<GoodsCategory> getCategoryById(@PathVariable Integer categoryId) {
        try {
            GoodsCategory category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                return Result.error("商品分类不存在");
            }
            return Result.success(category);
        } catch (Exception e) {
            return Result.error("获取商品分类详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据分类代码获取商品分类
     */
    @GetMapping("/code/{categoryCode}")
    @Operation(summary = "根据代码获取商品分类", description = "通过分类代码查询商品分类")
    public Result<GoodsCategory> getCategoryByCode(@PathVariable String categoryCode) {
        try {
            GoodsCategory category = categoryService.getCategoryByCode(categoryCode);
            if (category == null) {
                return Result.error("商品分类不存在");
            }
            return Result.success(category);
        } catch (Exception e) {
            return Result.error("获取商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有商品分类
     */
    @GetMapping
    @Operation(summary = "获取所有商品分类", description = "获取系统中所有商品分类")
    public Result<List<GoodsCategory>> getAllCategories() {
        try {
            List<GoodsCategory> categories = categoryService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("获取商品分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的商品分类
     */
    @GetMapping("/active")
    @Operation(summary = "获取启用的商品分类", description = "获取所有启用状态的商品分类")
    public Result<List<GoodsCategory>> getActiveCategories() {
        try {
            List<GoodsCategory> categories = categoryService.getActiveCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("获取启用商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询商品分类
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询商品分类", description = "分页查询商品分类列表")
    public Result<PageResponse<GoodsCategory>> pageCategories(
            PageRequest pageRequest,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(required = false) String keyword) {
        try {
            PageResponse<GoodsCategory> page = categoryService.pageCategories(pageRequest, isActive, keyword);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品分类
     */
    @PutMapping("/{categoryId}")
    @RequireAdmin
    @Operation(summary = "更新商品分类", description = "管理员更新商品分类信息")
    public Result<GoodsCategory> updateCategory(
            @PathVariable Integer categoryId,
            @RequestBody GoodsCategory category) {
        try {
            // 检查分类是否存在
            GoodsCategory existing = categoryService.getCategoryById(categoryId);
            if (existing == null) {
                return Result.error("商品分类不存在");
            }

            // 如果修改了分类代码，检查新代码是否已被使用
            if (category.getCategoryCode() != null &&
                !category.getCategoryCode().equals(existing.getCategoryCode())) {
                GoodsCategory codeCheck = categoryService.getCategoryByCode(category.getCategoryCode());
                if (codeCheck != null) {
                    return Result.error("分类代码已存在");
                }
            }

            GoodsCategory updated = categoryService.updateCategory(categoryId, category);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品分类
     */
    @DeleteMapping("/{categoryId}")
    @RequireAdmin
    @Operation(summary = "删除商品分类", description = "管理员删除指定商品分类")
    public Result<Void> deleteCategory(@PathVariable Integer categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除商品分类
     */
    @DeleteMapping("/batch")
    @RequireAdmin
    @Operation(summary = "批量删除商品分类", description = "管理员批量删除商品分类")
    public Result<Void> batchDeleteCategories(@RequestBody List<Integer> categoryIds) {
        try {
            categoryService.batchDeleteCategories(categoryIds);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("批量删除商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 启用商品分类
     */
    @PostMapping("/{categoryId}/activate")
    @RequireAdmin
    @Operation(summary = "启用商品分类", description = "管理员启用指定商品分类")
    public Result<Void> activateCategory(@PathVariable Integer categoryId) {
        try {
            categoryService.activateCategory(categoryId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("启用商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 禁用商品分类
     */
    @PostMapping("/{categoryId}/deactivate")
    @RequireAdmin
    @Operation(summary = "禁用商品分类", description = "管理员禁用指定商品分类")
    public Result<Void> deactivateCategory(@PathVariable Integer categoryId) {
        try {
            categoryService.deactivateCategory(categoryId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("禁用商品分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新排序顺序
     */
    @PutMapping("/{categoryId}/sort")
    @RequireAdmin
    @Operation(summary = "更新排序顺序", description = "管理员更新商品分类的排序顺序")
    public Result<Void> updateSortOrder(
            @PathVariable Integer categoryId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer sortOrder = request.get("sortOrder");
            if (sortOrder == null) {
                return Result.error("排序顺序不能为空");
            }
            categoryService.updateSortOrder(categoryId, sortOrder);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("更新排序顺序失败: " + e.getMessage());
        }
    }
}
