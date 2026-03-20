package com.campus.exchange.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

import java.time.LocalDateTime;

/**
 * 管理员更新用户信息请求DTO
 */
@Data
public class AdminUpdateUserRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 学校
     */
    private String school;

    /**
     * 地址
     */
    private String address;

    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 账户状态：0=禁用，1=正常，2=锁定
     */
    private Integer status;

    /**
     * 用户信誉值
     */
    private Integer creditScore;

    /**
     * 账户锁定截止时间
     */
    private LocalDateTime lockedUntil;

    /**
     * 用户类型：1=管理员，2=普通用户
     */
    private Integer userType;
}
