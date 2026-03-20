package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_cancellations")
public class OrderCancellation {

    @TableId(value = "cancellation_id", type = IdType.AUTO)
    private Integer cancellationId;

    @TableField("order_id")
    private Integer orderId;

    @TableField("cancelled_by")
    private Integer cancelledBy;

    @TableField("cancelled_by_role")
    private String cancelledByRole;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("cancel_type")
    private Integer cancelType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;
}
