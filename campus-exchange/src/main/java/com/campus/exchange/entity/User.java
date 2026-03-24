package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {

    /**
     * 用户唯一ID，8位数字（10000000-99999999）
     */
    @TableId(type = IdType.INPUT)
    private Integer userId;

    /**
     * 用户名，用于登录，3-50字符
     */
    private String username;

    /**
     * 密码哈希值（bcrypt加密）
     */
    private String passwordHash;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户类型：1=管理员，2=普通用户
     */
    private Integer userType;

    /**
     * 账户状态：0=禁用，1=正常，2=锁定
     */
    private Integer status;

    /**
     * 用户信誉值（1-100，默认80）
     */
    private Integer creditScore = 80;

    /**
     * 是否被封禁：0=正常，1=已封禁
     */
    private Integer isBanned = 0;

    /**
     * 封禁截止时间
     */
    private LocalDateTime bannedUntil;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL地址
     */
    private String avatarUrl;

    /**
     * 用户地址
     */
    private String address;

    /**
     * 学校名称
     */
    private String school;

    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 账户创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 连续登录失败次数
     */
    private Integer loginAttempts;

    /**
     * 账户锁定截止时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 软删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 用户主题偏好
     */
    private String themePreference = "ios-style";
}
