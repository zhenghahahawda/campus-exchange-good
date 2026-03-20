package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserInfoResponse {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户类型：1=管理员，2=普通用户")
    private Integer userType;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "学校")
    private String school;

    @Schema(description = "性别：0=未知，1=男，2=女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "注册时间")
    private LocalDateTime registerTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
}
