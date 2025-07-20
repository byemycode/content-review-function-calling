package com.xiaofuge.functioncalling;

import com.xiaofuge.service.*;
import com.xiaofuge.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentReviewFunctions implements ApplicationListener<ApplicationReadyEvent> {
    
    private final FunctionRegistry functionRegistry;
    private final ContentUploadService contentUploadService;
    private final SensitiveWordService sensitiveWordService;
    private final ImageRecognitionService imageRecognitionService;
    private final ManualReviewService manualReviewService;
    private final ContentPublishService contentPublishService;
    private final NotificationService notificationService;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        registerAllFunctions();
        log.info("所有内容审核相关函数已注册完成");
    }
    
    private void registerAllFunctions() {
        registerUploadContentFunction();
        registerCheckSensitiveWordsFunction();
        registerRecognizeImageFunction();
        registerSubmitManualReviewFunction();
        registerCheckManualReviewFunction();
        registerPublishContentFunction();
        registerSendNotificationFunction();
    }
    
    private void registerUploadContentFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("upload_content")
                .description("上传内容到系统")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "title", Map.of("type", "string", "description", "内容标题"),
                                "textContent", Map.of("type", "string", "description", "文本内容"),
                                "imageUrl", Map.of("type", "string", "description", "图片URL"),
                                "videoUrl", Map.of("type", "string", "description", "视频URL"),
                                "authorId", Map.of("type", "string", "description", "作者ID")
                        ),
                        "required", new String[]{"title", "authorId"}
                ))
                .returnType("ContentUploadResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            ContentUploadRequest request = ContentUploadRequest.builder()
                    .title((String) arguments.get("title"))
                    .textContent((String) arguments.get("textContent"))
                    .imageUrl((String) arguments.get("imageUrl"))
                    .videoUrl((String) arguments.get("videoUrl"))
                    .authorId((String) arguments.get("authorId"))
                    .build();
            return contentUploadService.uploadContent(request);
        };
        
        functionRegistry.registerFunction("upload_content", definition, handler);
    }
    
    private void registerCheckSensitiveWordsFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("check_sensitive_words")
                .description("检查内容中的敏感词")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "contentId", Map.of("type", "integer", "description", "内容ID")
                        ),
                        "required", new String[]{"contentId"}
                ))
                .returnType("SensitiveWordCheckResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            Long contentId = Long.valueOf(arguments.get("contentId").toString());
            SensitiveWordCheckRequest request = SensitiveWordCheckRequest.builder()
                    .contentId(contentId)
                    .build();
            return sensitiveWordService.checkSensitiveWords(request);
        };
        
        functionRegistry.registerFunction("check_sensitive_words", definition, handler);
    }
    
    private void registerRecognizeImageFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("recognize_image")
                .description("识别图片内容是否违规")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "contentId", Map.of("type", "integer", "description", "内容ID")
                        ),
                        "required", new String[]{"contentId"}
                ))
                .returnType("ImageRecognitionResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            Long contentId = Long.valueOf(arguments.get("contentId").toString());
            ImageRecognitionRequest request = ImageRecognitionRequest.builder()
                    .contentId(contentId)
                    .build();
            return imageRecognitionService.recognizeImage(request);
        };
        
        functionRegistry.registerFunction("recognize_image", definition, handler);
    }
    
    private void registerSubmitManualReviewFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("submit_manual_review")
                .description("提交内容进行人工审核")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "contentId", Map.of("type", "integer", "description", "内容ID"),
                                "reviewReason", Map.of("type", "string", "description", "审核原因")
                        ),
                        "required", new String[]{"contentId"}
                ))
                .returnType("ManualReviewResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            Long contentId = Long.valueOf(arguments.get("contentId").toString());
            ManualReviewRequest request = ManualReviewRequest.builder()
                    .contentId(contentId)
                    .reviewReason((String) arguments.get("reviewReason"))
                    .build();
            return manualReviewService.submitForManualReview(request);
        };
        
        functionRegistry.registerFunction("submit_manual_review", definition, handler);
    }
    
    private void registerCheckManualReviewFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("check_manual_review")
                .description("检查人工审核结果")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "contentId", Map.of("type", "integer", "description", "内容ID")
                        ),
                        "required", new String[]{"contentId"}
                ))
                .returnType("ManualReviewResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            Long contentId = Long.valueOf(arguments.get("contentId").toString());
            return manualReviewService.checkManualReviewResult(contentId);
        };
        
        functionRegistry.registerFunction("check_manual_review", definition, handler);
    }
    
    private void registerPublishContentFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("publish_content")
                .description("发布审核通过的内容")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "contentId", Map.of("type", "integer", "description", "内容ID")
                        ),
                        "required", new String[]{"contentId"}
                ))
                .returnType("ContentPublishResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            Long contentId = Long.valueOf(arguments.get("contentId").toString());
            ContentPublishRequest request = ContentPublishRequest.builder()
                    .contentId(contentId)
                    .build();
            return contentPublishService.publishContent(request);
        };
        
        functionRegistry.registerFunction("publish_content", definition, handler);
    }
    
    private void registerSendNotificationFunction() {
        FunctionDefinition definition = FunctionDefinition.builder()
                .name("send_notification")
                .description("发送通知给用户")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "contentId", Map.of("type", "integer", "description", "内容ID"),
                                "type", Map.of("type", "string", "description", "通知类型(EMAIL/SMS/PUSH/WECHAT)"),
                                "recipient", Map.of("type", "string", "description", "接收者"),
                                "message", Map.of("type", "string", "description", "通知消息")
                        ),
                        "required", new String[]{"contentId", "type", "recipient", "message"}
                ))
                .returnType("NotificationResponse")
                .build();
        
        FunctionHandler handler = (arguments) -> {
            Long contentId = Long.valueOf(arguments.get("contentId").toString());
            NotificationRequest request = NotificationRequest.builder()
                    .contentId(contentId)
                    .type((String) arguments.get("type"))
                    .recipient((String) arguments.get("recipient"))
                    .message((String) arguments.get("message"))
                    .build();
            return notificationService.sendNotification(request);
        };
        
        functionRegistry.registerFunction("send_notification", definition, handler);
    }
}