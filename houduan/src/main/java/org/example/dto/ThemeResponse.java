package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主题响应DTO
 */
@Data
@Schema(description = "主题响应")
public class ThemeResponse {

    @Schema(description = "主题ID")
    private Integer themeId;

    @Schema(description = "主题唯一标识")
    private String themeKey;

    @Schema(description = "主题名称")
    private String name;

    @Schema(description = "主题描述")
    private String description;

    @Schema(description = "分类JSON数组")
    private String category;

    @Schema(description = "标签JSON数组")
    private String tags;

    @Schema(description = "预览渐变色")
    private String gradient;

    @Schema(description = "主色")
    private String primaryColor;

    @Schema(description = "侧边栏背景")
    private String sidebarBg;

    @Schema(description = "CSS变量JSON对象")
    private String cssVariables;

    @Schema(description = "壁纸URL")
    private String wallpaperUrl;

    @Schema(description = "壁纸类型")
    private String wallpaperType;

    @Schema(description = "预览图URL")
    private String previewImageUrl;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Integer isActive;

    @Schema(description = "创建人ID")
    private Integer createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
