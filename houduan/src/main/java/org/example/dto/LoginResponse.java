package org.example.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Integer userId;
    private String username;
    private Integer userType; // 1=管理员, 2=普通用户
    private Integer status; // 0=封禁, 1=正常, 2=活跃
    private String role; // "user" or "admin"
    private String token;
    private String refreshToken;
    private String avatar;
    private String school;
    private Integer creditScore; // 用户信誉分（1-100）
}
