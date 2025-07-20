package com.xiaofuge.ai.claude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaudeResponse {
    private String id;
    private String model;
    private String role;
    private String content;
    private String stopReason;
    private ClaudeToolUse toolUse;
}