package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.ThemeRequest;
import com.campus.exchange.dto.ThemeResponse;
import com.campus.exchange.service.CosService;
import com.campus.exchange.service.ThemeService;
import com.campus.exchange.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 主题管理控制器
 */
@RestController
@RequestMapping("/api/themes")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "主题管理")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private CosService cosService;

    /**
     * 获取所有启用的主题（普通用户）
     */
    @GetMapping
    @Operation(summary = "获取启用主题列表", description = "获取所有启用的主题，供用户选择")
    public Result<List<ThemeResponse>> getActiveThemes() {
        return Result.success(themeService.getActiveThemes());
    }

    /**
     * 获取所有主题（管理员，含禁用）
     */
    @GetMapping("/all")
    @RequireAdmin
    @Operation(summary = "获取全部主题列表", description = "管理员获取所有主题（含禁用）")
    public Result<List<ThemeResponse>> getAllThemes() {
        return Result.success(themeService.getAllThemes());
    }

    /**
     * 获取单个主题详情
     */
    @GetMapping("/{themeId}")
    @RequireAdmin
    @Operation(summary = "获取主题详情")
    public Result<ThemeResponse> getThemeById(@PathVariable Integer themeId) {
        return Result.success(themeService.getThemeById(themeId));
    }

    /**
     * 创建主题
     */
    @PostMapping
    @RequireAdmin
    @Operation(summary = "创建主题")
    public Result<ThemeResponse> createTheme(
            @Valid @RequestBody ThemeRequest request,
            HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        return Result.success("创建成功", themeService.createTheme(request, userId));
    }

    /**
     * 更新主题
     */
    @PutMapping("/{themeId}")
    @RequireAdmin
    @Operation(summary = "更新主题")
    public Result<ThemeResponse> updateTheme(
            @PathVariable Integer themeId,
            @Valid @RequestBody ThemeRequest request) {
        return Result.success("更新成功", themeService.updateTheme(themeId, request));
    }

    /**
     * 删除主题
     */
    @DeleteMapping("/{themeId}")
    @RequireAdmin
    @Operation(summary = "删除主题")
    public Result<Void> deleteTheme(@PathVariable Integer themeId) {
        themeService.deleteTheme(themeId);
        return Result.success(null);
    }

    /**
     * 启用/禁用主题
     */
    @PostMapping("/{themeId}/toggle")
    @RequireAdmin
    @Operation(summary = "启用/禁用主题")
    public Result<ThemeResponse> toggleTheme(@PathVariable Integer themeId) {
        return Result.success(themeService.toggleTheme(themeId));
    }

    /**
     * 上传主题壁纸
     */
    @PostMapping("/upload/wallpaper")
    @RequireAdmin
    @Operation(summary = "上传主题壁纸", description = "支持图片(jpg/png/gif,5MB)和视频(mp4,50MB)")
    public Result<String> uploadWallpaper(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadThemeFile(file, "wallpapers");
        return Result.success("上传成功", url);
    }

    /**
     * 上传主题预览图
     */
    @PostMapping("/upload/preview")
    @RequireAdmin
    @Operation(summary = "上传主题预览图")
    public Result<String> uploadPreview(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadThemeFile(file, "previews");
        return Result.success("上传成功", url);
    }
}
