package com.xiaofuge.controller;

import com.xiaofuge.ai.ClaudeAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/content-review")
@RequiredArgsConstructor
@Slf4j
public class ContentReviewController {
    
    private final ClaudeAIService aiService;
    
    @PostMapping("/process")
    public Map<String, Object> processContentReview(@RequestBody Map<String, String> request) {
        String userPrompt = request.get("prompt");
        log.info("接收到内容审核请求: {}", userPrompt);
        
        try {
            String result = aiService.processContentReview(userPrompt);
            return Map.of(
                "success", true,
                "result", result
            );
        } catch (Exception e) {
            log.error("处理内容审核请求失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "healthy",
            "timestamp", System.currentTimeMillis()
        );
    }
}