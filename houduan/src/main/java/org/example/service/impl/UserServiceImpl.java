package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.Result;
import org.example.dto.LoginResponse;
import org.example.dto.UserProfileResponse;
import org.example.entity.Good;
import org.example.entity.Exchange;
import org.example.entity.User;
import org.example.entity.UserSession;
import org.example.mapper.ExchangeMapper;
import org.example.mapper.GoodMapper;
import org.example.mapper.UserMapper;
import org.example.service.UserLoginLogService;
import org.example.service.UserService;
import org.example.service.UserSessionService;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserLoginLogService loginLogService;
    
    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private ExchangeMapper exchangeMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<LoginResponse> login(String account, String password, HttpServletRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", account)
                .or().eq("email", account)
                .or().eq("phone", account);
        
        User user = this.getOne(queryWrapper);
        if (user == null) {
            // 记录登录失败日志
            loginLogService.recordLoginLog(null, false, "Account does not exist", request);
            return Result.error(40101, "Account does not exist");
        }

        // Check if account is banned
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            // 永久封禁，或封禁时间还未到期
            if (user.getBannedUntil() == null || user.getBannedUntil().isAfter(LocalDateTime.now())) {
                String reason = user.getBanReason() != null ? user.getBanReason() : "Account is banned";
                loginLogService.recordLoginLog(user.getUserId(), false, reason, request);
                return Result.error(40301, "账户已被封禁：" + reason);
            } else {
                // 封禁已到期，自动解封
                user.setIsBanned(0);
                user.setStatus(1);
                this.updateById(user);
            }
        }

        // Check if account is active
        if (user.getStatus() != null && user.getStatus() == 0) {
            loginLogService.recordLoginLog(user.getUserId(), false, "Account is inactive", request);
            return Result.error(40301, "账户已被禁用");
        }

        // Use BCrypt to verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            loginLogService.recordLoginLog(user.getUserId(), false, "Incorrect password", request);
            return Result.error(40101, "Incorrect password");
        }

        // Update last login time
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        // 记录登录成功日志
        loginLogService.recordLoginLog(user.getUserId(), true, null, request);

        // Generate JWT token with user role information
        String token = jwtUtil.generateToken(String.valueOf(user.getUserId()), user.getUsername(), user.getUserType());
        String refreshToken = jwtUtil.generateRefreshToken(String.valueOf(user.getUserId()), user.getUsername());

        // Create session record in user_sessions table
        UserSession session = userSessionService.createSession(user.getUserId(), token, refreshToken, request);

        System.out.println("=== 生成的Token信息 ===");
        System.out.println("Access Token: " + token);
        System.out.println("Refresh Token: " + refreshToken);
        System.out.println("Session ID: " + session.getSessionId());

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setUserType(user.getUserType()); // 1=管理员, 2=普通用户
        response.setStatus(user.getStatus()); // 0=封禁, 1=正常, 2=活跃
        response.setRole(user.getUserType() == 1 ? "admin" : "user");
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setAvatar(user.getAvatar());
        response.setSchool(user.getSchool());
        response.setCreditScore(user.getCreditScore()); // 返回信誉分
        
        System.out.println("=== LoginResponse对象 ===");
        System.out.println("Token in response: " + response.getToken());
        System.out.println("RefreshToken in response: " + response.getRefreshToken());

        // Add role description for clarity
        String roleMessage = user.getUserType() == 1 ? "Admin login successful" : "User login successful";
        
        return Result.success(roleMessage, response);
    }

    @Override
    public Result<String> register(User user, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername())
                .or().eq("email", user.getEmail())
                .or().eq("phone", user.getPhone());
        
        if (this.count(queryWrapper) > 0) {
            return Result.error(40901, "User already exists");
        }

        String hashedPassword = passwordEncoder.encode(password);
        user.setPasswordHash(hashedPassword);
        user.setUserType(2); // 2=普通用户
        user.setStatus(1); // 1=正常
        user.setCreditScore(80); // 初始信誉值80
        user.setIsBanned(0);
        user.setLoginAttempts(0);
        
        // Generate user_id manually if not auto-increment
        if (user.getUserId() == null) {
            // Get max user_id and increment
            QueryWrapper<User> maxIdWrapper = new QueryWrapper<>();
            maxIdWrapper.select("MAX(user_id) as user_id");
            User maxUser = this.getOne(maxIdWrapper);
            Integer maxId = (maxUser != null && maxUser.getUserId() != null) ? maxUser.getUserId() : 0;
            user.setUserId(maxId + 1);
        }
        
        this.save(user);

        return Result.success("Registration successful", String.valueOf(user.getUserId()));
    }

    @Override
    public Result<User> getUserInfo(String userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error(40401, "User not found");
        }
        user.setPasswordHash(null);
        return Result.success(user);
    }

    @Override
    public Result<String> updateUserInfo(String userId, User user) {
        user.setUserId(Integer.valueOf(userId));
        user.setPasswordHash(null);
        this.updateById(user);
        return Result.success("Update successful", null);
    }

    @Override
    public Result<String> updateAvatar(String userId, String avatarUrl) {
        User user = new User();
        user.setUserId(Integer.valueOf(userId));
        user.setAvatar(avatarUrl);
        this.updateById(user);
        return Result.success("Avatar updated", avatarUrl);
    }

    @Override
    public Result<String> banUser(Integer targetUserId, String reason, Integer days) {
        User user = this.getById(targetUserId);
        if (user == null) {
            return Result.error(40401, "User not found");
        }
        user.setIsBanned(1);
        user.setStatus(0);
        user.setBanReason(reason);
        if (days != null && days > 0) {
            user.setBannedUntil(LocalDateTime.now().plusDays(days));
        } else {
            user.setBannedUntil(null); // 永久封禁
        }
        this.updateById(user);
        // 立即踢出该用户所有活跃 session
        userSessionService.invalidateAllUserSessions(targetUserId);
        return Result.success("User banned successfully", null);
    }

    @Override
    public Result<String> unbanUser(Integer targetUserId) {
        User user = this.getById(targetUserId);
        if (user == null) {
            return Result.error(40401, "User not found");
        }
        user.setIsBanned(0);
        user.setStatus(1);
        user.setBanReason(null);
        user.setBannedUntil(null);
        this.updateById(user);
        return Result.success("User unbanned successfully", null);
    }

    @Override
    public Result<String> resetPassword(String account, String newPassword) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", account).or().eq("phone", account);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            return Result.error(40401, "该账号不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        this.updateById(user);
        // 重置密码后踢出所有 session
        userSessionService.invalidateAllUserSessions(user.getUserId());
        return Result.success("密码重置成功", null);
    }

    @Override
    public Result<UserProfileResponse> getUserProfile(Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error(40401, "User not found");
        }

        UserProfileResponse profile = new UserProfileResponse();
        profile.setUserId(user.getUserId());
        profile.setUsername(user.getUsername());
        profile.setAvatar(user.getAvatar());
        profile.setSchool(user.getSchool());
        profile.setCreditScore(user.getCreditScore());
        profile.setUserType(user.getUserType());
        profile.setCreatedAt(user.getCreatedAt());

        // 统计完成的换物次数（作为发起方或接收方）
        LambdaQueryWrapper<Exchange> exchangeWrapper = new LambdaQueryWrapper<>();
        exchangeWrapper.eq(Exchange::getStatus, "completed")
                .and(w -> w.eq(Exchange::getInitiatorId, userId)
                        .or().eq(Exchange::getReceiverId, userId));
        profile.setExchangeCount(exchangeMapper.selectCount(exchangeWrapper).intValue());

        // 查询该用户已审核通过且已上架的商品
        LambdaQueryWrapper<Good> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Good::getUserId, userId)
               .eq(Good::getStatus, 1)       // 已审核通过
               .eq(Good::getShelfStatus, 1)  // 已上架
               .orderByDesc(Good::getCreatedAt);
        profile.setGoods(goodMapper.selectList(wrapper));

        return Result.success(profile);
    }
}
