package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.*;
import com.campus.exchange.service.UserService;
import com.campus.exchange.util.Result;
import com.campus.exchange.vo.BaseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名、邮箱或手机号登录")
    public BaseResult<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = userService.login(request, httpRequest);
        return BaseResult.ok("登录成功", response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户（普通用户）")
    public BaseResult<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.register(request);
        return BaseResult.ok("注册成功", response);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public BaseResult<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        UserInfoResponse response = userService.getUserInfo(userId);
        return BaseResult.ok("获取成功", response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    public BaseResult<UserInfoResponse> updateUserInfo(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserInfoRequest updateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        UserInfoResponse response = userService.updateUserInfo(userId, updateRequest);
        return BaseResult.ok("更新成功", response);
    }

    /**
     * 验证会话
     */
    @GetMapping("/verify-session")
    @Operation(summary = "验证会话", description = "验证当前用户会话是否有效")
    public BaseResult<SessionVerifyResponse> verifySession(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        SessionVerifyResponse response = userService.verifySession(userId, username);
        return BaseResult.ok("验证成功", response);
    }

    /**
     * 获取所有用户列表（管理员功能）
     */
    @GetMapping("/list")
    @RequireAdmin
    @Operation(summary = "获取所有用户列表", description = "获取系统中所有用户的列表（管理员功能）")
    public Result<List<UserListResponse>> getAllUsers() {
        List<UserListResponse> users = userService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 管理员创建用户
     */
    @PostMapping("/admin/create")
    @RequireAdmin
    @Operation(summary = "管理员创建用户", description = "管理员可以创建任意类型的用户（管理员功能）")
    public Result<UserListResponse> adminCreateUser(@Valid @RequestBody AdminCreateUserRequest createRequest) {
        UserListResponse response = userService.adminCreateUser(createRequest);
        return Result.success(response);
    }

    /**
     * 管理员更新用户信息
     */
    @PutMapping("/admin/{userId}")
    @RequireAdmin
    @Operation(summary = "管理员更新用户信息", description = "管理员可以更新任意用户的信息（管理员功能）")
    public Result<UserListResponse> adminUpdateUser(
            @PathVariable("userId") Integer targetUserId,
            @Valid @RequestBody AdminUpdateUserRequest updateRequest) {
        UserListResponse response = userService.adminUpdateUser(targetUserId, updateRequest);
        return Result.success(response);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像图片，支持jpg/png/gif，最大5MB")
    public BaseResult<String> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("avatar") MultipartFile file) {
        Integer userId = (Integer) request.getAttribute("userId");
        String avatarUrl = userService.uploadAvatar(userId, file);
        return BaseResult.ok("头像上传成功", avatarUrl);
    }

    /**
     * 管理员上传指定用户的头像
     */
    @PostMapping("/admin/{userId}/avatar")
    @RequireAdmin
    @Operation(summary = "管理员上传用户头像", description = "管理员为指定用户上传头像")
    public BaseResult<String> adminUploadAvatar(
            @PathVariable("userId") Integer targetUserId,
            @RequestParam("avatar") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(targetUserId, file);
        return BaseResult.ok("头像上传成功", avatarUrl);
    }

    /**
     * 管理员删除用户
     */
    @DeleteMapping("/admin/{userId}")
    @RequireAdmin
    @Operation(summary = "管理员删除用户", description = "管理员删除指定用户（物理删除）")
    public Result<Void> adminDeleteUser(@PathVariable("userId") Integer userId) {
        userService.adminDeleteUser(userId);
        return Result.success(null);
    }

    /**
     * 管理员重置用户密码
     */
    @PostMapping("/admin/{userId}/reset-password")
    @RequireAdmin
    @Operation(summary = "管理员重置密码", description = "管理员重置指定用户密码为123456")
    public Result<Void> adminResetPassword(@PathVariable("userId") Integer userId) {
        userService.adminResetPassword(userId);
        return Result.success(null);
    }
}
