package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录响应")
public class LoginResponse {

    @Schema(description = "用户ID", example = "10000001")
    private Integer userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "用户类型：1=管理员，2=普通用户", example = "1")
    private Integer userType;

    @Schema(description = "用户状态：0=封禁，1=正常，2=活跃", example = "1")
    private Integer status;

    @Schema(description = "访问令牌（JWT）")
    private String token;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌过期时间（秒）", example = "7200")
    private Long expiresIn;
}
