package com.xiaofuge.ai;

import com.xiaofuge.functioncalling.FunctionCall;
import com.xiaofuge.functioncalling.FunctionDefinition;
import com.xiaofuge.functioncalling.FunctionRegistry;
import com.xiaofuge.functioncalling.FunctionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {
    
    private final FunctionRegistry functionRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${ai.openai.api-key}")
    private String apiKey;
    
    @Value("${ai.openai.model}")
    private String model;
    
    public String processContentReview(String userPrompt) {
        try {
            OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), buildSystemPrompt()));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userPrompt));
            
            ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .functions(convertFunctionDefinitions())
                    .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                    .maxTokens(2000)
                    .temperature(0.1)
                    .build();
            
            return processAIResponse(service, chatRequest, messages);
            
        } catch (Exception e) {
            log.error("AI服务调用失败", e);
            return "AI服务调用失败: " + e.getMessage();
        }
    }
    
    private String buildSystemPrompt() {
        return """
                你是一个智能内容审核助手，负责协调内容审核发布流程。
                
                你有以下功能可以调用：
                1. upload_content - 上传内容
                2. check_sensitive_words - 敏感词检测
                3. recognize_image - 图像识别
                4. submit_manual_review - 提交人工审核
                5. check_manual_review - 检查人工审核结果
                6. publish_content - 发布内容
                7. send_notification - 发送通知
                
                审核流程规则：
                - 对于纯文本内容：上传 → 敏感词检测 → 发布 → 通知
                - 对于图片内容：上传 → 敏感词检测 → 图像识别 → 发布 → 通知
                - 对于复杂内容：根据情况决定是否需要人工审核
                - 如果任何环节检测失败，终止流程并通知用户
                
                请根据用户的请求，智能地选择合适的函数调用顺序来完成内容审核发布流程。
                """;
    }
    
    private List<ChatFunction> convertFunctionDefinitions() {
        List<ChatFunction> functions = new ArrayList<>();
        
        for (FunctionDefinition def : functionRegistry.getAllFunctionDefinitions()) {
            ChatFunction function = ChatFunction.builder()
                    .name(def.getName())
                    .description(def.getDescription())
                    .build();
                    
            // 使用反射设置parameters，因为parameters字段是private的
            try {
                java.lang.reflect.Field parametersField = ChatFunction.class.getDeclaredField("parameters");
                parametersField.setAccessible(true);
                parametersField.set(function, def.getParameters());
            } catch (Exception e) {
                log.warn("设置函数参数失败: {}", def.getName(), e);
            }
            
            functions.add(function);
        }
        
        return functions;
    }
    
    private String processAIResponse(OpenAiService service, ChatCompletionRequest chatRequest, List<ChatMessage> messages) {
        StringBuilder result = new StringBuilder();
        int maxIterations = 10; // 防止无限循环
        int currentIteration = 0;
        
        while (currentIteration < maxIterations) {
            currentIteration++;
            
            var response = service.createChatCompletion(chatRequest);
            ChatCompletionChoice choice = response.getChoices().get(0);
            ChatMessage assistantMessage = choice.getMessage();
            
            if (assistantMessage.getFunctionCall() != null) {
                // AI选择调用函数
                var functionCall = assistantMessage.getFunctionCall();
                log.info("AI决定调用函数: {}", functionCall.getName());
                
                // 执行函数调用
                FunctionResult functionResult = executeFunctionCall(functionCall);
                
                // 将函数调用和结果添加到对话历史
                messages.add(assistantMessage);
                messages.add(new ChatMessage(ChatMessageRole.FUNCTION.value(), 
                    formatFunctionResult(functionResult), functionCall.getName()));
                
                result.append("执行函数: ").append(functionCall.getName()).append("\n");
                result.append("结果: ").append(functionResult.isSuccess() ? "成功" : "失败").append("\n");
                
                if (!functionResult.isSuccess()) {
                    result.append("错误信息: ").append(functionResult.getErrorMessage()).append("\n");
                    break; // 如果函数执行失败，终止流程
                }
                
                // 继续对话
                chatRequest = ChatCompletionRequest.builder()
                        .model(model)
                        .messages(messages)
                        .functions(convertFunctionDefinitions())
                        .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                        .maxTokens(2000)
                        .temperature(0.1)
                        .build();
                        
            } else {
                // AI给出最终回复
                result.append("AI最终回复: ").append(assistantMessage.getContent());
                break;
            }
        }
        
        if (currentIteration >= maxIterations) {
            result.append("\n注意: 达到最大迭代次数，流程可能未完全完成");
        }
        
        return result.toString();
    }
    
    private FunctionResult executeFunctionCall(ChatFunctionCall functionCall) {
        try {
            JsonNode argumentsNode = functionCall.getArguments();
            Map<String, Object> arguments;
            
            if (argumentsNode.isTextual()) {
                // 如果是字符串，先解析为JSON
                arguments = objectMapper.readValue(argumentsNode.asText(), Map.class);
            } else {
                // 如果已经是JsonNode，直接转换
                arguments = objectMapper.convertValue(argumentsNode, Map.class);
            }
            
            FunctionCall call = FunctionCall.builder()
                    .name(functionCall.getName())
                    .arguments(arguments)
                    .build();
                    
            return functionRegistry.executeFunction(call);
            
        } catch (Exception e) {
            log.error("解析函数参数失败", e);
            return FunctionResult.error(functionCall.getName(), "参数解析失败: " + e.getMessage());
        }
    }
    
    private String formatFunctionResult(FunctionResult result) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "success", result.isSuccess(),
                "result", result.getResult(),
                "error", result.getErrorMessage()
            ));
        } catch (JsonProcessingException e) {
            return "结果序列化失败";
        }
    }
}