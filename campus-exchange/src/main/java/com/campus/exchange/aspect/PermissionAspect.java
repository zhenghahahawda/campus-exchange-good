package com.campus.exchange.aspect;

import com.campus.exchange.annotation.RequireAdmin;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 权限验证切面
 */
@Aspect
@Component
public class PermissionAspect {

    /**
     * 管理员权限验证
     */
    @Around("@annotation(requireAdmin)")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint, RequireAdmin requireAdmin) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取请求信息");
        }

        HttpServletRequest request = attributes.getRequest();
        Integer userType = (Integer) request.getAttribute("userType");

        if (userType == null || userType != 1) {
            throw new RuntimeException("无权限访问");
        }

        return joinPoint.proceed();
    }
}
