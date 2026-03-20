package com.campus.exchange.service;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.Goods;

import java.util.List;

/**
 * 商品服务接口
 */
public interface GoodsService {

    /**
     * 获取所有商品列表
     */
    List<Goods> getAllGoods();

    /**
     * 分页查询商品列表
     */
    PageResponse<Goods> pageGoods(PageRequest pageRequest);

    /**
     * 根据ID获取商品详情
     */
    Goods getGoodsById(Long id);

    /**
     * 根据状态获取商品列表
     */
    List<Goods> getGoodsByStatus(String status);

    /**
     * 根据状态分页查询商品列表
     */
    PageResponse<Goods> pageGoodsByStatus(String status, PageRequest pageRequest);

    /**
     * 创建商品
     */
    Goods createGoods(Goods goods);

    /**
     * 更新商品
     */
    Goods updateGoods(Long id, Goods goods);

    /**
     * 删除商品
     */
    void deleteGoods(Long id);

    /**
     * 根据卖家ID删除所有商品（级联删除用户时使用）
     */
    void deleteGoodsBySellerId(Long sellerId);

    /**
     * 审核通过商品
     */
    Goods approveGoods(Long id, Integer auditorId);

    /**
     * 驳回商品
     */
    Goods rejectGoods(Long id, String reason);
}
