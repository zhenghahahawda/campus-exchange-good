package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.constant.SystemConstants;
import com.campus.exchange.dto.*;
import com.campus.exchange.entity.User;
import com.campus.exchange.enums.UserStatus;
import com.campus.exchange.enums.UserType;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.CosService;
import com.campus.exchange.service.LoginLogService;
import com.campus.exchange.service.UserService;
import com.campus.exchange.service.ThemeService;
import com.campus.exchange.service.UserSessionService;
import com.campus.exchange.entity.UserSession;
import com.campus.exchange.service.GoodsService;
import com.campus.exchange.service.ViolationReportService;
import com.campus.exchange.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private CosService cosService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired(required = false)
    private GoodsService goodsService;

    @Autowired(required = false)
    private ViolationReportService violationReportService;

    @Autowired
    private ThemeService themeService;

    @Autowired
    private com.campus.exchange.mapper.UserLoginLogMapper userLoginLogMapper;

    @Autowired
    private com.campus.exchange.mapper.GoodsMapper goodsMapper;

    @Autowired
    private com.campus.exchange.mapper.OrdersMapper ordersMapper;


    /**
     * 用户登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 步骤1：根据账号（用户名/邮箱/手机号）查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .eq(User::getUsername, request.getAccount())
                .or()
                .eq(User::getEmail, request.getAccount())
                .or()
                .eq(User::getPhone, request.getAccount())
        ).isNull(User::getDeletedAt);

        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        // 步骤2：检查账户状态
        if (user.getStatus() == UserStatus.DISABLED.getCode()) {
            loginLogService.recordLoginFailure(user.getUserId(), "账户已被禁用", httpRequest);
            throw new BusinessException("账户已被禁用");
        }

        // 检查账户是否被锁定（通过 lockedUntil 判断）
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            loginLogService.recordLoginFailure(user.getUserId(), "账户已被锁定", httpRequest);
            throw new BusinessException("账户已被锁定，请稍后再试");
        }
        // 锁定时间已过，清除锁定信息
        if (user.getLockedUntil() != null) {
            User unlockUser = new User();
            unlockUser.setUserId(user.getUserId());
            unlockUser.setLoginAttempts(0);
            unlockUser.setLockedUntil(null);
            userMapper.updateById(unlockUser);
        }

        // 步骤3：验证密码（使用BCrypt）
        if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            // 登录失败，增加失败次数
            handleLoginFailure(user);
            loginLogService.recordLoginFailure(user.getUserId(), "密码错误", httpRequest);
            throw new BusinessException("账号或密码错误");
        }

        // 步骤4：登录成功，更新最后登录时间
        // 重要：只更新特定字段，避免意外更新密码等敏感信息
        User updateUser = new User();
        updateUser.setUserId(user.getUserId());
        updateUser.setLastLoginAt(LocalDateTime.now());
        updateUser.setLoginAttempts(0);
        updateUser.setLockedUntil(null);
        userMapper.updateById(updateUser);

        // 步骤5：记录登录成功日志
        loginLogService.recordLoginSuccess(user.getUserId(), httpRequest);

        // 步骤5.5：检查连续7天登录，满足则标记为活跃用户
        if (user.getStatus() != null && user.getStatus().equals(UserStatus.NORMAL.getCode())) {
            checkAndSetActiveStatus(user.getUserId());
        }

        // 步骤6：生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), user.getUserType());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 步骤7：创建用户会话记录（必须成功，否则登录失败）
        UserSession session = new UserSession();
        session.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(user.getUserId());
        session.setToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getAccessTokenExpiration()));
        session.setLastActivityAt(LocalDateTime.now());
        session.setIpAddress(httpRequest.getRemoteAddr());
        session.setUserAgent(httpRequest.getHeader("User-Agent"));
        session.setDeviceId(httpRequest.getHeader("X-Device-Id"));
        session.setIsActive(1);
        userSessionService.createSession(session);

        // 步骤8：返回登录响应
        return LoginResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .userType(user.getUserType())
                .status(user.getStatus())
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    /**
     * 检查用户是否连续登录7天，满足则设为活跃用户(status=2)
     * 逻辑：查询最近7天的成功登录记录，按日期去重，如果覆盖了连续7个不同的日期则标记为活跃
     */
    private void checkAndSetActiveStatus(Integer userId) {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.campus.exchange.entity.UserLoginLog> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("login_status", 1)
                   .ge("login_time", sevenDaysAgo)
                   .orderByDesc("login_time");
            java.util.List<com.campus.exchange.entity.UserLoginLog> logs = userLoginLogMapper.selectList(wrapper);

            // 按日期去重，统计不同登录天数
            java.util.Set<java.time.LocalDate> loginDays = new java.util.HashSet<>();
            for (com.campus.exchange.entity.UserLoginLog log : logs) {
                if (log.getLoginTime() != null) {
                    loginDays.add(log.getLoginTime().toLocalDate());
                }
            }

            // 检查最近7天是否每天都有登录
            boolean consecutive = true;
            for (int i = 0; i < 7; i++) {
                java.time.LocalDate day = java.time.LocalDate.now().minusDays(i);
                if (!loginDays.contains(day)) {
                    consecutive = false;
                    break;
                }
            }

            if (consecutive) {
                User updateUser = new User();
                updateUser.setUserId(userId);
                updateUser.setStatus(UserStatus.ACTIVE.getCode());
                userMapper.updateById(updateUser);
            }
        } catch (Exception e) {
            // 活跃状态检查失败不影响登录流程
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user) {
        int attempts = user.getLoginAttempts() + 1;

        // 重要：只更新特定字段，避免意外更新密码等敏感信息
        User updateUser = new User();
        updateUser.setUserId(user.getUserId());
        updateUser.setLoginAttempts(attempts);

        // 如果失败次数 >= 5，锁定账户30分钟（只设 lockedUntil，不改 status）
        if (attempts >= SystemConstants.User.LOGIN_FAIL_LOCK_COUNT) {
            updateUser.setLockedUntil(LocalDateTime.now().plusMinutes(SystemConstants.User.ACCOUNT_LOCK_MINUTES));
        }

        userMapper.updateById(updateUser);
    }

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(RegisterRequest request) {
        // 步骤1：检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, request.getUsername())
                .isNull(User::getDeletedAt);
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 步骤2：检查邮箱是否已存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, request.getEmail())
                .isNull(User::getDeletedAt);
        if (userMapper.selectCount(emailWrapper) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 步骤3：检查手机号是否已存在
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, request.getPhone())
                .isNull(User::getDeletedAt);
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 步骤4：生成用户ID（调用存储过程）
        Integer newUserId = generateUserId();

        // 步骤5：使用BCrypt加密密码
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        // 步骤6：创建用户对象
        User user = new User();
        user.setUserId(newUserId);
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSchool(request.getSchool());
        user.setGender(request.getGender());
        user.setUserType(2); // 普通用户
        user.setStatus(1); // 正常状态
        user.setCreditScore(80); // 默认信誉分80
        user.setLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 步骤7：保存到数据库
        userMapper.insert(user);

        // 步骤8：返回注册响应
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return RegisterResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt().format(formatter))
                .build();
    }

    /**
     * 生成用户ID
     * 调用数据库存储过程生成8位数字ID
     */
    private Integer generateUserId() {
        try {
            // 调用存储过程
            Map<String, Object> params = new HashMap<>();
            params.put("userId", null);
            userMapper.generateUserId(params);

            // 获取输出参数
            Object userIdObj = params.get("userId");
            if (userIdObj != null) {
                return (Integer) userIdObj;
            }
        } catch (Exception e) {
            // 如果存储过程调用失败，使用备用方案
            System.err.println("调用存储过程失败，使用备用ID生成方案: " + e.getMessage());
        }

        // 备用方案：原子性递增 user_id_sequence 表，避免竞态条件
        userMapper.incrementUserIdSequence();
        Integer nextId = userMapper.getLastInsertId();

        if (nextId == null) {
            throw new RuntimeException("生成用户ID失败");
        }
        if (nextId > 99999999) {
            throw new RuntimeException("用户ID已达到上限");
        }

        return nextId;
    }

    /**
     * 获取用户信息
     */
    @Override
    public UserInfoResponse getUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new RuntimeException("用户不存在");
        }

        return convertToUserInfoResponse(user);
    }

    /**
     * 更新用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResponse updateUserInfo(Integer userId, UpdateUserInfoRequest request) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果要更新用户名，检查是否已被占用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername())
                    .isNull(User::getDeletedAt);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        // 更新其他字段
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getSchool() != null) {
            user.setSchool(request.getSchool());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getThemePreference() != null) {
            user.setThemePreference(themeService.validateAndNormalizeThemePreference(request.getThemePreference()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        return convertToUserInfoResponse(user);
    }

    /**
     * 验证会话
     */
    @Override
    public SessionVerifyResponse verifySession(Integer userId, String username) {
        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证用户名是否匹配
        if (!user.getUsername().equals(username)) {
            throw new RuntimeException("用户信息不匹配");
        }

        // 检查账户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账户状态异常");
        }

        return new SessionVerifyResponse(user.getUserId(), user.getUsername(), true);
    }

    /**
     * 转换为用户信息响应
     */
    private UserInfoResponse convertToUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setUserType(user.getUserType());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatarUrl());
        response.setAddress(user.getAddress());
        response.setSchool(user.getSchool());
        response.setGender(user.getGender());
        response.setBirthday(user.getBirthday());
        response.setRegisterTime(user.getCreatedAt());
        response.setLastLoginTime(user.getLastLoginAt());
        response.setThemePreference(user.getThemePreference());
        return response;
    }

    /**
     * 获取所有用户列表（包含商品数量和订单数量）
     */
    @Override
    public List<UserListResponse> getAllUsers() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(User::getDeletedAt)
                .orderByDesc(User::getCreatedAt);
        List<User> users = userMapper.selectList(queryWrapper);

        // 转换为UserListResponse，统计每个用户的商品和订单数量
        return users.stream().map(user -> {
            // 统计用户发布的商品数量（未删除的）
            Long uid = user.getUserId().longValue();
            LambdaQueryWrapper<com.campus.exchange.entity.Goods> goodsWrapper = new LambdaQueryWrapper<>();
            goodsWrapper.eq(com.campus.exchange.entity.Goods::getSellerId, uid)
                    .isNull(com.campus.exchange.entity.Goods::getDeletedAt);
            long goodsCount = goodsMapper.selectCount(goodsWrapper);

            // 统计用户参与的订单数量（作为发起方或接收方，未删除的）
            LambdaQueryWrapper<com.campus.exchange.entity.Orders> ordersWrapper = new LambdaQueryWrapper<>();
            ordersWrapper.and(wrapper -> wrapper
                    .eq(com.campus.exchange.entity.Orders::getBuyerId, uid)
                    .or()
                    .eq(com.campus.exchange.entity.Orders::getSellerId, uid))
                    .isNull(com.campus.exchange.entity.Orders::getDeletedAt);
            long orderCount = ordersMapper.selectCount(ordersWrapper);

            return UserListResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .userType(user.getUserType())
                    .status(user.getStatus())
                    .creditScore(user.getCreditScore())
                .isBanned(user.getIsBanned())
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                    .isBanned(user.getIsBanned())
                    .bannedUntil(user.getBannedUntil())
                    .banReason(user.getBanReason())
                    .lockedUntil(user.getLockedUntil())
                    .realName(user.getRealName())
                    .avatarUrl(user.getAvatarUrl())
                    .address(user.getAddress())
                    .school(user.getSchool())
                    .gender(user.getGender())
                    .birthday(user.getBirthday())
                    .createdAt(user.getCreatedAt())
                    .lastLoginAt(user.getLastLoginAt())
                    .goodsCount(goodsCount)
                    .orderCount(orderCount)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 管理员创建用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserListResponse adminCreateUser(AdminCreateUserRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, request.getUsername())
                .isNull(User::getDeletedAt);
        if (userMapper.selectCount(usernameWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getEmail, request.getEmail())
                .isNull(User::getDeletedAt);
        if (userMapper.selectCount(emailWrapper) > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 检查手机号是否已存在
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, request.getPhone())
                .isNull(User::getDeletedAt);
        if (userMapper.selectCount(phoneWrapper) > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 生成用户ID
        Integer newUserId = generateUserId();

        // 使用BCrypt加密密码
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        // 创建用户对象
        User user = new User();
        user.setUserId(newUserId);
        user.setUsername(request.getUsername());
        user.setPasswordHash(hashedPassword);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType());
        user.setStatus(request.getStatus());
        user.setRealName(request.getRealName());
        user.setSchool(request.getSchool());
        user.setAddress(request.getAddress());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());
        user.setLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        userMapper.insert(user);

        // 返回创建的用户信息（包含商品数量和订单数量，新用户都是0）
        return UserListResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .status(user.getStatus())
                .creditScore(user.getCreditScore())
                .isBanned(user.getIsBanned())
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                .lockedUntil(user.getLockedUntil())
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .school(user.getSchool())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .goodsCount(0L)
                .orderCount(0L)
                .build();
    }

    /**
     * 管理员更新用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserListResponse adminUpdateUser(Integer targetUserId, AdminUpdateUserRequest request) {
        // 查询目标用户
        User user = userMapper.selectById(targetUserId);
        if (user == null || user.getDeletedAt() != null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果要更新用户名，检查是否已被占用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername())
                    .isNull(User::getDeletedAt);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        // 如果要更新邮箱，检查是否已被占用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail())
                    .isNull(User::getDeletedAt);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }

        // 如果要更新手机号，检查是否已被占用
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, request.getPhone())
                    .isNull(User::getDeletedAt);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("手机号已被占用");
            }
            user.setPhone(request.getPhone());
        }

        // 更新其他字段
        if (request.getSchool() != null) {
            user.setSchool(request.getSchool());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getCreditScore() != null) {
            user.setCreditScore(request.getCreditScore());
        }
        if (request.getLockedUntil() != null) {
            user.setLockedUntil(request.getLockedUntil());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 状态缓存已移除，不再需要清除
        // if (request.getStatus() != null) {
        //     userStatusCacheService.clearUserStatusCache(targetUserId);
        // }

        return buildUserListResponse(user);
    }

    /**
     * 构建用户列表响应（包含商品和订单统计）
     */
    private UserListResponse buildUserListResponse(User user) {
        // 统计用户发布的商品数量
        Long uid = user.getUserId().longValue();
        LambdaQueryWrapper<com.campus.exchange.entity.Goods> goodsWrapper = new LambdaQueryWrapper<>();
        goodsWrapper.eq(com.campus.exchange.entity.Goods::getSellerId, uid)
                .isNull(com.campus.exchange.entity.Goods::getDeletedAt);
        long goodsCount = goodsMapper.selectCount(goodsWrapper);

        // 统计用户参与的订单数量
        LambdaQueryWrapper<com.campus.exchange.entity.Orders> ordersWrapper = new LambdaQueryWrapper<>();
        ordersWrapper.and(wrapper -> wrapper
                .eq(com.campus.exchange.entity.Orders::getBuyerId, uid)
                .or()
                .eq(com.campus.exchange.entity.Orders::getSellerId, uid))
                .isNull(com.campus.exchange.entity.Orders::getDeletedAt);
        long orderCount = ordersMapper.selectCount(ordersWrapper);

        return UserListResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .status(user.getStatus())
                .creditScore(user.getCreditScore())
                .isBanned(user.getIsBanned())
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                .lockedUntil(user.getLockedUntil())
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .school(user.getSchool())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .goodsCount(goodsCount)
                .orderCount(orderCount)
                .build();
    }

    /**
     * 上传用户头像
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Integer userId, MultipartFile file) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new RuntimeException("用户不存在");
        }

        // 上传到COS
        String avatarUrl = cosService.uploadAvatar(file, userId);

        // 更新数据库中的头像URL
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setAvatarUrl(avatarUrl);
        updateUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(updateUser);

        return avatarUrl;
    }

    /**
     * 管理员删除用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDeleteUser(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 级联删除：删除用户相关的所有数据
        try {
            // 1. 删除用户的登录日志
            loginLogService.deleteLoginLogsByUserId(userId);

            // 2. 删除用户的会话记录
            userSessionService.deleteSessionsByUserId(userId);

            // 3. 删除用户发布的商品
            if (goodsService != null) {
                goodsService.deleteGoodsBySellerId(Long.valueOf(userId));
            }

            // 4. 删除用户的违规举报记录
            if (violationReportService != null) {
                violationReportService.deleteReportsByUserId(userId);
            }

            // 5. 最后删除用户本身
            userMapper.deleteById(userId);
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败: " + e.getMessage(), e);
        }
    }

    /**
     * 管理员重置用户密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new RuntimeException("用户不存在");
        }

        // 重置密码为123456
        String hashedPassword = BCrypt.hashpw("123456", BCrypt.gensalt());

        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPasswordHash(hashedPassword);
        updateUser.setUpdatedAt(LocalDateTime.now());
        // 解锁账户（如果被锁定了）
        updateUser.setStatus(UserStatus.NORMAL.getCode());
        updateUser.setLockedUntil(null);
        updateUser.setLoginAttempts(0);

        userMapper.updateById(updateUser);
    }
}
