package com.xiaofuge.functioncalling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

@Component
@Slf4j
public class FunctionRegistry {
    
    private final Map<String, FunctionDefinition> functionDefinitions = new ConcurrentHashMap<>();
    private final Map<String, FunctionHandler> functionHandlers = new ConcurrentHashMap<>();
    
    public void registerFunction(String name, FunctionDefinition definition, FunctionHandler handler) {
        log.info("注册函数: {}", name);
        functionDefinitions.put(name, definition);
        functionHandlers.put(name, handler);
    }
    
    public FunctionDefinition getFunctionDefinition(String name) {
        return functionDefinitions.get(name);
    }
    
    public FunctionHandler getFunctionHandler(String name) {
        return functionHandlers.get(name);
    }
    
    public List<FunctionDefinition> getAllFunctionDefinitions() {
        return new ArrayList<>(functionDefinitions.values());
    }
    
    public boolean hasFunction(String name) {
        return functionDefinitions.containsKey(name);
    }
    
    public FunctionResult executeFunction(FunctionCall functionCall) {
        String functionName = functionCall.getName();
        
        if (!hasFunction(functionName)) {
            return FunctionResult.error(functionName, "函数不存在: " + functionName);
        }
        
        try {
            FunctionHandler handler = getFunctionHandler(functionName);
            Object result = handler.handle(functionCall.getArguments());
            log.info("函数执行成功: {}", functionName);
            return FunctionResult.success(functionName, result);
        } catch (Exception e) {
            log.error("函数执行失败: {}", functionName, e);
            return FunctionResult.error(functionName, "函数执行失败: " + e.getMessage());
        }
    }
}