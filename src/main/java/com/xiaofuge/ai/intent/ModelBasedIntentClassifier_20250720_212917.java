package com.xiaofuge.ai.intent;

import com.xiaofuge.ai.claude.ClaudeApiClient;
import com.xiaofuge.ai.claude.ClaudeRequest;
import com.xiaofuge.ai.claude.ClaudeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;

import java.util.*;

@Component("modelBasedIntentClassifier")
@RequiredArgsConstructor
@Slf4j
public class ModelBasedIntentClassifier {
    
    private final ClaudeApiClient claudeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 意图示例库 - Few-Shot Learning
    private static final Map<UserIntent, List<String>> INTENT_EXAMPLES = Map.of(
        UserIntent.CONTENT_PUBLISH, Arrays.asList(
            "我想发布一篇技术文章",
            "帮我上传这张图片",
            "要提交一个视频内容",
            "想分享我写的博客"
        ),
        UserIntent.CONTENT_REVIEW, Arrays.asList(
            "请审核这个内容",
            "帮我检查一下是否符合规范",
            "这个图片能过审吗",
            "验证一下内容质量"
        ),
        UserIntent.GREETING, Arrays.asList(
            "你好",
            "Hi there",
            "早上好",
            "Hello"
        ),
        UserIntent.QUESTION, Arrays.asList(
            "怎么发布文章？",
            "支持什么格式？",
            "审核需要多长时间？",
            "如何修改内容？"
        ),
        UserIntent.UNRELATED, Arrays.asList(
            "今天天气怎么样？",
            "1+1等于几？",
            "北京有什么好玩的？",
            "推荐个餐厅"
        )
    );
    
    public IntentResult classifyIntentWithModel(String userInput) {
        if (StrUtil.isBlank(userInput)) {
            return IntentResult.of(UserIntent.UNKNOWN, 0.0, "输入为空");
        }
        
        try {
            log.debug("使用模型进行意图识别: {}", userInput);
            
            // 构建Few-Shot Prompt
            String systemPrompt = buildFewShotSystemPrompt();
            
            ClaudeRequest request = ClaudeRequest.builder()
                    .systemPrompt(systemPrompt)
                    .messages(List.of(Map.of(
                        "role", "user", 
                        "content", String.format("请分析以下用户输入的意图：\n\n用户输入：\"%s\"\n\n请严格按照JSON格式返回结果。", userInput)
                    )))
                    .maxTokens(500)
                    .temperature(0.1)
                    .build();
            
            ClaudeResponse response = claudeClient.createMessage(request);
            
            if (response.getContent() != null) {
                return parseIntentResponse(response.getContent(), userInput);
            } else {
                log.warn("Claude意图识别响应为空");
                return IntentResult.of(UserIntent.UNKNOWN, 0.0, "模型响应为空");
            }
            
        } catch (Exception e) {
            log.error("模型意图识别失败，降级到规则方法", e);
            // 降级到规则方法
            return fallbackToRuleBasedClassification(userInput);
        }
    }
    
    private String buildFewShotSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                你是一个专业的意图识别模型，专门用于识别用户在内容审核发布系统中的意图。
                
                ## 意图分类体系
                1. CONTENT_PUBLISH - 用户想要发布/上传/提交内容
                2. CONTENT_REVIEW - 用户想要审核/检查现有内容
                3. GREETING - 问候语或打招呼
                4. QUESTION - 询问如何使用系统或相关问题
                5. UNRELATED - 与内容发布审核完全无关的话题
                6. UNKNOWN - 无法明确分类的输入
                
                ## Few-Shot学习示例
                """);
        
        // 添加Few-Shot示例
        INTENT_EXAMPLES.forEach((intent, examples) -> {
            prompt.append(String.format("\n### %s 示例：\n", intent.name()));
            examples.forEach(example -> 
                prompt.append(String.format("用户输入：\"%s\" -> 意图：%s\n", example, intent.name()))
            );
        });
        
        prompt.append("""
                
                ## 分析要求
                1. 仔细分析用户输入的语义和上下文
                2. 考虑用户的真实意图，而不仅仅是关键词
                3. 给出0-1之间的置信度分数
                4. 提供简短的分析理由
                
                ## 输出格式
                请严格按照以下JSON格式返回结果：
                {
                  "intent": "CONTENT_PUBLISH",
                  "confidence": 0.95,
                  "reasoning": "用户明确表达了发布文章的意图"
                }
                
                ## 特别注意
                - 即使包含发布、内容等词汇，也要判断真实意图
                - 例如"我昨天发布了工资"应该分类为UNRELATED
                - 置信度低于0.6时请分类为对应的低置信度类别
                """);
        
        return prompt.toString();
    }
    
    private IntentResult parseIntentResponse(String response, String originalInput) {
        try {
            // 提取JSON部分
            String jsonStr = extractJsonFromResponse(response);
            
            Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);
            
            String intentStr = (String) result.get("intent");
            Object confidenceObj = result.get("confidence");
            String reasoning = (String) result.get("reasoning");
            
            UserIntent intent = UserIntent.valueOf(intentStr);
            double confidence = confidenceObj instanceof Number ? 
                ((Number) confidenceObj).doubleValue() : 0.0;
            
            log.info("模型意图识别结果: {} (置信度: {:.2f}) - {}", 
                    intent.name(), confidence, reasoning);
            
            return IntentResult.of(intent, confidence, reasoning);
            
        } catch (Exception e) {
            log.error("解析意图识别结果失败: {}", response, e);
            return fallbackToRuleBasedClassification(originalInput);
        }
    }
    
    private String extractJsonFromResponse(String response) {
        // 找到JSON开始和结束位置
        int jsonStart = response.indexOf("{");
        int jsonEnd = response.lastIndexOf("}");
        
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return response.substring(jsonStart, jsonEnd + 1);
        }
        
        throw new IllegalArgumentException("响应中未找到有效的JSON格式");
    }
    
    private IntentResult fallbackToRuleBasedClassification(String userInput) {
        log.info("使用规则方法作为后备方案");
        
        // 简化的规则方法
        String normalizedInput = userInput.toLowerCase().trim();
        
        if (containsAny(normalizedInput, Arrays.asList("发布", "上传", "提交", "分享"))) {
            if (containsAny(normalizedInput, Arrays.asList("文章", "内容", "图片", "视频"))) {
                return IntentResult.of(UserIntent.CONTENT_PUBLISH, 0.7, "规则匹配：发布相关");
            }
        }
        
        if (containsAny(normalizedInput, Arrays.asList("审核", "检查", "验证"))) {
            return IntentResult.of(UserIntent.CONTENT_REVIEW, 0.7, "规则匹配：审核相关");
        }
        
        if (containsAny(normalizedInput, Arrays.asList("你好", "hello", "hi"))) {
            return IntentResult.of(UserIntent.GREETING, 0.8, "规则匹配：问候语");
        }
        
        if (normalizedInput.contains("?") || normalizedInput.contains("？") || 
            containsAny(normalizedInput, Arrays.asList("怎么", "如何", "什么"))) {
            return IntentResult.of(UserIntent.QUESTION, 0.6, "规则匹配：问题询问");
        }
        
        return IntentResult.of(UserIntent.UNRELATED, 0.5, "规则匹配：无法分类");
    }
    
    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
    
    // 意图识别结果封装
    public static class IntentResult {
        private UserIntent intent;
        private double confidence;
        private String reasoning;
        
        private IntentResult(UserIntent intent, double confidence, String reasoning) {
            this.intent = intent;
            this.confidence = confidence;
            this.reasoning = reasoning;
        }
        
        public static IntentResult of(UserIntent intent, double confidence, String reasoning) {
            return new IntentResult(intent, confidence, reasoning);
        }
        
        // Getters
        public UserIntent getIntent() { return intent; }
        public double getConfidence() { return confidence; }
        public String getReasoning() { return reasoning; }
        
        @Override
        public String toString() {
            return String.format("IntentResult{intent=%s, confidence=%.2f, reasoning='%s'}", 
                    intent, confidence, reasoning);
        }
    }
}