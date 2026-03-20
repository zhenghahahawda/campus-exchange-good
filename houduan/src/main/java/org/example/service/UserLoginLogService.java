package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.UserLoginLog;

import javax.servlet.http.HttpServletRequest;

public interface UserLoginLogService extends IService<UserLoginLog> {
    
    /**
     * 记录登录日志
     */
    void recordLoginLog(Integer userId, boolean success, String failReason, HttpServletRequest request);
}
