package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 用户注册请求DTO
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @Schema(description = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含中文、字母、数字和下划线")
    private String username;

    @Schema(description = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Schema(description = "邮箱", required = true, example = "zhangsan@example.com")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "手机号（中国大陆）", required = true, example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确，仅支持中国大陆手机号")
    private String phone;

    @Schema(description = "学校名称", required = true, example = "清华大学")
    @NotBlank(message = "学校名称不能为空")
    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    private String school;

    @Schema(description = "性别：1=男，2=女", required = true, example = "1")
    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值必须为1（男）或2（女）")
    @Max(value = 2, message = "性别值必须为1（男）或2（女）")
    private Integer gender;
}
