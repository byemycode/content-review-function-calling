package com.xiaofuge.ai.claude;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaudeToolUse {
    private String id;
    private String name;
    private JsonNode input;
}