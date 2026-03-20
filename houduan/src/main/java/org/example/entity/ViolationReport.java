package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "violation_reports", autoResultMap = true)
public class ViolationReport implements Serializable {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Integer reportId;

    @TableField("report_number")
    private String reportNumber;

    @TableField("target_type")
    private String targetType; // product / user

    @TableField("target_id")
    private Integer targetId;

    @TableField("target_name")
    private String targetName;

    @TableField("reporter_id")
    private Integer reporterId;

    @TableField("report_type")
    private String reportType;

    private String title;

    private String description;

    @TableField("detail_content")
    private String detailContent;

    @TableField(value = "evidence_images", typeHandler = JacksonTypeHandler.class)
    private List<String> evidenceImages;

    private String status; // pending/processing/approved/rejected

    @TableField("handler_id")
    private Integer handlerId;

    @TableField("handle_action")
    private String handleAction; // warning/tempBan/permBan/removeProduct

    @TableField("handle_result")
    private String handleResult;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("credit_deduction")
    private Integer creditDeduction;

    @TableField("ban_duration")
    private Integer banDuration;

    @TableField("ban_until")
    private LocalDateTime banUntil;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
