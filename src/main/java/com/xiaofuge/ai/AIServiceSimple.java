package com.xiaofuge.ai;

import com.xiaofuge.functioncalling.FunctionCall;
import com.xiaofuge.functioncalling.FunctionRegistry;
import com.xiaofuge.functioncalling.FunctionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("aiServiceSimple")
@RequiredArgsConstructor
@Slf4j
public class AIServiceSimple {
    
    private final FunctionRegistry functionRegistry;
    
    public String processContentReview(String userPrompt) {
        log.info("收到用户请求: {}", userPrompt);
        
        try {
            // 简化的AI逻辑：根据用户输入智能选择执行流程
            if (containsKeywords(userPrompt, "上传", "发布", "文章", "内容")) {
                return executeContentPublishFlow(userPrompt);
            } else if (containsKeywords(userPrompt, "审核", "检查")) {
                return executeContentReviewFlow(userPrompt);
            } else {
                return "请提供更具体的内容审核请求，例如：'我想发布一篇技术文章'";
            }
        } catch (Exception e) {
            log.error("处理内容审核请求失败", e);
            return "处理失败: " + e.getMessage();
        }
    }
    
    private String executeContentPublishFlow(String userPrompt) {
        StringBuilder result = new StringBuilder("开始执行内容发布流程:\n\n");
        
        try {
            // 1. 上传内容
            log.info("步骤1: 上传内容");
            FunctionResult uploadResult = functionRegistry.executeFunction(
                FunctionCall.builder()
                    .name("upload_content")
                    .arguments(Map.of(
                        "title", extractTitle(userPrompt),
                        "textContent", "这是从用户请求中提取的内容: " + userPrompt,
                        "authorId", "demo_user_001"
                    ))
                    .build()
            );
            
            if (!uploadResult.isSuccess()) {
                return result.append("上传失败: ").append(uploadResult.getErrorMessage()).toString();
            }
            
            result.append("✅ 内容上传成功\n");
            Long contentId = extractContentId(uploadResult.getResult());
            
            // 2. 敏感词检测
            log.info("步骤2: 敏感词检测");
            FunctionResult sensitiveResult = functionRegistry.executeFunction(
                FunctionCall.builder()
                    .name("check_sensitive_words")
                    .arguments(Map.of("contentId", contentId))
                    .build()
            );
            
            if (!sensitiveResult.isSuccess()) {
                return result.append("❌ 敏感词检测失败: ").append(sensitiveResult.getErrorMessage()).toString();
            }
            
            result.append("✅ 敏感词检测通过\n");
            
            // 3. 如果有图片，进行图像识别
            if (containsKeywords(userPrompt, "图片", "图像", "照片")) {
                log.info("步骤3: 图像识别");
                FunctionResult imageResult = functionRegistry.executeFunction(
                    FunctionCall.builder()
                        .name("recognize_image")
                        .arguments(Map.of("contentId", contentId))
                        .build()
                );
                
                if (!imageResult.isSuccess()) {
                    return result.append("❌ 图像识别失败: ").append(imageResult.getErrorMessage()).toString();
                }
                
                result.append("✅ 图像识别通过\n");
            }
            
            // 4. 发布内容
            log.info("步骤4: 发布内容");
            FunctionResult publishResult = functionRegistry.executeFunction(
                FunctionCall.builder()
                    .name("publish_content")
                    .arguments(Map.of("contentId", contentId))
                    .build()
            );
            
            if (!publishResult.isSuccess()) {
                return result.append("❌ 内容发布失败: ").append(publishResult.getErrorMessage()).toString();
            }
            
            result.append("✅ 内容发布成功\n");
            
            // 5. 发送通知
            log.info("步骤5: 发送通知");
            FunctionResult notifyResult = functionRegistry.executeFunction(
                FunctionCall.builder()
                    .name("send_notification")
                    .arguments(Map.of(
                        "contentId", contentId,
                        "type", "EMAIL",
                        "recipient", "demo_user_001@example.com",
                        "message", "您的内容已成功发布!"
                    ))
                    .build()
            );
            
            if (notifyResult.isSuccess()) {
                result.append("✅ 通知发送成功\n");
            }
            
            result.append("\n🎉 内容发布流程完成!");
            
        } catch (Exception e) {
            result.append("\n❌ 流程执行出错: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    private String executeContentReviewFlow(String userPrompt) {
        StringBuilder result = new StringBuilder("开始执行内容审核流程:\n\n");
        
        // 模拟审核已有内容
        Long contentId = 1L; // 假设检查ID为1的内容
        
        try {
            // 检查敏感词
            FunctionResult sensitiveResult = functionRegistry.executeFunction(
                FunctionCall.builder()
                    .name("check_sensitive_words")
                    .arguments(Map.of("contentId", contentId))
                    .build()
            );
            
            result.append("敏感词检测结果: ")
                  .append(sensitiveResult.isSuccess() ? "✅ 通过" : "❌ 未通过")
                  .append("\n");
            
            // 图像识别
            FunctionResult imageResult = functionRegistry.executeFunction(
                FunctionCall.builder()
                    .name("recognize_image")
                    .arguments(Map.of("contentId", contentId))
                    .build()
            );
            
            result.append("图像识别结果: ")
                  .append(imageResult.isSuccess() ? "✅ 通过" : "❌ 未通过")
                  .append("\n");
                  
        } catch (Exception e) {
            result.append("❌ 审核过程出错: ").append(e.getMessage());
        }
        
        return result.toString();
    }
    
    private boolean containsKeywords(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String extractTitle(String userPrompt) {
        // 简单的标题提取逻辑
        Pattern titlePattern = Pattern.compile("标题[是为]?[\"']?([^\"'，。！]+)[\"']?");
        Matcher matcher = titlePattern.matcher(userPrompt);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 如果没有明确标题，生成一个默认标题
        if (containsKeywords(userPrompt, "技术", "Java", "Spring")) {
            return "技术分享文章";
        } else if (containsKeywords(userPrompt, "产品", "宣传")) {
            return "产品宣传内容";
        } else {
            return "用户发布内容";
        }
    }
    
    private Long extractContentId(Object result) {
        // 从上传结果中提取content ID
        if (result != null && result.toString().contains("contentId")) {
            try {
                // 简单的ID提取逻辑，实际项目中会更复杂
                String resultStr = result.toString();
                Pattern idPattern = Pattern.compile("contentId=(\\d+)");
                Matcher matcher = idPattern.matcher(resultStr);
                if (matcher.find()) {
                    return Long.parseLong(matcher.group(1));
                }
            } catch (Exception e) {
                log.warn("提取contentId失败", e);
            }
        }
        return 1L; // 默认返回1
    }
}