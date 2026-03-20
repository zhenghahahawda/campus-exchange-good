package com.campus.exchange.service;

import com.campus.exchange.dto.*;
import com.campus.exchange.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * @param request 登录请求
     * @param httpRequest HTTP请求（用于获取IP、User-Agent等）
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册响应
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoResponse getUserInfo(Integer userId);

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserInfoResponse updateUserInfo(Integer userId, UpdateUserInfoRequest request);

    /**
     * 验证会话
     * @param userId 用户ID
     * @param username 用户名
     * @return 会话验证响应
     */
    SessionVerifyResponse verifySession(Integer userId, String username);

    /**
     * 获取所有用户列表（包含商品数量和订单数量）
     * @return 用户列表
     */
    List<UserListResponse> getAllUsers();

    /**
     * 管理员创建用户
     * @param request 创建用户请求
     * @return 创建的用户信息
     */
    UserListResponse adminCreateUser(AdminCreateUserRequest request);

    /**
     * 管理员更新用户信息
     * @param targetUserId 目标用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserListResponse adminUpdateUser(Integer targetUserId, AdminUpdateUserRequest request);

    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像URL
     */
    String uploadAvatar(Integer userId, MultipartFile file);

    /**
     * 管理员删除用户
     * @param userId 用户ID
     */
    void adminDeleteUser(Integer userId);

    /**
     * 管理员重置用户密码
     * @param userId 用户ID
     */
    void adminResetPassword(Integer userId);
}
