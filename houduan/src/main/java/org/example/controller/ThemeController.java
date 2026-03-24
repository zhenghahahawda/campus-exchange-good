package org.example.controller;

import org.example.common.Result;
import org.example.dto.ThemeRequest;
import org.example.dto.ThemeResponse;
import org.example.service.CosService;
import org.example.service.ThemeService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private CosService cosService;

    @GetMapping
    public Result<List<ThemeResponse>> getActiveThemes() {
        return Result.success(themeService.getActiveThemes());
    }

    @GetMapping("/all")
    public Result<List<ThemeResponse>> getAllThemes() {
        return Result.success(themeService.getAllThemes());
    }

    @GetMapping("/{themeId}")
    public Result<ThemeResponse> getThemeById(@PathVariable Integer themeId) {
        return Result.success(themeService.getThemeById(themeId));
    }

    @PostMapping
    public Result<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest request, HttpServletRequest httpRequest) {
        Integer userId = (Integer) httpRequest.getAttribute("userId");
        return Result.success("创建成功", themeService.createTheme(request, userId));
    }

    @PutMapping("/{themeId}")
    public Result<ThemeResponse> updateTheme(@PathVariable Integer themeId, @Valid @RequestBody ThemeRequest request) {
        return Result.success("更新成功", themeService.updateTheme(themeId, request));
    }

    @DeleteMapping("/{themeId}")
    public Result<Void> deleteTheme(@PathVariable Integer themeId) {
        themeService.deleteTheme(themeId);
        return Result.success(null);
    }

    @PostMapping("/{themeId}/toggle")
    public Result<ThemeResponse> toggleTheme(@PathVariable Integer themeId) {
        return Result.success(themeService.toggleTheme(themeId));
    }

    @PostMapping("/upload/wallpaper")
    public Result<String> uploadWallpaper(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadThemeFile(file, "wallpapers");
        return Result.success("上传成功", url);
    }

    @PostMapping("/upload/preview")
    public Result<String> uploadPreview(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadThemeFile(file, "previews");
        return Result.success("上传成功", url);
    }
}
