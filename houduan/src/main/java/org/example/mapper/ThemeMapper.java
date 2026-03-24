package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.Theme;
import org.apache.ibatis.annotations.Mapper;

/**
 * 主题配置 Mapper
 */
@Mapper
public interface ThemeMapper extends BaseMapper<Theme> {
}
