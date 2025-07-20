package com.xiaofuge.exception;

import lombok.Getter;

@Getter
public class FunctionCallException extends RuntimeException {
    private final String functionName;
    
    public FunctionCallException(String functionName, String message) {
        super(message);
        this.functionName = functionName;
    }
    
    public FunctionCallException(String functionName, String message, Throwable cause) {
        super(message, cause);
        this.functionName = functionName;
    }
    
    public static FunctionCallException functionNotFound(String functionName) {
        return new FunctionCallException(functionName, "函数不存在: " + functionName);
    }
    
    public static FunctionCallException parameterError(String functionName, String parameterName) {
        return new FunctionCallException(functionName, "参数错误: " + parameterName);
    }
    
    public static FunctionCallException executionError(String functionName, Throwable cause) {
        return new FunctionCallException(functionName, "函数执行失败", cause);
    }
}