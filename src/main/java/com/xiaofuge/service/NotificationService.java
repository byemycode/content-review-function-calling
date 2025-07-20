package com.xiaofuge.service;

import com.xiaofuge.service.dto.NotificationRequest;
import com.xiaofuge.service.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    public NotificationResponse sendNotification(NotificationRequest request) {
        log.info("发送通知，内容ID: {}, 类型: {}", request.getContentId(), request.getType());
        
        // 模拟发送不同类型的通知
        switch (request.getType()) {
            case "EMAIL":
                sendEmail(request);
                break;
            case "SMS":
                sendSms(request);
                break;
            case "PUSH":
                sendPushNotification(request);
                break;
            case "WECHAT":
                sendWeChatMessage(request);
                break;
            default:
                log.warn("未知的通知类型: {}", request.getType());
                return NotificationResponse.builder()
                        .contentId(request.getContentId())
                        .sent(false)
                        .reason("未知的通知类型")
                        .build();
        }
        
        log.info("通知发送成功，内容ID: {}", request.getContentId());
        
        return NotificationResponse.builder()
                .contentId(request.getContentId())
                .sent(true)
                .reason("通知发送成功")
                .notificationType(request.getType())
                .build();
    }
    
    private void sendEmail(NotificationRequest request) {
        log.info("发送邮件通知: {}", request.getMessage());
        // 集成邮件服务
    }
    
    private void sendSms(NotificationRequest request) {
        log.info("发送短信通知: {}", request.getMessage());
        // 集成短信服务
    }
    
    private void sendPushNotification(NotificationRequest request) {
        log.info("发送推送通知: {}", request.getMessage());
        // 集成推送服务
    }
    
    private void sendWeChatMessage(NotificationRequest request) {
        log.info("发送微信通知: {}", request.getMessage());
        // 集成微信服务
    }
}