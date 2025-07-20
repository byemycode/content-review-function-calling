package com.xiaofuge.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ContentReviewException.class)
    public ResponseEntity<Map<String, Object>> handleContentReviewException(ContentReviewException e) {
        log.error("内容审核异常", e);
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", e.getMessage(),
            "code", e.getCode()
        ));
    }
    
    @ExceptionHandler(FunctionCallException.class)
    public ResponseEntity<Map<String, Object>> handleFunctionCallException(FunctionCallException e) {
        log.error("函数调用异常", e);
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "error", e.getMessage(),
            "functionName", e.getFunctionName()
        ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "error", "系统内部错误: " + e.getMessage()
        ));
    }
}