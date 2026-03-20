package org.example.dto;

import lombok.Data;
import org.example.entity.Good;
import org.example.entity.User;

import java.time.LocalDateTime;

@Data
public class ExchangeDetailResponse {
    // 订单基本信息
    private String id;
    private Integer orderId;
    private String orderNumber;
    private String status;
    
    // 发起方信息
    private Integer initiatorId;
    private String initiatorName;
    private String initiatorAvatar;
    private Integer initiatorGoodId;
    private String initiatorGoodName;
    private String initiatorGoodImage;
    
    // 接收方信息
    private Integer receiverId;
    private String receiverName;
    private String receiverAvatar;
    private Integer receiverGoodId;
    private String receiverGoodName;
    private String receiverGoodImage;
    
    // 其他信息
    private String message;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 当前用户的角色
    private String userRole; // "initiator" 或 "receiver"
    
    // 兼容旧字段
    private Integer requesterId;
    private Integer sellerId;
    private Integer goodsId;
    
    public void setCompatibleFields() {
        this.requesterId = this.initiatorId;
        this.sellerId = this.receiverId;
        this.goodsId = this.receiverGoodId; // 对方想要的商品
    }
}
