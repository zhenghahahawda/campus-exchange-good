package com.campus.exchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.exchange.entity.ViolationReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规举报Mapper接口
 */
@Mapper
public interface ViolationReportMapper extends BaseMapper<ViolationReport> {
}
