package com.campus.exchange.config;

import com.campus.exchange.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 /api/** 路径
                .excludePathPatterns(
                        "/api/user/login",      // 登录接口不拦截
                        "/api/user/register",   // 注册接口不拦截
                        "/api/auth/refresh-token", // 刷新令牌接口不拦截
                        // 注意：移除了 /api/goods 相关的排除规则
                        // 所有商品操作都需要验证 Token（包括查询、创建、更新、删除）
                        "/swagger-ui/**",       // Swagger UI 不拦截
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/webjars/**"
                );
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
