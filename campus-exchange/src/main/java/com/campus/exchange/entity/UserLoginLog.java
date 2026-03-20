package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 */
@Data
@TableName("user_login_logs")
public class UserLoginLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Integer userId;

    private LocalDateTime loginTime;

    private String loginIp;

    private Integer loginStatus;

    private String failReason;

    private String userAgent;

    private String deviceType;

    private String browser;

    private String os;
}
