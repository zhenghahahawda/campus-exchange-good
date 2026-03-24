package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 主题配置实体类
 */
@Data
@TableName("themes")
public class Theme {

    @TableId(type = IdType.AUTO)
    private Integer themeId;

    private String themeKey;
    private String name;
    private String description;
    private String category;
    private String tags;
    private String gradient;
    private String primaryColor;
    private String sidebarBg;
    private String cssVariables;
    private String wallpaperUrl;
    private String wallpaperType;
    private String previewImageUrl;
    private Integer sortOrder;
    private Integer isActive;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
