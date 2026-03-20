package com.campus.exchange.enums;

import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
public enum UserType {
    SUPER_ADMIN(0, "超级管理员"),
    ADMIN(1, "管理员"),
    USER(2, "普通用户");

    private final Integer code;
    private final String desc;

    UserType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserType fromCode(Integer code) {
        for (UserType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
