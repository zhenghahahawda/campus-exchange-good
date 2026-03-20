package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper接口
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
