package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.common.Result;
import org.example.entity.GoodsCategory;
import org.example.mapper.GoodsCategoryMapper;
import org.example.service.GoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVE_CATEGORIES_KEY = "categories:active";
    private static final String ALL_CATEGORIES_KEY = "categories:all";
    private static final long CATEGORIES_TTL_HOURS = 12; // 分类变动少，缓存12小时

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Result<List<GoodsCategory>> getAllCategories() {
        try {
            Object cached = redisTemplate.opsForValue().get(ALL_CATEGORIES_KEY);
            if (cached != null) {
                List<GoodsCategory> list = objectMapper.convertValue(cached,
                        new TypeReference<List<GoodsCategory>>() {});
                System.out.println("=== 命中 Redis 分类缓存: " + ALL_CATEGORIES_KEY + " ===");
                return Result.success(list);
            }
        } catch (Exception e) {
            System.err.println("Redis 分类缓存读取失败: " + e.getMessage());
        }
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(GoodsCategory::getSortOrder)
               .orderByAsc(GoodsCategory::getCategoryId);
        List<GoodsCategory> categories = this.list(wrapper);
        try {
            redisTemplate.opsForValue().set(ALL_CATEGORIES_KEY, categories, CATEGORIES_TTL_HOURS, TimeUnit.HOURS);
            System.out.println("=== 写入 Redis 分类缓存: " + ALL_CATEGORIES_KEY + " ===");
        } catch (Exception e) {
            System.err.println("Redis 分类缓存写入失败: " + e.getMessage());
        }
        return Result.success(categories);
    }

    @Override
    public Result<List<GoodsCategory>> getActiveCategories() {
        try {
            Object cached = redisTemplate.opsForValue().get(ACTIVE_CATEGORIES_KEY);
            if (cached != null) {
                List<GoodsCategory> list = objectMapper.convertValue(cached,
                        new TypeReference<List<GoodsCategory>>() {});
                System.out.println("=== 命中 Redis 分类缓存: " + ACTIVE_CATEGORIES_KEY + " ===");
                return Result.success(list);
            }
        } catch (Exception e) {
            System.err.println("Redis 分类缓存读取失败: " + e.getMessage());
        }
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsCategory::getIsActive, 1)
               .orderByAsc(GoodsCategory::getSortOrder)
               .orderByAsc(GoodsCategory::getCategoryId);
        List<GoodsCategory> categories = this.list(wrapper);
        try {
            redisTemplate.opsForValue().set(ACTIVE_CATEGORIES_KEY, categories, CATEGORIES_TTL_HOURS, TimeUnit.HOURS);
            System.out.println("=== 写入 Redis 分类缓存: " + ACTIVE_CATEGORIES_KEY + " ===");
        } catch (Exception e) {
            System.err.println("Redis 分类缓存写入失败: " + e.getMessage());
        }
        return Result.success(categories);
    }
}
