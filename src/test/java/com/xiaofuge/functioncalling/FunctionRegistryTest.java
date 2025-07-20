package com.xiaofuge.functioncalling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionRegistryTest {
    
    private FunctionRegistry functionRegistry;
    
    @BeforeEach
    void setUp() {
        functionRegistry = new FunctionRegistry();
    }
    
    @Test
    void testRegisterAndExecuteFunction() {
        // 注册一个测试函数
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("test_function")
                .description("测试函数")
                .parameters(Map.of("type", "object"))
                .build();
        
        FunctionHandler handler = (arguments) -> {
            String name = (String) arguments.get("name");
            return "Hello, " + name + "!";
        };
        
        functionRegistry.registerFunction("test_function", definition, handler);
        
        // 测试函数是否注册成功
        assertTrue(functionRegistry.hasFunction("test_function"));
        assertNotNull(functionRegistry.getFunctionDefinition("test_function"));
        
        // 测试函数执行
        FunctionCall functionCall = FunctionCall.builder()
                .name("test_function")
                .arguments(Map.of("name", "World"))
                .build();
        
        FunctionResult result = functionRegistry.executeFunction(functionCall);
        
        assertTrue(result.isSuccess());
        assertEquals("Hello, World!", result.getResult());
    }
    
    @Test
    void testExecuteNonExistentFunction() {
        FunctionCall functionCall = FunctionCall.builder()
                .name("non_existent_function")
                .arguments(Map.of())
                .build();
        
        FunctionResult result = functionRegistry.executeFunction(functionCall);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("函数不存在"));
    }
    
    @Test
    void testFunctionExecutionError() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("error_function")
                .description("会出错的函数")
                .parameters(Map.of("type", "object"))
                .build();
        
        FunctionHandler handler = (arguments) -> {
            throw new RuntimeException("测试异常");
        };
        
        functionRegistry.registerFunction("error_function", definition, handler);
        
        FunctionCall functionCall = FunctionCall.builder()
                .name("error_function")
                .arguments(Map.of())
                .build();
        
        FunctionResult result = functionRegistry.executeFunction(functionCall);
        
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("测试异常"));
    }
}