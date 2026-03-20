package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.UserSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户会话Mapper接口
 */
@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {
}
