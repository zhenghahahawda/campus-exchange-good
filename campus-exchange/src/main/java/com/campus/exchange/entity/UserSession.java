package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户会话实体类
 */
@Data
@TableName("user_sessions")
public class UserSession {

    @TableId(value = "session_id", type = IdType.INPUT)
    private String sessionId;

    private Integer userId;

    private String token;

    private String refreshToken;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime lastActivityAt;

    private String ipAddress;

    private String userAgent;

    private String deviceId;

    private Integer isActive;
}
