package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.Result;
import org.example.dto.GoodsListResponse;
import org.example.entity.Good;
import org.example.entity.User;
import org.example.mapper.GoodMapper;
import org.example.mapper.UserMapper;
import org.example.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GoodServiceImpl extends ServiceImpl<GoodMapper, Good> implements GoodService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String GOOD_DETAIL_KEY = "good:detail:";
    private static final long GOOD_TTL_MINUTES = 30;

    @Override
    public Result<GoodsListResponse> getGoodsList(Integer page, Integer limit, String category, String search, String sort, String exchangeType, Integer currentUserId) {
        LambdaQueryWrapper<Good> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Good::getStatus, 1)
                    .eq(Good::getShelfStatus, 1);

        if (StringUtils.hasText(category)) {
            try {
                queryWrapper.eq(Good::getCategoryId, Integer.parseInt(category));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (StringUtils.hasText(search)) {
            queryWrapper.and(wrapper -> wrapper.like(Good::getGoodName, search)
                    .or().like(Good::getDescription, search));
        }

        // 先按时间/热度取数据，后面再做同校重排
        if ("view_count".equalsIgnoreCase(sort)) {
            queryWrapper.orderByDesc(Good::getViewCount);
        } else {
            queryWrapper.orderByDesc(Good::getCreatedAt);
        }

        Page<Good> pageParam = new Page<>(page, limit);
        IPage<Good> resultPage = this.page(pageParam, queryWrapper);
        List<Good> goods = resultPage.getRecords();

        // 填充卖家信息
        for (Good good : goods) {
            good.setExchanged(good.getSoldAt() != null);
            User seller = userMapper.selectById(good.getUserId());
            if (seller != null) {
                seller.setPasswordHash(null);
                good.setSeller(seller);
            }
        }

        // 同校优先 + 同分类聚合排序（仅在未指定分类筛选时生效）
        if (!StringUtils.hasText(category) && currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            String currentSchool = currentUser != null ? currentUser.getSchool() : null;

            if (currentSchool != null && !currentSchool.isEmpty()) {
                // 按分类分组，同校商品排在每组最前
                Map<Integer, List<Good>> byCategory = goods.stream()
                        .collect(Collectors.groupingBy(
                                g -> g.getCategoryId() != null ? g.getCategoryId() : 0));

                final String school = currentSchool;
                List<Good> sorted = new ArrayList<>();
                // 有同校商品的分类优先
                byCategory.entrySet().stream()
                        .sorted((a, b) -> {
                            long sameSchoolA = a.getValue().stream()
                                    .filter(g -> g.getSeller() != null && school.equals(g.getSeller().getSchool()))
                                    .count();
                            long sameSchoolB = b.getValue().stream()
                                    .filter(g -> g.getSeller() != null && school.equals(g.getSeller().getSchool()))
                                    .count();
                            return Long.compare(sameSchoolB, sameSchoolA);
                        })
                        .forEach(entry -> {
                            List<Good> group = entry.getValue();
                            // 每组内同校排前面
                            group.sort((a, b) -> {
                                boolean aSchool = a.getSeller() != null && school.equals(a.getSeller().getSchool());
                                boolean bSchool = b.getSeller() != null && school.equals(b.getSeller().getSchool());
                                return Boolean.compare(bSchool, aSchool);
                            });
                            sorted.addAll(group);
                        });
                goods = sorted;
            }
        }

        GoodsListResponse response = new GoodsListResponse();
        response.setList(goods);
        response.setTotal(resultPage.getTotal());
        response.setPage(page);
        response.setLimit(limit);

        return Result.success(response);
    }

    @Override
    public Result<Good> getGoodDetail(Integer id, Integer userId) {
        String cacheKey = GOOD_DETAIL_KEY + id;
        Good good = null;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                System.out.println("=== 命中 Redis 缓存: " + cacheKey + " ===");
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                good = mapper.convertValue(cached, Good.class);
                final Integer goodId = id;
                new Thread(() -> {
                    Good dbGood = this.getById(goodId);
                    if (dbGood != null) {
                        dbGood.setViewCount(dbGood.getViewCount() + 1);
                        this.updateById(dbGood);
                    }
                }).start();
            }
        } catch (Exception e) {
            System.err.println("Redis 缓存读取失败，回落数据库: " + e.getMessage());
        }

        if (good == null) {
            good = this.getById(id);
            if (good == null) return Result.error(40401, "Good not found");
            good.setViewCount(good.getViewCount() + 1);
            this.updateById(good);
            good.setExchanged(good.getSoldAt() != null);
            User seller = userMapper.selectById(good.getUserId());
            if (seller != null) {
                seller.setPasswordHash(null);
                good.setSeller(seller);
            }
            try {
                redisTemplate.opsForValue().set(cacheKey, good, GOOD_TTL_MINUTES, TimeUnit.MINUTES);
                System.out.println("=== 写入 Redis 缓存: " + cacheKey + " ===");
            } catch (Exception e) {
                System.err.println("Redis 缓存写入失败: " + e.getMessage());
            }
        }

        // 判断当前用户是否已点赞（不走缓存，实时查 Redis Set）
        if (userId != null) {
            Boolean liked = redisTemplate.opsForSet().isMember(GOOD_LIKE_KEY + id, userId.toString());
            good.setIsLiked(Boolean.TRUE.equals(liked));
        } else {
            good.setIsLiked(false);
        }

        return Result.success(good);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> publishGood(Good good) {
        try {
            System.out.println("=== Service层开始处理发布商品 ===");
            System.out.println("接收到的商品对象: " + good);
            
            // 设置默认值
            good.setStatus(0); // 0=待审核
            good.setShelfStatus(0); // 0=已下架
            good.setViewCount(0);
            good.setLikeCount(0);
            good.setFavoriteCount(0);
            
            System.out.println("设置默认值后: " + good);
            
            // 保存商品（good_id由数据库自动生成）
            System.out.println("准备调用save方法...");
            boolean success = this.save(good);
            
            System.out.println("save方法返回: " + success);
            System.out.println("保存后的商品ID: " + good.getGoodId());
            
            if (!success) {
                System.err.println("保存失败！");
                return Result.error(50001, "Failed to publish good");
            }
            
            System.out.println("=== 商品发布成功 ===");
            return Result.success("Published successfully", String.valueOf(good.getGoodId()));
            
        } catch (Exception e) {
            System.err.println("Service层异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(50001, "发布异常: " + e.getMessage());
        }
    }

    private static final String GOOD_LIKE_KEY = "like:good:";

    @Override
    public Result<String> likeGood(Integer goodId, Integer userId) {
        String likeKey = GOOD_LIKE_KEY + goodId;
        Boolean isNew = redisTemplate.opsForSet().add(likeKey, userId.toString()) == 1;
        if (!isNew) {
            return Result.error(40901, "Already liked");
        }
        // 更新数据库 like_count
        Good good = this.getById(goodId);
        if (good == null) {
            redisTemplate.opsForSet().remove(likeKey, userId.toString());
            return Result.error(40401, "Good not found");
        }
        good.setLikeCount(good.getLikeCount() + 1);
        this.updateById(good);
        // 清除商品详情缓存
        redisTemplate.delete(GOOD_DETAIL_KEY + goodId);
        return Result.success("Liked successfully", null);
    }

    @Override
    public Result<String> unlikeGood(Integer goodId, Integer userId) {
        String likeKey = GOOD_LIKE_KEY + goodId;
        Long removed = redisTemplate.opsForSet().remove(likeKey, userId.toString());
        if (removed == null || removed == 0) {
            return Result.error(40401, "Not liked yet");
        }
        Good good = this.getById(goodId);
        if (good != null && good.getLikeCount() > 0) {
            good.setLikeCount(good.getLikeCount() - 1);
            this.updateById(good);
            redisTemplate.delete(GOOD_DETAIL_KEY + goodId);
        }
        return Result.success("Unliked successfully", null);
    }

    @Override
    public Result<String> updateGoodStatus(Integer id, String status, Integer userId) {
        Good good = this.getById(id);
        if (good == null) {
            return Result.error(40401, "Good not found");
        }
        
        if (!good.getUserId().equals(userId)) {
            return Result.error(40301, "Forbidden: You are not the owner");
        }
        
        try {
            good.setStatus(Integer.parseInt(status));
        } catch (NumberFormatException e) {
            return Result.error(40001, "Invalid status value");
        }
        this.updateById(good);
        // 清除该商品的 Redis 缓存
        redisTemplate.delete(GOOD_DETAIL_KEY + id);
        return Result.success("Status updated", null);
    }

    @Override
    public Result<GoodsListResponse> getMyGoods(Integer userId, Integer page, Integer limit, Integer status, Integer shelfStatus) {
        LambdaQueryWrapper<Good> queryWrapper = new LambdaQueryWrapper<>();
        
        // 查询当前用户的商品
        queryWrapper.eq(Good::getUserId, userId);
        
        // 可选的状态筛选
        if (status != null) {
            queryWrapper.eq(Good::getStatus, status);
        }
        
        if (shelfStatus != null) {
            queryWrapper.eq(Good::getShelfStatus, shelfStatus);
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Good::getCreatedAt);
        
        Page<Good> pageParam = new Page<>(page, limit);
        IPage<Good> resultPage = this.page(pageParam, queryWrapper);
        
        // Populate seller info
        List<Good> goods = resultPage.getRecords();
        for (Good good : goods) {
            good.setExchanged(good.getSoldAt() != null);
            
            User seller = userMapper.selectById(good.getUserId());
            if (seller != null) {
                seller.setPasswordHash(null);
                good.setSeller(seller);
            }
        }
        
        GoodsListResponse response = new GoodsListResponse();
        response.setList(goods);
        response.setTotal(resultPage.getTotal());
        response.setPage(page);
        response.setLimit(limit);
        
        return Result.success(response);
    }

    @Override
    public Result<List<Good>> getSimilarGoods(Integer goodId, Integer currentUserId, Integer conditionLevel, String sortBy, Integer limit) {
        Good target = this.getById(goodId);
        if (target == null) {
            return Result.error(40401, "Good not found");
        }

        // 查询当前用户学校（用于同校加分）
        String currentUserSchool = null;
        if (currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser != null) currentUserSchool = currentUser.getSchool();
        }

        // 查同分类、已上架、已审核的商品（排除自身）
        LambdaQueryWrapper<Good> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Good::getStatus, 1)
               .eq(Good::getShelfStatus, 1)
               .ne(Good::getGoodId, goodId)
               .isNull(Good::getDeletedAt);

        // 按成色筛选
        if (conditionLevel != null) {
            wrapper.eq(Good::getConditionLevel, conditionLevel);
        }

        // 按发布时间排序（前端传 sortBy=time）或默认按相似度
        if ("time".equals(sortBy)) {
            wrapper.orderByDesc(Good::getCreatedAt);
        }

        List<Good> candidates = this.list(wrapper);

        // 计算相似分并排序
        final String school = currentUserSchool;
        candidates.sort((a, b) -> {
            int scoreA = calcScore(a, target, school);
            int scoreB = calcScore(b, target, school);
            return scoreB - scoreA;
        });

        // 取前 N 条，填充卖家信息
        int size = limit != null ? limit : 10;
        List<Good> result = candidates.subList(0, Math.min(size, candidates.size()));
        for (Good g : result) {
            User seller = userMapper.selectById(g.getUserId());
            if (seller != null) {
                seller.setPasswordHash(null);
                g.setSeller(seller);
            }
        }

        return Result.success(result);
    }

    private int calcScore(Good candidate, Good target, String currentUserSchool) {
        int score = 0;
        // 同分类 +30
        if (target.getCategoryId() != null && target.getCategoryId().equals(candidate.getCategoryId())) {
            score += 30;
        }
        // 成色相近（差值越小分越高）
        if (target.getConditionLevel() != null && candidate.getConditionLevel() != null) {
            int diff = Math.abs(target.getConditionLevel() - candidate.getConditionLevel());
            score += Math.max(0, 10 - diff * 5);
        }
        // 同校 +20
        if (currentUserSchool != null && !currentUserSchool.isEmpty()) {
            User seller = userMapper.selectById(candidate.getUserId());
            if (seller != null && currentUserSchool.equals(seller.getSchool())) {
                score += 20;
            }
        }
        return score;
    }
}
