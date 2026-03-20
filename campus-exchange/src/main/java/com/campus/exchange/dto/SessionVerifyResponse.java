package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话验证响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话验证响应")
public class SessionVerifyResponse {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "会话是否有效")
    private Boolean sessionValid;
}
