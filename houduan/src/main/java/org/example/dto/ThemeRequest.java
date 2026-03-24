package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * 主题创建/更新请求DTO
 */
@Data
@Schema(description = "主题请求")
public class ThemeRequest {

    @Schema(description = "主题唯一标识", example = "dark-gold")
    @NotBlank(message = "主题标识不能为空")
    @Size(max = 50, message = "主题标识不能超过50个字符")
    private String themeKey;

    @Schema(description = "主题名称", example = "暗黑金主题")
    @NotBlank(message = "主题名称不能为空")
    @Size(max = 100, message = "主题名称不能超过100个字符")
    private String name;

    @Schema(description = "主题描述")
    @Size(max = 255, message = "描述不能超过255个字符")
    private String description;

    @Schema(description = "分类JSON数组", example = "[\"dark\",\"luxury\"]")
    private String category;

    @Schema(description = "标签JSON数组", example = "[\"深色\",\"奢华\"]")
    private String tags;

    @Schema(description = "预览渐变色")
    private String gradient;

    @Schema(description = "主色", example = "#D4AF37")
    private String primaryColor;

    @Schema(description = "侧边栏背景")
    private String sidebarBg;

    @Schema(description = "CSS变量JSON对象")
    @NotBlank(message = "CSS变量不能为空")
    private String cssVariables;

    @Schema(description = "壁纸URL(COS)")
    private String wallpaperUrl;

    @Schema(description = "壁纸类型: video/image")
    private String wallpaperType;

    @Schema(description = "预览图URL(COS)")
    private String previewImageUrl;

    @Schema(description = "排序")
    private Integer sortOrder;
}
