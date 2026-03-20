package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.UserLoginLog;
import org.example.mapper.UserLoginLogMapper;
import org.example.service.UserLoginLogService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements UserLoginLogService {

    @Override
    public void recordLoginLog(Integer userId, boolean success, String failReason, HttpServletRequest request) {
        try {
            UserLoginLog log = new UserLoginLog();
            log.setUserId(userId);
            log.setLoginTime(LocalDateTime.now());
            log.setLoginStatus(success ? 1 : 0);
            log.setFailReason(failReason);
            
            // 获取IP地址
            String ip = getClientIp(request);
            log.setLoginIp(ip);
            
            // 获取User-Agent
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent);
            
            // 解析设备类型、浏览器、操作系统
            if (userAgent != null) {
                log.setDeviceType(parseDeviceType(userAgent));
                log.setBrowser(parseBrowser(userAgent));
                log.setOs(parseOs(userAgent));
            }
            
            this.save(log);
        } catch (Exception e) {
            // 记录日志失败不影响登录流程
            System.err.println("Failed to record login log: " + e.getMessage());
        }
    }
    
    /**
     * 获取客户端真实IP
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
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 解析设备类型
     */
    private String parseDeviceType(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        } else {
            return "PC";
        }
    }
    
    /**
     * 解析浏览器
     */
    private String parseBrowser(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg")) {
            return "Edge";
        } else if (ua.contains("chrome")) {
            return "Chrome";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("opera") || ua.contains("opr")) {
            return "Opera";
        } else if (ua.contains("msie") || ua.contains("trident")) {
            return "IE";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * 解析操作系统
     */
    private String parseOs(String userAgent) {
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 10")) {
            return "Windows 10";
        } else if (ua.contains("windows nt 6.3")) {
            return "Windows 8.1";
        } else if (ua.contains("windows nt 6.2")) {
            return "Windows 8";
        } else if (ua.contains("windows nt 6.1")) {
            return "Windows 7";
        } else if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac os x")) {
            return "Mac OS X";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else {
            return "Unknown";
        }
    }
}
