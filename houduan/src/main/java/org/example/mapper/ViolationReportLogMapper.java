package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.ViolationReportLog;

@Mapper
public interface ViolationReportLogMapper extends BaseMapper<ViolationReportLog> {
}
