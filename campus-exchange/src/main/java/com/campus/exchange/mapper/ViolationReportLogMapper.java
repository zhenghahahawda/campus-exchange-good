package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.ViolationReportLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规举报处理日志Mapper接口
 */
@Mapper
public interface ViolationReportLogMapper extends BaseMapper<ViolationReportLog> {
}
