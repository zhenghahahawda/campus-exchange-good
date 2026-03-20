package com.campus.exchange.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    DISABLED(0, "已禁用"),
    NORMAL(1, "正常"),
    ACTIVE(2, "活跃");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus fromCode(Integer code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
