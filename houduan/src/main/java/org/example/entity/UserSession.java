package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_sessions")
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "session_id", type = IdType.ASSIGN_UUID)
    private String sessionId;

    @TableField("user_id")
    private Integer userId;

    private String token;

    @TableField("refresh_token")
    private String refreshToken;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField("expires_at")
    private LocalDateTime expiresAt;

    @TableField(value = "last_activity_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastActivityAt;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("device_id")
    private String deviceId;

    @TableField("is_active")
    private Integer isActive; // 0=已失效, 1=活跃
}
