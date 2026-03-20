package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "goods", autoResultMap = true)
public class Good implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "good_id", type = IdType.AUTO)
    private Integer goodId;

    @TableField("good_name")
    private String goodName;

    private String description;

    @TableField("exchange_for")
    private String exchangeFor; // 期望交换的物品描述

    @TableField("category_id")
    private Integer categoryId;

    @TableField("condition_level")
    private Integer conditionLevel; // 1=全新, 2=几乎全新, 3=良好, 4=一般

    private Integer status; // 0=待审核, 1=已通过, 2=已拒绝

    @TableField("shelf_status")
    private Integer shelfStatus; // 0=已下架, 1=已上架

    @TableField("user_id")
    private Integer userId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    @TableField("exchange_code")
    private String exchangeCode;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("auditor_id")
    private Integer auditorId;

    @TableField("audited_at")
    private LocalDateTime auditedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("sold_at")
    private LocalDateTime soldAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    // Transient fields for response
    @TableField(exist = false)
    private User seller;

    @TableField(exist = false)
    private Boolean exchanged; // 是否已交换

    @TableField(exist = false)
    private Boolean isLiked; // 当前用户是否已点赞

    // Getter and Setter for seller
    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    // Getter and Setter for exchanged
    public Boolean getExchanged() {
        return exchanged;
    }

    public void setExchanged(Boolean exchanged) {
        this.exchanged = exchanged;
    }
}
