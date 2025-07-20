package com.xiaofuge.ai;

import com.xiaofuge.ai.claude.*;
import com.xiaofuge.ai.intent.HybridIntentClassifier;
import com.xiaofuge.ai.intent.IntentClassificationResult;
import com.xiaofuge.ai.intent.UserIntent;
import com.xiaofuge.functioncalling.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaudeAIService {
    
    private final ClaudeApiClient claudeClient;
    private final FunctionRegistry functionRegistry;
    private final HybridIntentClassifier intentClassifier;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public String processContentReview(String userPrompt) {
        log.info("Claude处理用户请求: {}", userPrompt);
        
        try {
            // 第一步：混合意图识别和预处理
            IntentClassificationResult intentResult = intentClassifier.classifyIntent(userPrompt);
            UserIntent intent = intentResult.getIntent();
            double confidence = intentResult.getConfidence();
            
            log.info("混合意图识别结果: {} - {}", intentResult, intentResult.getReasoning());
            
            // 第二步：基于意图的响应策略
            String preResponse = handleIntentPreprocessing(userPrompt, intentResult);
            if (preResponse != null) {
                return preResponse;
            }
            
            // 第三步：调用Claude进行Function Calling
            return executeClaudeFunctionCalling(userPrompt, intent);
            
        } catch (Exception e) {
            log.error("Claude AI服务调用失败", e);
            return "抱歉，处理您的请求时遇到了问题：" + e.getMessage();
        }
    }
    
    private String handleIntentPreprocessing(String userPrompt, IntentClassificationResult intentResult) {
        UserIntent intent = intentResult.getIntent();
        double confidence = intentResult.getConfidence();
        switch (intent) {
            case GREETING:
                return buildGreetingResponse();
                
            case QUESTION:
                if (!isContentRelatedQuestion(userPrompt)) {
                    return buildUnrelatedQuestionResponse();
                }
                break;
                
            case NEGATIVE:
                return "好的，我明白您不需要相关服务。如果之后有内容发布或审核需求，请随时告诉我。";
                
            case UNRELATED:
                return buildUnrelatedResponse();
                
            case CONTENT_RELATED:
                if (confidence < 0.3) {
                    return buildClarificationResponse(userPrompt);
                }
                break;
                
            case CONTENT_PUBLISH:
            case CONTENT_REVIEW:
                if (confidence < 0.4) {
                    return buildLowConfidenceResponse(userPrompt, intent);
                }
                break;
        }
        
        return null; // 继续使用Claude处理
    }
    
    private String executeClaudeFunctionCalling(String userPrompt, UserIntent intent) throws Exception {
        // 构建Claude请求
        ClaudeRequest request = ClaudeRequest.builder()
                .systemPrompt(buildEnhancedSystemPrompt(intent))
                .messages(List.of(Map.of(
                    "role", "user",
                    "content", userPrompt
                )))
                .tools(convertFunctionDefinitions())
                .toolChoice(Map.of("type", "auto"))
                .maxTokens(2000)
                .temperature(0.1)
                .build();
        
        return processClaudeResponse(request, userPrompt);
    }
    
    private String processClaudeResponse(ClaudeRequest request, String originalPrompt) throws Exception {
        List<Map<String, Object>> conversationHistory = new ArrayList<>(request.getMessages());
        StringBuilder result = new StringBuilder();
        int maxIterations = 8;
        int currentIteration = 0;
        
        while (currentIteration < maxIterations) {
            currentIteration++;
            
            // 更新请求的消息历史
            ClaudeRequest currentRequest = ClaudeRequest.builder()
                    .systemPrompt(request.getSystemPrompt())
                    .messages(conversationHistory)
                    .tools(request.getTools())
                    .toolChoice(request.getToolChoice())
                    .maxTokens(request.getMaxTokens())
                    .temperature(request.getTemperature())
                    .build();
            
            ClaudeResponse response = claudeClient.createMessage(currentRequest);
            
            if (response.getToolUse() != null) {
                // Claude选择使用工具
                ClaudeToolUse toolUse = response.getToolUse();
                log.info("Claude决定调用函数: {}", toolUse.getName());
                
                // 执行函数调用
                FunctionResult functionResult = executeFunctionCall(toolUse);
                
                // 添加assistant消息
                conversationHistory.add(Map.of(
                    "role", "assistant",
                    "content", List.of(Map.of(
                        "type", "tool_use",
                        "id", toolUse.getId(),
                        "name", toolUse.getName(),
                        "input", toolUse.getInput()
                    ))
                ));
                
                // 添加工具结果消息
                conversationHistory.add(Map.of(
                    "role", "user",
                    "content", List.of(Map.of(
                        "type", "tool_result",
                        "tool_use_id", toolUse.getId(),
                        "content", formatFunctionResult(functionResult)
                    ))
                ));
                
                result.append(String.format("🔧 执行: %s - %s\n", 
                    toolUse.getName(), 
                    functionResult.isSuccess() ? "✅ 成功" : "❌ 失败"
                ));
                
                if (!functionResult.isSuccess()) {
                    result.append(String.format("错误: %s\n", functionResult.getErrorMessage()));
                    break;
                }
                
            } else if (response.getContent() != null) {
                // Claude给出最终回复
                result.append("\n🤖 ").append(response.getContent());
                break;
            } else {
                log.warn("Claude响应格式异常: {}", response);
                break;
            }
        }
        
        if (currentIteration >= maxIterations) {
            result.append("\n⚠️ 注意: 达到最大处理步骤，流程可能未完全完成");
        }
        
        return result.toString();
    }
    
    private String buildEnhancedSystemPrompt(UserIntent intent) {
        String basePrompt = """
                你是一个专业的内容审核与发布助手，负责协调智能化内容管理流程。
                
                ## 核心职责
                1. 准确理解用户的内容发布或审核需求
                2. 根据内容类型和复杂度选择合适的处理流程
                3. 智能调用相关工具完成任务
                4. 提供清晰的执行反馈
                
                ## 可用工具
                - upload_content: 上传内容到系统
                - check_sensitive_words: 敏感词检测
                - recognize_image: 图像识别审核
                - submit_manual_review: 提交人工审核
                - check_manual_review: 检查人工审核结果
                - publish_content: 发布已审核内容
                - send_notification: 发送通知
                
                ## 智能流程规则
                ### 文本内容流程
                1. upload_content → 2. check_sensitive_words → 3. publish_content → 4. send_notification
                
                ### 图像内容流程
                1. upload_content → 2. check_sensitive_words → 3. recognize_image → 4. publish_content → 5. send_notification
                
                ### 复杂/敏感内容流程
                1. upload_content → 2. check_sensitive_words → 3. recognize_image → 4. submit_manual_review → 5. check_manual_review → 6. publish_content → 7. send_notification
                
                ## 决策逻辑
                - 如果敏感词检测失败，终止流程并说明原因
                - 如果图像识别检测到违规内容，转入人工审核
                - 如果内容涉及复杂主题（社会、政治、争议话题），主动提交人工审核
                - 所有审核通过后才能发布
                
                ## 响应风格
                - 专业、简洁、友好
                - 清楚说明每个步骤的执行情况
                - 如果遇到问题，提供具体的解决建议
                """;
        
        // 根据意图添加特定指导
        return basePrompt + buildIntentSpecificGuidance(intent);
    }
    
    private String buildIntentSpecificGuidance(UserIntent intent) {
        return switch (intent) {
            case CONTENT_PUBLISH -> """
                
                ## 当前场景：内容发布
                用户想要发布内容，请：
                1. 仔细提取内容信息（标题、正文、媒体链接等）
                2. 根据内容类型选择合适的审核流程
                3. 确保所有审核步骤都正确执行
                4. 发布成功后发送确认通知
                """;
                
            case CONTENT_REVIEW -> """
                
                ## 当前场景：内容审核
                用户想要审核现有内容，请：
                1. 理解用户要审核的具体内容
                2. 执行相应的检测工具
                3. 提供详细的审核报告
                4. 如有问题，给出改进建议
                """;
                
            default -> "";
        };
    }
    
    private List<Map<String, Object>> convertFunctionDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();
        
        for (FunctionDefinition def : functionRegistry.getAllFunctionDefinitions()) {
            Map<String, Object> tool = Map.of(
                "name", def.getName(),
                "description", def.getDescription(),
                "input_schema", def.getParameters()
            );
            tools.add(tool);
        }
        
        return tools;
    }
    
    private FunctionResult executeFunctionCall(ClaudeToolUse toolUse) {
        try {
            Map<String, Object> arguments = objectMapper.convertValue(toolUse.getInput(), Map.class);
            
            FunctionCall call = FunctionCall.builder()
                    .name(toolUse.getName())
                    .arguments(arguments)
                    .build();
                    
            return functionRegistry.executeFunction(call);
            
        } catch (Exception e) {
            log.error("解析函数参数失败", e);
            return FunctionResult.error(toolUse.getName(), "参数解析失败: " + e.getMessage());
        }
    }
    
    private String formatFunctionResult(FunctionResult result) {
        try {
            Map<String, Object> resultMap = Map.of(
                "success", result.isSuccess(),
                "result", result.getResult() != null ? result.getResult() : "",
                "error", result.getErrorMessage() != null ? result.getErrorMessage() : ""
            );
            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            return String.format("{\"success\": %s, \"error\": \"结果序列化失败\"}", result.isSuccess());
        }
    }
    
    // 各种响应构建方法
    private String buildGreetingResponse() {
        return """
                👋 您好！我是智能内容审核助手。
                
                我可以帮您：
                ✅ 发布文章、图片、视频等内容
                ✅ 进行内容安全审核和检测
                ✅ 管理内容发布流程
                
                请告诉我您想要做什么，比如：
                • "我想发布一篇技术文章"
                • "帮我审核这个内容"
                • "上传一张产品图片"
                """;
    }
    
    private String buildUnrelatedQuestionResponse() {
        return """
                🤔 您的问题似乎与内容发布和审核无关。
                
                我专注于帮助您：
                📝 发布和管理内容
                🛡️ 进行内容安全审核
                📤 处理内容发布流程
                
                如果您有相关需求，请告诉我具体想要做什么。
                """;
    }
    
    private String buildUnrelatedResponse() {
        return """
                😊 抱歉，我专门负责内容发布和审核相关的工作。
                
                如果您需要：
                • 发布文章、图片、视频
                • 内容安全检测
                • 审核流程管理
                
                我很乐意为您提供帮助！
                """;
    }
    
    private String buildClarificationResponse(String userPrompt) {
        return String.format("""
                🤔 我理解您提到了内容相关的需求，但需要更多信息来帮助您。
                
                请您明确告诉我：
                1️⃣ 您想要发布什么类型的内容？（文章/图片/视频）
                2️⃣ 具体想要做什么操作？（发布/审核/检查）
                
                原始输入："%s"
                
                举例：
                • "我想发布一篇关于Java的技术文章"
                • "帮我审核这张产品图片"
                """, userPrompt);
    }
    
    private String buildLowConfidenceResponse(String userPrompt, UserIntent intent) {
        return String.format("""
                ⚠️ 我检测到您可能想要%s，但不太确定具体需求。
                
                为了更好地帮助您，请提供：
                • 📋 内容类型（文章/图片/视频等）
                • 🎯 具体操作（发布/审核/检查等）
                • 📝 内容标题或描述
                
                您的输入："%s"
                
                您可以重新描述一下具体需求吗？
                """, intent.getDescription(), userPrompt);
    }
    
    private boolean isContentRelatedQuestion(String prompt) {
        List<String> contentQuestions = Arrays.asList(
            "怎么发布", "如何上传", "怎么审核", "支持什么格式",
            "how to publish", "how to upload", "what format"
        );
        return contentQuestions.stream().anyMatch(prompt.toLowerCase()::contains);
    }
}