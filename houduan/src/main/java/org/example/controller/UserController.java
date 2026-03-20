package org.example.controller;

import org.example.common.Result;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.UserProfileResponse;
import org.example.dto.UserRegisterDTO;
import org.example.entity.User;
import org.example.service.CosService;
import org.example.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CosService cosService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return userService.login(request.getAccount(), request.getPassword(), httpRequest);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        return userService.register(user, dto.getPassword());
    }
    
    // 查看任意用户公开主页（不需要登录）
    @GetMapping("/{userId}")
    public Result<UserProfileResponse> getUserProfile(@PathVariable Integer userId) {
        return userService.getUserProfile(userId);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return userService.getUserInfo(userId);
    }

    @PutMapping("/info")
    public Result<String> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return userService.updateUserInfo(userId, user);
    }

    // 管理员封禁用户
    @PostMapping("/admin/ban/{targetUserId}")
    public Result<String> banUser(@PathVariable Integer targetUserId,
                                  @RequestParam(required = false) String reason,
                                  @RequestParam(required = false) Integer days,
                                  HttpServletRequest request) {
        Integer userType = (Integer) request.getAttribute("userType");
        if (userType == null || userType != 1) {
            return Result.error(40301, "需要管理员权限");
        }
        return userService.banUser(targetUserId, reason, days);
    }

    // 管理员解封用户
    @PostMapping("/admin/unban/{targetUserId}")
    public Result<String> unbanUser(@PathVariable Integer targetUserId, HttpServletRequest request) {
        Integer userType = (Integer) request.getAttribute("userType");
        if (userType == null || userType != 1) {
            return Result.error(40301, "需要管理员权限");
        }
        return userService.unbanUser(targetUserId);
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("avatar") MultipartFile file, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        
        if (file.isEmpty()) {
            return Result.error(40001, "File is empty");
        }
        
        try {
            File tempFile = File.createTempFile("avatar-", file.getOriginalFilename());
            file.transferTo(tempFile);
            String key = "avatars/" + userId + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            cosService.uploadFile(tempFile.getAbsolutePath(), key);
            
            String url = "https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/" + key;
            
            if (tempFile.exists()) {
                tempFile.delete();
            }
            
            // Update user avatar
            userService.updateAvatar(userId, url);
            
            return Result.success("Avatar uploaded successfully", url);
        } catch (Exception e) {
            return Result.error(50001, "Upload failed: " + e.getMessage());
        }
    }
}
