package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.UserLoginLog;

@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}