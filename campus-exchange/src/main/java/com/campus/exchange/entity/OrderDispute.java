package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_disputes")
public class OrderDispute {

    @TableId(value = "dispute_id", type = IdType.AUTO)
    private Integer disputeId;

    @TableField("order_id")
    private Integer orderId;

    @TableField("reporter_id")
    private Integer reporterId;

    @TableField("reporter_role")
    private String reporterRole;

    private String reason;

    private String description;

    private String images;

    private Integer status;

    @TableField("handler_id")
    private Integer handlerId;

    @TableField("handle_result")
    private String handleResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("handled_at")
    private LocalDateTime handledAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
