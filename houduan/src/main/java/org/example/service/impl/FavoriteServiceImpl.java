package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.Result;
import org.example.entity.Favorite;
import org.example.entity.Good;
import org.example.entity.User;
import org.example.mapper.FavoriteMapper;
import org.example.mapper.GoodMapper;
import org.example.mapper.UserMapper;
import org.example.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<List<Good>> getUserFavorites(Integer userId) {
        System.out.println("=== 获取用户收藏列表 userId=" + userId + " ===");
        
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);
        
        System.out.println("查询到 " + favorites.size() + " 条收藏记录");

        List<Good> goods = new ArrayList<>();
        for (Favorite favorite : favorites) {
            System.out.println("处理收藏记录: favoriteId=" + favorite.getFavoriteId() + ", goodsId=" + favorite.getGoodsId());
            
            Good good = goodMapper.selectById(favorite.getGoodsId());
            if (good != null) {
                System.out.println("找到商品: goodId=" + good.getGoodId() + ", goodName=" + good.getGoodName());
                
                good.setExchanged(good.getSoldAt() != null);
                System.out.println("设置 exchanged=" + good.getExchanged());
                
                User seller = userMapper.selectById(good.getUserId());
                if (seller != null) {
                    seller.setPasswordHash(null);
                    good.setSeller(seller);
                    System.out.println("设置卖家信息: sellerId=" + seller.getUserId() + ", username=" + seller.getUsername());
                }
                goods.add(good);
            } else {
                System.out.println("警告：商品不存在 goodsId=" + favorite.getGoodsId());
            }
        }
        
        System.out.println("最终返回 " + goods.size() + " 个商品");
        System.out.println("=== 收藏列表处理完成 ===");
        
        return Result.success(goods);
    }

    @Override
    public Result<String> addFavorite(Integer userId, Integer goodsId) {
        // Check if already favorited
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId);
        
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return Result.success("Already favorited", null);
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favoriteMapper.insert(favorite);

        // Update favorite count
        Good good = goodMapper.selectById(goodsId);
        if (good != null) {
            good.setFavoriteCount(good.getFavoriteCount() + 1);
            goodMapper.updateById(good);
        }

        return Result.success("Favorited successfully", null);
    }

    @Override
    public Result<String> removeFavorite(Integer userId, Integer goodsId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId);
        
        int deleted = favoriteMapper.delete(wrapper);
        
        if (deleted > 0) {
            // Update favorite count
            Good good = goodMapper.selectById(goodsId);
            if (good != null && good.getFavoriteCount() > 0) {
                good.setFavoriteCount(good.getFavoriteCount() - 1);
                goodMapper.updateById(good);
            }
        }

        return Result.success("Unfavorited successfully", null);
    }
}
