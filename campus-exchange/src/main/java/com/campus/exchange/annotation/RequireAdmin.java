package com.campus.exchange.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * 标注在需要管理员权限的接口上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
    /**
     * 权限描述
     */
    String value() default "需要管理员权限";
}
