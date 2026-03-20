package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 查询订单详情（关联用户和商品信息）
     */
    Orders selectOrderDetail(@Param("orderId") Long orderId);

    /**
     * 分页查询订单列表（关联用户和商品信息）
     */
    IPage<Orders> selectOrdersWithDetails(Page<Orders> page,
                                          @Param("status") String status,
                                          @Param("keyword") String keyword);
}
