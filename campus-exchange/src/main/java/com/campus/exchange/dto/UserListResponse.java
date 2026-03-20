package com.campus.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户列表响应DTO（用于管理后台）
 * 包含用户基本信息、发布商品数量和交易订单数量
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
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
     * 用户信誉值
     */
    private Integer creditScore;

    /**
     * 是否被封禁
     */
    private Integer isBanned;

    /**
     * 封禁截止时间
     */
    private LocalDateTime bannedUntil;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 账户锁定截止时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 地址
     */
    private String address;

    /**
     * 学校
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
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 用户发布的商品数量
     */
    private Long goodsCount;

    /**
     * 用户参与的订单数量（作为买家或卖家）
     */
    private Long orderCount;
}
