package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exchange_proofs")
public class ExchangeProof {

    @TableId(value = "proof_id", type = IdType.AUTO)
    private Integer proofId;

    @TableField("order_id")
    private Integer orderId;

    @TableField("user_id")
    private Integer userId;

    @TableField("user_role")
    private String userRole;

    private Integer status;

    private String images;

    private String comment;

    @TableField("verified_by")
    private Integer verifiedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("verified_at")
    private LocalDateTime verifiedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("uploaded_at")
    private LocalDateTime uploadedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
