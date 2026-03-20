package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册响应")
public class RegisterResponse {

    @Schema(description = "用户ID", example = "10000002")
    private Integer userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "注册时间", example = "2026-03-10T10:30:00")
    private String createdAt;
}
