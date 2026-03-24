package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.dto.ThemeRequest;
import org.example.dto.ThemeResponse;
import org.example.entity.Theme;
import org.example.mapper.ThemeMapper;
import org.example.mapper.UserMapper;
import org.example.service.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主题服务实现类
 */
@Service
public class ThemeServiceImpl implements ThemeService {

    private static final String DEFAULT_THEME_KEY = "ios-style";
    private static final String LEGACY_DEFAULT_THEME_KEY = "default";

    @Autowired
    private ThemeMapper themeMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @SuppressWarnings("unchecked")
    public List<ThemeResponse> getActiveThemes() {
        LambdaQueryWrapper<Theme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Theme::getIsActive, 1).orderByAsc(Theme::getSortOrder);
        return themeMapper.selectList(wrapper)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ThemeResponse> getAllThemes() {
        LambdaQueryWrapper<Theme> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Theme::getSortOrder);
        return themeMapper.selectList(wrapper)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ThemeResponse getThemeById(Integer themeId) {
        Theme theme = themeMapper.selectById(themeId);
        if (theme == null) {
            throw new RuntimeException("主题不存在");
        }
        return toResponse(theme);
    }

    @Override
    public ThemeResponse createTheme(ThemeRequest request, Integer createdBy) {
        String normalizedThemeKey = normalizeThemeKey(request.getThemeKey());

        LambdaQueryWrapper<Theme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Theme::getThemeKey, normalizedThemeKey);
        if (themeMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("主题标识已存在");
        }

        Theme theme = new Theme();
        copyFromRequest(theme, request, normalizedThemeKey);
        theme.setIsActive(1);
        theme.setCreatedBy(createdBy);
        theme.setCreatedAt(LocalDateTime.now());
        theme.setUpdatedAt(LocalDateTime.now());
        themeMapper.insert(theme);

        return toResponse(theme);
    }

    @Override
    public ThemeResponse updateTheme(Integer themeId, ThemeRequest request) {
        Theme theme = themeMapper.selectById(themeId);
        if (theme == null) {
            throw new RuntimeException("主题不存在");
        }

        String normalizedThemeKey = normalizeThemeKey(request.getThemeKey());

        if (!theme.getThemeKey().equals(normalizedThemeKey)) {
            LambdaQueryWrapper<Theme> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Theme::getThemeKey, normalizedThemeKey);
            if (themeMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("主题标识已存在");
            }
        }

        copyFromRequest(theme, request, normalizedThemeKey);
        theme.setUpdatedAt(LocalDateTime.now());
        themeMapper.updateById(theme);

        return toResponse(theme);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTheme(Integer themeId) {
        Theme theme = themeMapper.selectById(themeId);
        if (theme == null) {
            throw new RuntimeException("主题不存在");
        }
        if (isProtectedTheme(theme.getThemeKey())) {
            throw new RuntimeException("默认主题不能删除");
        }

        String fallbackThemeKey = resolveFallbackThemeKey(theme.getThemeKey());
        userMapper.updateThemePreferenceByOldPreference(theme.getThemeKey(), fallbackThemeKey);
        themeMapper.deleteById(themeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThemeResponse toggleTheme(Integer themeId) {
        Theme theme = themeMapper.selectById(themeId);
        if (theme == null) {
            throw new RuntimeException("主题不存在");
        }
        if (isProtectedTheme(theme.getThemeKey())) {
            throw new RuntimeException("默认主题不能禁用");
        }

        int nextStatus = theme.getIsActive() == 1 ? 0 : 1;
        if (nextStatus == 0) {
            String fallbackThemeKey = resolveFallbackThemeKey(theme.getThemeKey());
            userMapper.updateThemePreferenceByOldPreference(theme.getThemeKey(), fallbackThemeKey);
        }

        theme.setIsActive(nextStatus);
        theme.setUpdatedAt(LocalDateTime.now());
        themeMapper.updateById(theme);
        return toResponse(theme);
    }

    @Override
    public String validateAndNormalizeThemePreference(String themePreference) {
        String normalizedThemeKey = normalizeThemeKey(themePreference);
        Theme theme = getActiveThemeByKey(normalizedThemeKey);
        if (theme == null) {
            throw new RuntimeException("主题不存在或已停用");
        }
        return theme.getThemeKey();
    }

    private void copyFromRequest(Theme theme, ThemeRequest request, String normalizedThemeKey) {
        theme.setThemeKey(normalizedThemeKey);
        theme.setName(request.getName());
        theme.setDescription(request.getDescription());
        theme.setCategory(request.getCategory());
        theme.setTags(request.getTags());
        theme.setGradient(request.getGradient());
        theme.setPrimaryColor(request.getPrimaryColor());
        theme.setSidebarBg(request.getSidebarBg());
        theme.setCssVariables(request.getCssVariables());
        theme.setWallpaperUrl(request.getWallpaperUrl());
        theme.setWallpaperType(request.getWallpaperType());
        theme.setPreviewImageUrl(request.getPreviewImageUrl());
        if (request.getSortOrder() != null) {
            theme.setSortOrder(request.getSortOrder());
        }
    }

    private boolean isProtectedTheme(String themeKey) {
        return DEFAULT_THEME_KEY.equals(themeKey) || LEGACY_DEFAULT_THEME_KEY.equals(themeKey);
    }

    private String normalizeThemeKey(String themeKey) {
        if (themeKey == null || themeKey.trim().isEmpty() || LEGACY_DEFAULT_THEME_KEY.equals(themeKey)) {
            return DEFAULT_THEME_KEY;
        }
        return themeKey;
    }

    private Theme getActiveThemeByKey(String themeKey) {
        LambdaQueryWrapper<Theme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Theme::getThemeKey, themeKey)
                .eq(Theme::getIsActive, 1)
                .last("LIMIT 1");
        return themeMapper.selectOne(wrapper);
    }

    private String resolveFallbackThemeKey(String excludedThemeKey) {
        Theme iosTheme = getActiveThemeByKey(DEFAULT_THEME_KEY);
        if (iosTheme != null && !iosTheme.getThemeKey().equals(excludedThemeKey)) {
            return iosTheme.getThemeKey();
        }

        Theme legacyDefaultTheme = getActiveThemeByKey(LEGACY_DEFAULT_THEME_KEY);
        if (legacyDefaultTheme != null && !legacyDefaultTheme.getThemeKey().equals(excludedThemeKey)) {
            return legacyDefaultTheme.getThemeKey();
        }

        LambdaQueryWrapper<Theme> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Theme::getIsActive, 1)
                .ne(Theme::getThemeKey, excludedThemeKey)
                .orderByAsc(Theme::getSortOrder)
                .orderByAsc(Theme::getThemeId)
                .last("LIMIT 1");
        Theme fallbackTheme = themeMapper.selectOne(wrapper);
        if (fallbackTheme == null) {
            throw new RuntimeException("至少需要保留一个启用主题");
        }
        return fallbackTheme.getThemeKey();
    }

    private ThemeResponse toResponse(Theme theme) {
        ThemeResponse resp = new ThemeResponse();
        resp.setThemeId(theme.getThemeId());
        resp.setThemeKey(theme.getThemeKey());
        resp.setName(theme.getName());
        resp.setDescription(theme.getDescription());
        resp.setCategory(theme.getCategory());
        resp.setTags(theme.getTags());
        resp.setGradient(theme.getGradient());
        resp.setPrimaryColor(theme.getPrimaryColor());
        resp.setSidebarBg(theme.getSidebarBg());
        resp.setCssVariables(theme.getCssVariables());
        resp.setWallpaperUrl(theme.getWallpaperUrl());
        resp.setWallpaperType(theme.getWallpaperType());
        resp.setPreviewImageUrl(theme.getPreviewImageUrl());
        resp.setSortOrder(theme.getSortOrder());
        resp.setIsActive(theme.getIsActive());
        resp.setCreatedBy(theme.getCreatedBy());
        resp.setCreatedAt(theme.getCreatedAt());
        resp.setUpdatedAt(theme.getUpdatedAt());
        return resp;
    }
}
