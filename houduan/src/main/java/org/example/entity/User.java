package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Integer userId;

    private String username;

    @JsonIgnore
    @TableField("password_hash")
    private String passwordHash;

    private String email;

    private String phone;

    @TableField("user_type")
    private Integer userType; // 1=管理员, 2=普通用户

    private Integer status; // 0=禁用, 1=正常, 2=锁定

    @TableField("credit_score")
    private Integer creditScore;

    @TableField("is_banned")
    private Integer isBanned; // 0=正常, 1=已封禁

    @TableField("banned_until")
    private LocalDateTime bannedUntil;

    @TableField("ban_reason")
    private String banReason;

    @TableField("real_name")
    private String realName;

    @TableField("avatar_url")
    private String avatar;

    private String address;

    private String school;

    private Integer gender; // 0=未知, 1=男, 2=女

    private LocalDate birthday;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("last_login_at")
    private LocalDateTime lastLoginTime;

    @TableField("login_attempts")
    private Integer loginAttempts;

    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
