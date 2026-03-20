package com.campus.exchange.util;

import lombok.Data;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> {

    // 成功状态码
    public static final int SUCCESS_CODE = 200;
    // 失败状态码
    public static final int ERROR_CODE = 500;

    // 状态码
    private Integer code;
    // 消息
    private String message;
    // 数据
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
