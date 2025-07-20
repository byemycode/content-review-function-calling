package com.xiaofuge.ai.claude;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ClaudeApiClient {
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.claude.api-key}")
    private String apiKey;
    
    @Value("${ai.claude.model:claude-3-haiku-20240307}")
    private String model;
    
    @Value("${ai.claude.base-url:https://api.anthropic.com}")
    private String baseUrl;
    
    public ClaudeApiClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public ClaudeResponse createMessage(ClaudeRequest request) throws IOException {
        String requestBody = objectMapper.writeValueAsString(Map.of(
            "model", model,
            "max_tokens", request.getMaxTokens(),
            "temperature", request.getTemperature(),
            "messages", request.getMessages(),
            "tools", request.getTools(),
            "tool_choice", request.getToolChoice(),
            "system", request.getSystemPrompt()
        ));
        
        Request httpRequest = new Request.Builder()
                .url(baseUrl + "/v1/messages")
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2024-06-01")
                .header("anthropic-beta", "tools-2024-04-04")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build();
        
        log.debug("发送Claude API请求: {}", requestBody);
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                log.error("Claude API调用失败: HTTP {}, Body: {}", response.code(), errorBody);
                throw new IOException("Claude API调用失败: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            log.debug("Claude API响应: {}", responseBody);
            
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            return parseClaudeResponse(jsonResponse);
        }
    }
    
    private ClaudeResponse parseClaudeResponse(JsonNode response) {
        ClaudeResponse.ClaudeResponseBuilder builder = ClaudeResponse.builder();
        
        builder.id(response.path("id").asText())
               .model(response.path("model").asText())
               .role(response.path("role").asText())
               .stopReason(response.path("stop_reason").asText());
        
        JsonNode contentArray = response.path("content");
        if (contentArray.isArray() && contentArray.size() > 0) {
            JsonNode firstContent = contentArray.get(0);
            String type = firstContent.path("type").asText();
            
            if ("text".equals(type)) {
                builder.content(firstContent.path("text").asText());
            } else if ("tool_use".equals(type)) {
                ClaudeToolUse toolUse = ClaudeToolUse.builder()
                        .id(firstContent.path("id").asText())
                        .name(firstContent.path("name").asText())
                        .input(firstContent.path("input"))
                        .build();
                builder.toolUse(toolUse);
            }
        }
        
        return builder.build();
    }
}