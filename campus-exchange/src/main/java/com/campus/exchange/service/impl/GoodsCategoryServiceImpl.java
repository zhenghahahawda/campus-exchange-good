package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.GoodsCategory;
import com.campus.exchange.mapper.GoodsCategoryMapper;
import com.campus.exchange.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品分类服务实现类
 */
@Service
public class GoodsCategoryServiceImpl implements GoodsCategoryService {

    private static final String CACHE_ALL_CATEGORIES = "categories:all";
    private static final String CACHE_ACTIVE_CATEGORIES = "categories:active";
    private static final long CACHE_TTL_HOURS = 24;

    @Autowired
    private GoodsCategoryMapper categoryMapper;

    @Autowired(required = false)  // Redis 已禁用，设置为可选依赖
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public GoodsCategory createCategory(GoodsCategory category) {
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
        if (category.getIsActive() == null) {
            category.setIsActive(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
        clearCategoryCache();
        return category;
    }

    @Override
    public GoodsCategory getCategoryById(Integer categoryId) {
        // 优先从全量缓存中查找，避免单独查库
        List<GoodsCategory> allCategories = getAllCategories();
        if (allCategories != null) {
            for (GoodsCategory cat : allCategories) {
                if (cat.getCategoryId() != null && cat.getCategoryId().equals(categoryId)) {
                    return cat;
                }
            }
        }
        // 缓存中未找到，回退查库
        return categoryMapper.selectById(categoryId);
    }

    @Override
    public GoodsCategory getCategoryByCode(String categoryCode) {
        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_code", categoryCode);
        return categoryMapper.selectOne(queryWrapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GoodsCategory> getAllCategories() {
        // 尝试从缓存读取（Redis 已禁用时跳过）
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(CACHE_ALL_CATEGORIES);
                if (cached instanceof List) {
                    return (List<GoodsCategory>) cached;
                }
            } catch (Exception e) {
                // Redis 异常不影响业务，降级到数据库查询
            }
        }

        // 从数据库查询
        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order", "category_id");
        List<GoodsCategory> categories = categoryMapper.selectList(queryWrapper);

        // 写入缓存（Redis 已禁用时跳过）
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_ALL_CATEGORIES, categories, CACHE_TTL_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                // 缓存写入失败不影响业务
            }
        }

        return categories;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<GoodsCategory> getActiveCategories() {
        // 尝试从缓存读取（Redis 已禁用时跳过）
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(CACHE_ACTIVE_CATEGORIES);
                if (cached instanceof List) {
                    return (List<GoodsCategory>) cached;
                }
            } catch (Exception e) {
                // Redis 异常不影响业务
            }
        }

        // 从数据库查询
        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", 1);
        queryWrapper.orderByAsc("sort_order", "category_id");
        List<GoodsCategory> categories = categoryMapper.selectList(queryWrapper);

        // 写入缓存（Redis 已禁用时跳过）
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_ACTIVE_CATEGORIES, categories, CACHE_TTL_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                // 缓存写入失败不影响业务
            }
        }

        return categories;
    }

    @Override
    public PageResponse<GoodsCategory> pageCategories(PageRequest pageRequest, Integer isActive, String keyword) {
        Page<GoodsCategory> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<GoodsCategory> queryWrapper = new QueryWrapper<>();

        // 根据状态筛选
        if (isActive != null) {
            queryWrapper.eq("is_active", isActive);
        }

        // 根据关键词搜索（分类名称或分类代码）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("category_name", keyword)
                    .or()
                    .like("category_code", keyword)
            );
        }

        // 排序
        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByAsc("sort_order", "category_id");
        }

        IPage<GoodsCategory> result = categoryMapper.selectPage(page, queryWrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public GoodsCategory updateCategory(Integer categoryId, GoodsCategory category) {
        category.setCategoryId(categoryId);
        categoryMapper.updateById(category);
        clearCategoryCache();
        return categoryMapper.selectById(categoryId);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        categoryMapper.deleteById(categoryId);
        clearCategoryCache();
    }

    @Override
    public void batchDeleteCategories(List<Integer> categoryIds) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryMapper.deleteBatchIds(categoryIds);
            clearCategoryCache();
        }
    }

    @Override
    public void activateCategory(Integer categoryId) {
        GoodsCategory category = new GoodsCategory();
        category.setCategoryId(categoryId);
        category.setIsActive(1);
        categoryMapper.updateById(category);
        clearCategoryCache();
    }

    @Override
    public void deactivateCategory(Integer categoryId) {
        GoodsCategory category = new GoodsCategory();
        category.setCategoryId(categoryId);
        category.setIsActive(0);
        categoryMapper.updateById(category);
        clearCategoryCache();
    }

    @Override
    public void updateSortOrder(Integer categoryId, Integer sortOrder) {
        GoodsCategory category = new GoodsCategory();
        category.setCategoryId(categoryId);
        category.setSortOrder(sortOrder);
        categoryMapper.updateById(category);
        clearCategoryCache();
    }

    /**
     * 清除分类缓存
     */
    private void clearCategoryCache() {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(CACHE_ALL_CATEGORIES);
                redisTemplate.delete(CACHE_ACTIVE_CATEGORIES);
            } catch (Exception e) {
                // 缓存清除失败不影响业务
            }
        }
    }
}
