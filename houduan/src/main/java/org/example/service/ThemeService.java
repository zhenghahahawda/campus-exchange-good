package org.example.service;

import org.example.dto.ThemeRequest;
import org.example.dto.ThemeResponse;

import java.util.List;

/**
 * 主题服务接口
 */
public interface ThemeService {

    /**
     * 获取所有启用的主题（普通用户）
     */
    List<ThemeResponse> getActiveThemes();

    /**
     * 获取所有主题（管理员，含禁用）
     */
    List<ThemeResponse> getAllThemes();

    /**
     * 获取单个主题详情
     */
    ThemeResponse getThemeById(Integer themeId);

    /**
     * 创建主题
     */
    ThemeResponse createTheme(ThemeRequest request, Integer createdBy);

    /**
     * 更新主题
     */
    ThemeResponse updateTheme(Integer themeId, ThemeRequest request);

    /**
     * 删除主题
     */
    void deleteTheme(Integer themeId);

    /**
     * 启用/禁用主题
     */
    ThemeResponse toggleTheme(Integer themeId);

    /**
     * 校验并规范化用户主题偏好
     */
    String validateAndNormalizeThemePreference(String themePreference);
}
