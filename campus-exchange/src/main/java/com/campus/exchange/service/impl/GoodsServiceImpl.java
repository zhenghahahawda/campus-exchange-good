package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.constant.SystemConstants;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.enums.GoodsStatus;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.entity.User;
import com.campus.exchange.entity.GoodsCategory;
import com.campus.exchange.entity.Orders;
import com.campus.exchange.mapper.OrdersMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.mapper.GoodsCategoryMapper;
import com.campus.exchange.service.GoodsCategoryService;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.util.RandomCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    private static final Logger log = LoggerFactory.getLogger(GoodsServiceImpl.class);
    private static final String CACHE_ALL_GOODS = "goods:all";
    private static final long CACHE_TTL_MINUTES = 5;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // 成色映射
    private static final Map<Integer, String> CONDITION_MAP = new HashMap<>();

    static {
        CONDITION_MAP.put(10, "全新");
        CONDITION_MAP.put(9, "九成新");
        CONDITION_MAP.put(8, "八成新");
        CONDITION_MAP.put(7, "七成及以下");
        CONDITION_MAP.put(1, "全新");
        CONDITION_MAP.put(2, "九成新");
        CONDITION_MAP.put(3, "八成新");
        CONDITION_MAP.put(4, "七成及以下");
    }

    /**
     * 从缓存/数据库加载分类映射（categoryId -> categoryName）
     */
    private Map<Long, String> loadCategoryMap() {
        List<GoodsCategory> categories = goodsCategoryService.getAllCategories();
        Map<Long, String> map = new HashMap<>();
        if (categories != null) {
            for (GoodsCategory cat : categories) {
                map.put(cat.getCategoryId().longValue(), cat.getCategoryName());
            }
        }
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Goods> getAllGoods() {
        // 优先从 Redis 读取
        if (redisTemplate != null) {
            try {
                Object cached = redisTemplate.opsForValue().get(CACHE_ALL_GOODS);
                if (cached instanceof List) {
                    return (List<Goods>) cached;
                }
            } catch (Exception e) {
                log.warn("从Redis读取商品缓存失败: {}", e.getMessage());
            }
        }

        // Redis miss，查数据库
        List<Goods> goodsList = goodsMapper.selectList(null);
        populateGoodsList(goodsList);

        // 回填缓存
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_ALL_GOODS, goodsList, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("商品列表写入Redis失败: {}", e.getMessage());
            }
        }

        return goodsList;
    }

    @Override
    public PageResponse<Goods> pageGoods(PageRequest pageRequest) {
        Page<Goods> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        Page<Goods> result = goodsMapper.selectPage(page, null);
        populateGoodsList(result.getRecords());
        return convertToPageResponse(result);
    }

    @Override
    public Goods getGoodsById(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        populateGoodsDetails(goods);
        return goods;
    }

    @Override
    public List<Goods> getGoodsByStatus(String status) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, status);
        List<Goods> goodsList = goodsMapper.selectList(wrapper);
        populateGoodsList(goodsList);
        return goodsList;
    }

    @Override
    public PageResponse<Goods> pageGoodsByStatus(String status, PageRequest pageRequest) {
        Page<Goods> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, status);
        Page<Goods> result = goodsMapper.selectPage(page, wrapper);
        populateGoodsList(result.getRecords());
        return convertToPageResponse(result);
    }

    @Override
    @Transactional
    public Goods createGoods(Goods goods) {
        goods.setStatus(GoodsStatus.PENDING.getCode());
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.insert(goods);
        populateGoodsDetails(goods);

        clearGoodsCache();
        return goods;
    }

    @Override
    @Transactional
    public Goods updateGoods(Long id, Goods goods) {
        Goods existingGoods = getGoodsById(id); // 这里已经 populate 了，但没关系，下面更新后会再次 save

        if (goods.getName() != null) {
            existingGoods.setName(goods.getName());
        }
        if (goods.getDescription() != null) {
            existingGoods.setDescription(goods.getDescription());
        }
        if (goods.getCategory() != null) {
            existingGoods.setCategory(goods.getCategory());
        }
        if (goods.getCategoryId() != null) {
             existingGoods.setCategoryId(goods.getCategoryId());
        }
        if (goods.getConditionLevel() != null) {
            existingGoods.setConditionLevel(goods.getConditionLevel());
        }
        if (goods.getRegion() != null) {
            existingGoods.setRegion(goods.getRegion());
        }
        if (goods.getSchool() != null) {
            existingGoods.setSchool(goods.getSchool());
        }
        if (goods.getImages() != null) {
            existingGoods.setImages(goods.getImages());
        }
        if (goods.getDeliveryType() != null) {
            existingGoods.setDeliveryType(goods.getDeliveryType());
        }
        if (goods.getShelfStatus() != null) {
            existingGoods.setShelfStatus(goods.getShelfStatus());
        }

        existingGoods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(existingGoods);

        // 重新填充详情以确保返回最新数据
        populateGoodsDetails(existingGoods);
        clearGoodsCache();
        return existingGoods;
    }

    @Override
    @Transactional
    public void deleteGoods(Long id) {
        // 检查是否有关联的订单
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Orders::getBuyerItemId, id)
                .or()
                .eq(Orders::getSellerItemId, id);
        Long orderCount = ordersMapper.selectCount(orderWrapper);

        if (orderCount > 0) {
            throw new BusinessException("该商品存在关联订单，无法删除");
        }

        goodsMapper.deleteById(id);
        clearGoodsCache();
    }

    @Override
    @Transactional
    public void deleteGoodsBySellerId(Long sellerId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getSellerId, sellerId);
        goodsMapper.delete(wrapper);
        clearGoodsCache();
    }

    @Override
    @Transactional
    public Goods approveGoods(Long id, Integer auditorId) {
        Goods goods = getGoodsById(id); // 这里已经 populate 了
        goods.setStatus(GoodsStatus.ACTIVE.getCode());
        // 审核通过时，默认上架
        goods.setShelfStatus(1);
        goods.setAuditTime(LocalDateTime.now());
        goods.setAuditorId(auditorId != null ? auditorId.longValue() : null);

        // 生成交换凭证码
        // 注意：getGoodsById 填充了 school，但如果数据库里没有 school，可能需要从 user 获取
        // populateGoodsDetails 已经确保了 goods.getSchool() 有值（如果 User 有值）
        String exchangeCode = generateExchangeCode(goods.getSchool());
        goods.setExchangeCode(exchangeCode);

        goodsMapper.updateById(goods);
        clearGoodsCache();
        return goods;
    }

    @Override
    @Transactional
    public Goods rejectGoods(Long id, String reason) {
        Goods goods = getGoodsById(id); // 这里已经 populate 了
        goods.setStatus(GoodsStatus.REJECTED.getCode());
        goods.setRejectReason(reason);
        goods.setAuditTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
        clearGoodsCache();
        return goods;
    }

    /**
     * 填充商品详情（分类名、成色描述、卖家信息）
     */
    private void populateGoodsDetails(Goods goods) {
        if (goods == null) return;

        // 填充分类名称（通过缓存服务查询）
        if (goods.getCategoryId() != null) {
            GoodsCategory category = goodsCategoryService.getCategoryById(goods.getCategoryId().intValue());
            goods.setCategory(category != null ? category.getCategoryName() : "其他");
        }

        // 填充成色描述
        if (goods.getConditionLevel() != null) {
            goods.setCondition(CONDITION_MAP.getOrDefault(goods.getConditionLevel(), "未知成色"));
        }

        // 填充卖家信息
        if (goods.getSellerId() != null) {
            User seller = userMapper.selectById(goods.getSellerId());
            if (seller != null) {
                goods.setSellerName(seller.getUsername()); // 或者使用 realName
                goods.setSellerAvatar(seller.getAvatarUrl());
                // 如果商品本身没有存储学校和地区，使用用户的
                if (goods.getSchool() == null || goods.getSchool().isEmpty()) {
                    goods.setSchool(seller.getSchool());
                }
                if (goods.getRegion() == null || goods.getRegion().isEmpty()) {
                    goods.setRegion(seller.getAddress());
                }
            }
        }
    }

    /**
     * 批量填充商品列表详情
     */
    private void populateGoodsList(List<Goods> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) return;

        // 1. 从数据库加载分类映射
        Map<Long, String> categoryMap = loadCategoryMap();

        // 2. 填充分类名和成色描述
        for (Goods goods : goodsList) {
            if (goods.getCategoryId() != null) {
                goods.setCategory(categoryMap.getOrDefault(goods.getCategoryId(), "其他"));
            }
            if (goods.getConditionLevel() != null) {
                goods.setCondition(CONDITION_MAP.getOrDefault(goods.getConditionLevel(), "未知成色"));
            }
        }

        // 2. 批量查询卖家信息
        Set<Long> sellerIds = goodsList.stream()
                .map(Goods::getSellerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!sellerIds.isEmpty()) {
            List<User> sellers = userMapper.selectBatchIds(sellerIds);
            Map<Integer, User> sellerMap = sellers.stream()
                    .collect(Collectors.toMap(User::getUserId, user -> user));

            for (Goods goods : goodsList) {
                if (goods.getSellerId() != null) {
                    // sellerId 是 Long，User.userId 是 Integer，需要转换
                    User seller = sellerMap.get(goods.getSellerId().intValue());
                    if (seller != null) {
                        goods.setSellerName(seller.getUsername());
                        goods.setSellerAvatar(seller.getAvatarUrl());
                        // 优先使用商品自己的信息，如果没有则使用卖家的
                        if (goods.getSchool() == null || goods.getSchool().isEmpty()) {
                            goods.setSchool(seller.getSchool());
                        }
                        if (goods.getRegion() == null || goods.getRegion().isEmpty()) {
                            goods.setRegion(seller.getAddress());
                        }
                    }
                }
            }
        }
    }

    /**
     * 生成交换凭证码
     * 格式：EX-{学校代码}-{日期}-{随机码}
     */
    private String generateExchangeCode(String school) {
        // 获取学校代码（取前两个字）
        String schoolCode = school != null && school.length() >= 2
            ? school.substring(0, 2)
            : "XX";

        // 获取日期
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 生成随机码
        String randomCode = RandomCodeUtil.generate(SystemConstants.Goods.RANDOM_CODE_LENGTH);

        return String.format("%s-%s-%s-%s",
            SystemConstants.Goods.EXCHANGE_CODE_PREFIX, schoolCode, date, randomCode);
    }

    /**
     * 转换为分页响应
     */
    private <T> PageResponse<T> convertToPageResponse(Page<T> page) {
        return new PageResponse<>(
            page.getCurrent(),
            page.getSize(),
            page.getTotal(),
            page.getPages(),
            page.getRecords()
        );
    }

    /**
     * 清除商品列表缓存
     */
    private void clearGoodsCache() {
        if (redisTemplate == null) return;
        try {
            redisTemplate.delete(CACHE_ALL_GOODS);
        } catch (Exception e) {
            log.warn("清除商品缓存失败: {}", e.getMessage());
        }
    }
}
