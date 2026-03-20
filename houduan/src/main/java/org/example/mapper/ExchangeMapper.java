package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.example.entity.Exchange;

@Mapper
public interface ExchangeMapper extends BaseMapper<Exchange> {
    
    @Insert("INSERT INTO orders (order_number, status, initiator_id, initiator_good_id, receiver_id, receiver_good_id, remark, created_at, updated_at) " +
            "VALUES (#{orderNumber}, #{status}, #{initiatorId}, #{initiatorGoodId}, #{receiverId}, #{receiverGoodId}, #{remark}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId", keyColumn = "order_id")
    int insertExchange(Exchange exchange);
}
