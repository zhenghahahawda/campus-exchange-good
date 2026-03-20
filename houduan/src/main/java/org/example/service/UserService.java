package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.common.Result;
import org.example.dto.UserProfileResponse;
import org.example.dto.LoginResponse;
import org.example.entity.User;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {

    Result<LoginResponse> login(String account, String password, HttpServletRequest request);

    Result<String> register(User user, String password);

    Result<User> getUserInfo(String userId);

    Result<String> updateUserInfo(String userId, User user);

    Result<String> updateAvatar(String userId, String avatarUrl);
    
    Result<String> banUser(Integer targetUserId, String reason, Integer days);
    
    Result<String> unbanUser(Integer targetUserId);

    Result<UserProfileResponse> getUserProfile(Integer userId);

    Result<String> resetPassword(String account, String newPassword);
}
