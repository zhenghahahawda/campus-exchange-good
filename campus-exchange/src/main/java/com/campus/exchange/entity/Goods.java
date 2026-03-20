package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("goods")
public class Goods {

    @TableId(value = "good_id", type = IdType.AUTO)
    private Long id;

    @TableField("good_name")
    private String name;

    private String description;

    @TableField("category_id")
    private Long categoryId;

    @TableField(exist = false) // 数据库中是category_id，这里需要关联查询或者单独处理，暂时先忽略
    private String category;

    @TableField("condition_level")
    private Integer conditionLevel;

    @TableField(exist = false) // 兼容旧代码，后续建议重构
    private String condition;

    /**
     * 状态：0=待审核，1=已通过，2=已拒绝
     */
    private Integer status;

    /**
     * 上架状态：0=已下架，1=已上架
     */
    @TableField("shelf_status")
    private Integer shelfStatus;

    @TableField("user_id")
    private Long sellerId;

    @TableField(exist = false)
    private String sellerName;

    @TableField(exist = false)
    private String sellerAvatar;

    @TableField(exist = false)
    private String region;

    @TableField(exist = false)
    private String school;

    // 图片信息（JSON格式）
    private String images;

    // 交换凭证
    @TableField("exchange_code")
    private String exchangeCode;

    @TableField(exist = false)
    private String deliveryType = "pickup";

    // 统计数据
    @TableField("view_count")
    private Integer views = 0;

    @TableField("like_count")
    private Integer likes = 0;

    @TableField("favorite_count")
    private Integer favorites = 0;

    // 审核信息
    @TableField("reject_reason")
    private String rejectReason;

    @TableField("auditor_id")
    private Long auditorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("audited_at")
    private LocalDateTime auditTime;

    // 时间戳
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_at")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("updated_at")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("sold_at")
    private LocalDateTime soldTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
