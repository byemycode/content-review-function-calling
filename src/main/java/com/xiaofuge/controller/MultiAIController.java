package com.xiaofuge.controller;

import com.xiaofuge.ai.AIService;
import com.xiaofuge.ai.AIServiceSimple;
import com.xiaofuge.ai.ClaudeAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/multi-ai")
@RequiredArgsConstructor
@Slf4j
public class MultiAIController {
    
    private final ClaudeAIService claudeAIService;
    private final AIServiceSimple simpleAIService;
    private final AIService openaiService;
    
    @PostMapping("/claude")
    public Map<String, Object> processWithClaude(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("prompt");
        log.info("使用Claude处理请求: {}", userPrompt);
        
        try {
            String result = claudeAIService.processContentReview(userPrompt);
            return Map.of(
                "success", true,
                "result", result,
                "aiService", "Claude"
            );
        } catch (Exception e) {
            log.error("Claude处理失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "aiService", "Claude"
            );
        }
    }
    
    @PostMapping("/openai")
    public Map<String, Object> processWithOpenAI(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("prompt");
        log.info("使用OpenAI处理请求: {}", userPrompt);
        
        try {
            String result = openaiService.processContentReview(userPrompt);
            return Map.of(
                "success", true,
                "result", result,
                "aiService", "OpenAI"
            );
        } catch (Exception e) {
            log.error("OpenAI处理失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "aiService", "OpenAI"
            );
        }
    }
    
    @PostMapping("/simple")
    public Map<String, Object> processWithSimple(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("prompt");
        log.info("使用Simple AI处理请求: {}", userPrompt);
        
        try {
            String result = simpleAIService.processContentReview(userPrompt);
            return Map.of(
                "success", true,
                "result", result,
                "aiService", "Simple"
            );
        } catch (Exception e) {
            log.error("Simple AI处理失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "aiService", "Simple"
            );
        }
    }
    
    @GetMapping("/compare")
    public Map<String, Object> compareAIServices() {
        return Map.of(
            "aiServices", Map.of(
                "claude", Map.of(
                    "name", "Claude AI",
                    "features", new String[]{
                        "智能意图识别",
                        "零误触发机制", 
                        "上下文理解",
                        "动态流程编排"
                    },
                    "endpoint", "/api/multi-ai/claude"
                ),
                "openai", Map.of(
                    "name", "OpenAI GPT",
                    "features", new String[]{
                        "强大的Function Calling",
                        "多轮对话支持",
                        "完整的工具调用链"
                    },
                    "endpoint", "/api/multi-ai/openai"
                ),
                "simple", Map.of(
                    "name", "Simple AI",
                    "features", new String[]{
                        "无需API Key",
                        "基于规则的流程",
                        "快速响应",
                        "演示用途"
                    },
                    "endpoint", "/api/multi-ai/simple"
                )
            )
        );
    }
}