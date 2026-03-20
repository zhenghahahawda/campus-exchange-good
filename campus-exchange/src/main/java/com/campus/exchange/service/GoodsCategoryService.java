package com.campus.exchange.service;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.GoodsCategory;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface GoodsCategoryService {

    /**
     * 创建商品分类
     */
    GoodsCategory createCategory(GoodsCategory category);

    /**
     * 根据ID获取商品分类
     */
    GoodsCategory getCategoryById(Integer categoryId);

    /**
     * 根据分类代码获取商品分类
     */
    GoodsCategory getCategoryByCode(String categoryCode);

    /**
     * 获取所有商品分类
     */
    List<GoodsCategory> getAllCategories();

    /**
     * 获取所有启用的商品分类
     */
    List<GoodsCategory> getActiveCategories();

    /**
     * 分页查询商品分类
     */
    PageResponse<GoodsCategory> pageCategories(PageRequest pageRequest, Integer isActive, String keyword);

    /**
     * 更新商品分类
     */
    GoodsCategory updateCategory(Integer categoryId, GoodsCategory category);

    /**
     * 删除商品分类
     */
    void deleteCategory(Integer categoryId);

    /**
     * 批量删除商品分类
     */
    void batchDeleteCategories(List<Integer> categoryIds);

    /**
     * 启用商品分类
     */
    void activateCategory(Integer categoryId);

    /**
     * 禁用商品分类
     */
    void deactivateCategory(Integer categoryId);

    /**
     * 更新排序顺序
     */
    void updateSortOrder(Integer categoryId, Integer sortOrder);
}
