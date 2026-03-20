package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.UserLoginLog;
import com.campus.exchange.mapper.UserLoginLogMapper;
import com.campus.exchange.service.LoginLogService;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志服务实现类
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private UserLoginLogMapper loginLogMapper;

    @Override
    public void recordLoginSuccess(Integer userId, HttpServletRequest request) {
        UserLoginLog log = buildLoginLog(userId, request);
        log.setLoginStatus(1);
        loginLogMapper.insert(log);
    }

    @Override
    public void recordLoginFailure(Integer userId, String failReason, HttpServletRequest request) {
        UserLoginLog log = buildLoginLog(userId, request);
        log.setLoginStatus(0);
        log.setFailReason(failReason);
        loginLogMapper.insert(log);
    }

    @Override
    public PageResponse<UserLoginLog> pageLoginLogs(PageRequest pageRequest) {
        Page<UserLoginLog> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<UserLoginLog> queryWrapper = new QueryWrapper<>();

        // 状态筛选
        if (pageRequest.getStatus() != null) {
            queryWrapper.eq("login_status", pageRequest.getStatus());
        }

        // 关键词搜索（用户名/IP/浏览器/操作系统）
        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().trim().isEmpty()) {
            String kw = pageRequest.getKeyword().trim();
            queryWrapper.and(w -> w
                .like("login_ip", kw)
                .or().like("browser", kw)
                .or().like("os", kw)
                .or().like("user_agent", kw)
                .or().like("user_id", kw)
            );
        }

        // 排序
        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("login_time");
        }

        IPage<UserLoginLog> result = loginLogMapper.selectPage(page, queryWrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public PageResponse<UserLoginLog> pageLoginLogsByUserId(Integer userId, PageRequest pageRequest) {
        Page<UserLoginLog> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<UserLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        // 排序
        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("login_time");
        }

        IPage<UserLoginLog> result = loginLogMapper.selectPage(page, queryWrapper);
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public UserLoginLog getLoginLogById(Long logId) {
        return loginLogMapper.selectById(logId);
    }

    @Override
    public List<UserLoginLog> getLoginLogsByUserId(Integer userId) {
        QueryWrapper<UserLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("login_time");
        return loginLogMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteLoginLog(Long logId) {
        loginLogMapper.deleteById(logId);
    }

    @Override
    public void batchDeleteLoginLogs(List<Long> logIds) {
        if (logIds != null && !logIds.isEmpty()) {
            loginLogMapper.deleteBatchIds(logIds);
        }
    }

    @Override
    public void deleteLoginLogsByUserId(Integer userId) {
        QueryWrapper<UserLoginLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        loginLogMapper.delete(queryWrapper);
    }

    private UserLoginLog buildLoginLog(Integer userId, HttpServletRequest request) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setLoginTime(LocalDateTime.now());
        log.setLoginIp(getClientIp(request));

        // 解析 User-Agent
        String uaString = request.getHeader("User-Agent");
        log.setUserAgent(uaString);

        if (uaString != null) {
            UserAgent userAgent = UserAgent.parseUserAgentString(uaString);
            Browser browser = userAgent.getBrowser();
            OperatingSystem os = userAgent.getOperatingSystem();

            log.setBrowser(browser.getName());
            log.setOs(os.getName());

            DeviceType deviceType = os.getDeviceType();
            log.setDeviceType(deviceType.getName());
        }

        return log;
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public java.util.Map<String, Long> getStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        long total = loginLogMapper.selectCount(null);
        QueryWrapper<UserLoginLog> successWrapper = new QueryWrapper<>();
        successWrapper.eq("login_status", 1);
        long success = loginLogMapper.selectCount(successWrapper);
        stats.put("total", total);
        stats.put("success", success);
        stats.put("failed", total - success);
        return stats;
    }
}
