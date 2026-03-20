package org.example.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        log.error("Exception in request {}: ", request.getRequestURI(), e);
        // 返回具体的异常信息而不是通用的"Internal Server Error"
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
        return Result.error(50001, errorMessage);
    }

    // Add more specific exceptions as needed
    // e.g., MethodArgumentNotValidException for @Valid
}
