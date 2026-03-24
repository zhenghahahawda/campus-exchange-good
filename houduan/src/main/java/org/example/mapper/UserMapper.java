package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE users SET theme_preference = #{newThemeKey} WHERE theme_preference = #{oldThemeKey}")
    void updateThemePreferenceByOldPreference(@Param("oldThemeKey") String oldThemeKey,
                                               @Param("newThemeKey") String newThemeKey);
}
