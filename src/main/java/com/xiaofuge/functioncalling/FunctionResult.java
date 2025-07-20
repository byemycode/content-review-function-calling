package com.xiaofuge.functioncalling;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionResult {
    private String functionName;
    private boolean success;
    private Object result;
    private String errorMessage;
    
    public static FunctionResult success(String functionName, Object result) {
        return FunctionResult.builder()
                .functionName(functionName)
                .success(true)
                .result(result)
                .build();
    }
    
    public static FunctionResult error(String functionName, String errorMessage) {
        return FunctionResult.builder()
                .functionName(functionName)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}