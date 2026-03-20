package com.campus.exchange.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 管理员创建用户请求DTO
 */
@Data
public class AdminCreateUserRequest {

    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱（必填）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（必填）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 用户类型：1=管理员，2=普通用户（必填）
     */
    private Integer userType = 2;

    /**
     * 真实姓名
     */
    private String realName;

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
     * 账户状态：0=禁用，1=正常，2=锁定（默认正常）
     */
    private Integer status = 1;
}
