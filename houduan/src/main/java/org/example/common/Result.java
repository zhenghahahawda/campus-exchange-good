package org.example.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private String details;
    private String timestamp;
    private String path;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(20000);
        result.setMessage("Success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(20000);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message, String details) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setDetails(details);
        return result;
    }
}
