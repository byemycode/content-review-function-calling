package com.xiaofuge.service;

import com.xiaofuge.domain.Content;
import com.xiaofuge.repository.ContentRepository;
import com.xiaofuge.service.dto.ContentPublishRequest;
import com.xiaofuge.service.dto.ContentPublishResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentPublishService {
    
    private final ContentRepository contentRepository;
    
    @Transactional
    public ContentPublishResponse publishContent(ContentPublishRequest request) {
        log.info("开始发布内容，内容ID: {}", request.getContentId());
        
        Optional<Content> contentOpt = contentRepository.findById(request.getContentId());
        if (contentOpt.isEmpty()) {
            return ContentPublishResponse.builder()
                    .contentId(request.getContentId())
                    .published(false)
                    .reason("内容不存在")
                    .build();
        }
        
        Content content = contentOpt.get();
        
        if (content.getStatus() != Content.ReviewStatus.APPROVED) {
            return ContentPublishResponse.builder()
                    .contentId(request.getContentId())
                    .published(false)
                    .reason("内容未通过审核，当前状态: " + content.getStatus())
                    .build();
        }
        
        // 执行发布操作
        content.setStatus(Content.ReviewStatus.PUBLISHED);
        content.setPublishTime(LocalDateTime.now());
        contentRepository.save(content);
        
        // 模拟发布到各个平台
        simulatePublishToPlatforms(content);
        
        log.info("内容发布成功，内容ID: {}", request.getContentId());
        
        return ContentPublishResponse.builder()
                .contentId(request.getContentId())
                .published(true)
                .reason("内容发布成功")
                .publishTime(content.getPublishTime())
                .publishUrl("https://platform.com/content/" + content.getId())
                .build();
    }
    
    private void simulatePublishToPlatforms(Content content) {
        // 模拟发布到不同平台
        log.info("正在发布内容到各平台: 微博、微信公众号、抖音...");
        
        // 这里可以集成真实的平台API
        try {
            Thread.sleep(1000); // 模拟网络请求延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("内容已成功发布到所有平台");
    }
}