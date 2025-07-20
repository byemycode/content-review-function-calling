package com.xiaofuge.ai.intent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HybridIntentClassifier {
    
    private final IntentClassifier ruleBasedClassifier;
    private final ModelBasedIntentClassifier modelBasedClassifier;
    
    @Value("${ai.intent.strategy:hybrid}")
    private String intentStrategy;
    
    @Value("${ai.intent.confidence-threshold:0.7}")
    private double confidenceThreshold;
    
    /**
     * 混合意图识别策略
     * 1. 优先使用模型识别
     * 2. 低置信度时结合规则方法
     * 3. 模型失败时降级到规则方法
     */
    public IntentClassificationResult classifyIntent(String userInput) {
        log.debug("开始混合意图识别，策略: {}", intentStrategy);
        
        switch (intentStrategy.toLowerCase()) {
            case "model":
                return useModelOnly(userInput);
            case "rule":
                return useRuleOnly(userInput);
            case "hybrid":
            default:
                return useHybridStrategy(userInput);
        }
    }
    
    private IntentClassificationResult useModelOnly(String userInput) {
        log.debug("使用纯模型策略");
        ModelBasedIntentClassifier.IntentResult result = 
            modelBasedClassifier.classifyIntentWithModel(userInput);
        
        return IntentClassificationResult.builder()
                .intent(result.getIntent())
                .confidence(result.getConfidence())
                .reasoning(result.getReasoning())
                .method("model")
                .build();
    }
    
    private IntentClassificationResult useRuleOnly(String userInput) {
        log.debug("使用纯规则策略");
        UserIntent intent = ruleBasedClassifier.classifyIntent(userInput);
        double confidence = ruleBasedClassifier.getConfidenceScore(userInput, intent);
        
        return IntentClassificationResult.builder()
                .intent(intent)
                .confidence(confidence)
                .reasoning("基于规则的意图识别")
                .method("rule")
                .build();
    }
    
    private IntentClassificationResult useHybridStrategy(String userInput) {
        log.debug("使用混合策略进行意图识别");
        
        try {
            // 第一步：模型识别
            ModelBasedIntentClassifier.IntentResult modelResult = 
                modelBasedClassifier.classifyIntentWithModel(userInput);
            
            // 第二步：规则验证
            UserIntent ruleIntent = ruleBasedClassifier.classifyIntent(userInput);
            double ruleConfidence = ruleBasedClassifier.getConfidenceScore(userInput, ruleIntent);
            
            // 第三步：结果融合
            return fuseResults(userInput, modelResult, ruleIntent, ruleConfidence);
            
        } catch (Exception e) {
            log.error("混合策略失败，降级到规则方法", e);
            return useRuleOnly(userInput);
        }
    }
    
    private IntentClassificationResult fuseResults(
            String userInput,
            ModelBasedIntentClassifier.IntentResult modelResult, 
            UserIntent ruleIntent, 
            double ruleConfidence) {
        
        UserIntent modelIntent = modelResult.getIntent();
        double modelConfidence = modelResult.getConfidence();
        
        log.debug("模型结果: {} (置信度: {:.2f})", modelIntent, modelConfidence);
        log.debug("规则结果: {} (置信度: {:.2f})", ruleIntent, ruleConfidence);
        
        // 策略1：高置信度模型结果直接采用
        if (modelConfidence >= confidenceThreshold) {
            log.debug("模型置信度高，直接采用模型结果");
            return IntentClassificationResult.builder()
                    .intent(modelIntent)
                    .confidence(modelConfidence)
                    .reasoning(modelResult.getReasoning() + " (高置信度模型识别)")
                    .method("model-high-confidence")
                    .build();
        }
        
        // 策略2：模型与规则一致时，增强置信度
        if (modelIntent == ruleIntent) {
            double enhancedConfidence = Math.min(0.95, (modelConfidence + ruleConfidence) / 2 + 0.1);
            log.debug("模型与规则结果一致，增强置信度: {:.2f}", enhancedConfidence);
            
            return IntentClassificationResult.builder()
                    .intent(modelIntent)
                    .confidence(enhancedConfidence)
                    .reasoning(String.format("%s (模型+规则一致)", modelResult.getReasoning()))
                    .method("model-rule-consensus")
                    .build();
        }
        
        // 策略3：冲突解决 - 优先考虑更高置信度
        if (modelConfidence > ruleConfidence) {
            log.debug("模型置信度更高，采用模型结果");
            return IntentClassificationResult.builder()
                    .intent(modelIntent)
                    .confidence(modelConfidence * 0.9) // 轻微降权
                    .reasoning(String.format("%s (模型vs规则冲突，选择模型)", modelResult.getReasoning()))
                    .method("model-rule-conflict-model-wins")
                    .build();
        } else {
            log.debug("规则置信度更高，采用规则结果");
            return IntentClassificationResult.builder()
                    .intent(ruleIntent)
                    .confidence(ruleConfidence * 0.9) // 轻微降权
                    .reasoning("基于规则的意图识别 (模型vs规则冲突，选择规则)")
                    .method("model-rule-conflict-rule-wins")
                    .build();
        }
    }
    
    public boolean isHighConfidence(IntentClassificationResult result) {
        return result.getConfidence() >= confidenceThreshold;
    }
    
    public boolean needsClarification(IntentClassificationResult result) {
        return result.getConfidence() < 0.5 || 
               result.getIntent() == UserIntent.UNKNOWN ||
               result.getIntent() == UserIntent.CONTENT_RELATED;
    }
}