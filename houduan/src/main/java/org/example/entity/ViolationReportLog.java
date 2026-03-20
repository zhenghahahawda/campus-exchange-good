package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("violation_report_logs")
public class ViolationReportLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    @TableField("report_id")
    private Integer reportId;

    @TableField("operator_id")
    private Integer operatorId;

    @TableField("action_type")
    private String actionType; // assign/process/approve/reject/comment

    @TableField("action_content")
    private String actionContent;

    @TableField("old_status")
    private String oldStatus;

    @TableField("new_status")
    private String newStatus;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
