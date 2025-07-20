package com.xiaofuge.ai.claude;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ClaudeRequest {
    private String systemPrompt;
    private List<Map<String, Object>> messages;
    private List<Map<String, Object>> tools;
    private Map<String, Object> toolChoice;
    private Integer maxTokens;
    private Double temperature;
}