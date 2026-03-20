package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违规举报实体类
 */
@Data
@TableName("violation_reports")
public class ViolationReport {

    @TableId(value = "report_id", type = IdType.AUTO)
    private Integer reportId;

    private String reportNumber;

    private String targetType;

    private Integer targetId;

    private String targetName;

    private Integer reporterId;

    private String reportType;

    private String title;

    private String description;

    private String detailContent;

    private String evidenceImages;

    private String status;

    private Integer handlerId;

    private String handleAction;

    private String handleResult;

    private String rejectReason;

    private Integer creditDeduction;

    private Integer banDuration;

    private LocalDateTime banUntil;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime handledAt;

    private LocalDateTime deletedAt;

    /**
     * 举报人用户名（非数据库字段）
     */
    @TableField(exist = false)
    private String reporterName;
}
