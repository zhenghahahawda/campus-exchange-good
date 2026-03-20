package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "orders", autoResultMap = true)
public class Exchange implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    @TableField(value = "order_number")
    private String orderNumber;

    private String status;

    @TableField("initiator_id")
    private Integer initiatorId;

    @TableField("initiator_good_id")
    private Integer initiatorGoodId;

    @TableField("receiver_id")
    private Integer receiverId;

    @TableField("receiver_good_id")
    private Integer receiverGoodId;

    @TableField("exchange_code")
    private String exchangeCode;

    @TableField("initiator_confirmed")
    private Integer initiatorConfirmed; // 0=未确认, 1=已确认

    @TableField("receiver_confirmed")
    private Integer receiverConfirmed; // 0=未确认, 1=已确认

    private String remark;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("admin_note")
    private String adminNote;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
    
    @TableField("conversation_id")
    private String conversationId;

    @TableField("initiator_review")
    private String initiatorReview;

    @TableField("initiator_review_images")
    private String initiatorReviewImages;

    @TableField("initiator_reviewed_at")
    private LocalDateTime initiatorReviewedAt;

    @TableField("receiver_review")
    private String receiverReview;

    @TableField("receiver_review_images")
    private String receiverReviewImages;

    @TableField("receiver_reviewed_at")
    private LocalDateTime receiverReviewedAt;

    @TableField(exist = false)
    private String appealReason;

    @TableField(exist = false)
    private String appealImages;

    @TableField(exist = false)
    private Integer appealUserId;

    @TableField(exist = false)
    private LocalDateTime appealedAt;

    // 保留旧字段作为兼容（标记为不存在于数据库）
    @TableField(exist = false)
    private String message;
    
    // 兼容旧代码的方法
    public String getId() {
        return orderId != null ? String.valueOf(orderId) : null;
    }
    
    public void setId(String id) {
        this.orderId = id != null ? Integer.valueOf(id) : null;
    }
    
    public Integer getRequesterId() {
        return initiatorId;
    }
    
    public void setRequesterId(Integer requesterId) {
        this.initiatorId = requesterId;
    }
    
    public Integer getSellerId() {
        return receiverId;
    }
    
    public void setSellerId(Integer sellerId) {
        this.receiverId = sellerId;
    }
    
    public Integer getGoodsId() {
        return initiatorGoodId;
    }
    
    public void setGoodsId(Integer goodsId) {
        this.initiatorGoodId = goodsId;
    }
    
    // 添加 message 字段的 getter，从 remark 字段获取
    public String getMessage() {
        return remark;
    }
    
    public void setMessage(String message) {
        this.remark = message;
    }
    
    public List<String> getOfferItems() {
        return null; // 旧字段，不再使用
    }
    
    public void setOfferItems(List<String> offerItems) {
        // 旧字段，不再使用
    }
}
