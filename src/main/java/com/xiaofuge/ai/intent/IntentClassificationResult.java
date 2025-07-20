package com.xiaofuge.ai.intent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntentClassificationResult {
    private UserIntent intent;
    private double confidence;
    private String reasoning;
    private String method;
    
    @Override
    public String toString() {
        return String.format("IntentResult{intent=%s, confidence=%.2f, method=%s, reasoning='%s'}", 
                intent, confidence, method, reasoning);
    }
}