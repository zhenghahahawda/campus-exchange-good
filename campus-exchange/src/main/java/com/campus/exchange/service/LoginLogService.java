package com.campus.exchange.service;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.UserLoginLog;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {

    /**
     * 记录登录成功日志
     */
    void recordLoginSuccess(Integer userId, HttpServletRequest request);

    /**
     * 记录登录失败日志
     */
    void recordLoginFailure(Integer userId, String failReason, HttpServletRequest request);

    /**
     * 分页查询登录日志
     */
    PageResponse<UserLoginLog> pageLoginLogs(PageRequest pageRequest);

    /**
     * 根据用户ID分页查询登录日志
     */
    PageResponse<UserLoginLog> pageLoginLogsByUserId(Integer userId, PageRequest pageRequest);

    /**
     * 根据ID获取登录日志详情
     */
    UserLoginLog getLoginLogById(Long logId);

    /**
     * 根据用户ID获取所有登录日志
     */
    List<UserLoginLog> getLoginLogsByUserId(Integer userId);

    /**
     * 删除登录日志
     */
    void deleteLoginLog(Long logId);

    /**
     * 批量删除登录日志
     */
    void batchDeleteLoginLogs(List<Long> logIds);

    /**
     * 根据用户ID删除所有登录日志
     */
    void deleteLoginLogsByUserId(Integer userId);

    /**
     * 获取登录日志统计（总数、成功数、失败数）
     */
    java.util.Map<String, Long> getStats();
}
