package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @Schema(description = "登录账号（用户名/邮箱/手机号）", required = true, example = "admin")
    @NotBlank(message = "登录账号不能为空")
    private String account;

    @Schema(description = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
