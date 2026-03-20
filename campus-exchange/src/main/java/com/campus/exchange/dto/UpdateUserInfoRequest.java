package com.campus.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 用户信息更新请求
 */
@Data
@Schema(description = "用户信息更新请求")
public class UpdateUserInfoRequest {

    @Schema(description = "用户名", example = "zhangsan")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @Schema(description = "真实姓名", example = "张三")
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @Schema(description = "学校名称", example = "清华大学")
    @Size(max = 100, message = "学校名称不能超过100个字符")
    private String school;

    @Schema(description = "地址", example = "北京市海淀区")
    @Size(max = 200, message = "地址不能超过200个字符")
    private String address;

    @Schema(description = "性别：0=未知，1=男，2=女", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "2000-01-01")
    private LocalDate birthday;
}
