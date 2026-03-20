package com.campus.exchange.enums;

import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
public enum GoodsStatus {
    PENDING(0, "待审核"),
    ACTIVE(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final Integer code;
    private final String desc;

    GoodsStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static GoodsStatus fromCode(Integer code) {
        for (GoodsStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
