package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志Mapper接口
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}
