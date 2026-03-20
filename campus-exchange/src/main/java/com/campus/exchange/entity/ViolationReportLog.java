package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违规举报处理日志实体类
 */
@Data
@TableName("violation_report_logs")
public class ViolationReportLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Integer reportId;

    private Integer operatorId;

    private String actionType;

    private String actionContent;

    private String oldStatus;

    private String newStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
