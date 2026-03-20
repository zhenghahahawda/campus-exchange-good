package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("orders")
public class Orders {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long id;

    @TableField("order_number")
    private String orderNo;

    private String status = "pending";

    // 换物方A（买家/发起方）
    @TableField("initiator_id")
    private Long buyerId;

    @TableField(exist = false)
    private String buyerName;

    @TableField(exist = false)
    private String buyerRealName;

    @TableField(exist = false)
    private String buyerAvatar;

    @TableField(exist = false)
    private String buyerPhone;

    @TableField(exist = false)
    private String buyerAddress;

    @TableField(exist = false)
    private String buyerSchool;

    // 换物方A提供的物品
    @TableField("initiator_good_id")
    private Long buyerItemId;

    @TableField(exist = false)
    private String buyerItemName;

    @TableField(exist = false)
    private String buyerItemDescription;

    @TableField(exist = false)
    private String buyerItemImages;

    // 换物方B（卖家/接收方）
    @TableField("receiver_id")
    private Long sellerId;

    @TableField(exist = false)
    private String sellerName;

    @TableField(exist = false)
    private String sellerRealName;

    @TableField(exist = false)
    private String sellerAvatar;

    @TableField(exist = false)
    private String sellerPhone;

    @TableField(exist = false)
    private String sellerAddress;

    @TableField(exist = false)
    private String sellerSchool;

    // 换物方B提供的物品
    @TableField("receiver_good_id")
    private Long sellerItemId;

    @TableField(exist = false)
    private String sellerItemName;

    @TableField(exist = false)
    private String sellerItemDescription;

    @TableField(exist = false)
    private String sellerItemImages;

    // 交换凭证
    @TableField("exchange_code")
    private String exchangeCode;

    // 发起方确认状态（0=未确认，1=已确认）
    @TableField("initiator_confirmed")
    private Integer initiatorConfirmed = 0;

    // 接收方确认状态（0=未确认，1=已确认）
    @TableField("receiver_confirmed")
    private Integer receiverConfirmed = 0;

    // 订单备注
    private String remark;

    // 取消原因
    @TableField("cancel_reason")
    private String cancelReason;

    // 管理员备注
    @TableField("admin_note")
    private String adminNote;

    // 发起方评价
    @TableField("initiator_review")
    private String initiatorReview;

    @TableField("initiator_review_images")
    private String initiatorReviewImages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("initiator_reviewed_at")
    private LocalDateTime initiatorReviewedAt;

    // 接收方评价
    @TableField("receiver_review")
    private String receiverReview;

    @TableField("receiver_review_images")
    private String receiverReviewImages;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("receiver_reviewed_at")
    private LocalDateTime receiverReviewedAt;

    // 时间戳
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_at")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("updated_at")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("completed_at")
    private LocalDateTime completeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("cancelled_at")
    private LocalDateTime cancelTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    // 交换凭证列表（不存在于数据库表中）
    @TableField(exist = false)
    private java.util.List<ExchangeProof> exchangeProofs;
}
