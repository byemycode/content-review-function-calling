package com.xiaofuge.service;

import com.xiaofuge.domain.Content;
import com.xiaofuge.repository.ContentRepository;
import com.xiaofuge.service.dto.SensitiveWordCheckRequest;
import com.xiaofuge.service.dto.SensitiveWordCheckResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensitiveWordService {
    
    private final ContentRepository contentRepository;
    
    @Value("${content.review.sensitive-words:违法,暴力,色情}")
    private String sensitiveWordsStr;
    
    private List<String> getSensitiveWords() {
        return Arrays.asList(sensitiveWordsStr.split(","));
    }
    
    @Transactional
    public SensitiveWordCheckResponse checkSensitiveWords(SensitiveWordCheckRequest request) {
        log.info("开始敏感词检测，内容ID: {}", request.getContentId());
        
        Optional<Content> contentOpt = contentRepository.findById(request.getContentId());
        if (contentOpt.isEmpty()) {
            return SensitiveWordCheckResponse.builder()
                    .contentId(request.getContentId())
                    .passed(false)
                    .reason("内容不存在")
                    .build();
        }
        
        Content content = contentOpt.get();
        content.setStatus(Content.ReviewStatus.TEXT_REVIEWING);
        contentRepository.save(content);
        
        String textToCheck = buildTextToCheck(content);
        
        // 模拟敏感词检测逻辑
        for (String sensitiveWord : getSensitiveWords()) {
            if (textToCheck.contains(sensitiveWord)) {
                log.warn("发现敏感词: {} 在内容ID: {}", sensitiveWord, request.getContentId());
                
                content.setStatus(Content.ReviewStatus.REJECTED);
                content.setRejectReason("包含敏感词: " + sensitiveWord);
                contentRepository.save(content);
                
                return SensitiveWordCheckResponse.builder()
                        .contentId(request.getContentId())
                        .passed(false)
                        .reason("包含敏感词: " + sensitiveWord)
                        .build();
            }
        }
        
        log.info("敏感词检测通过，内容ID: {}", request.getContentId());
        return SensitiveWordCheckResponse.builder()
                .contentId(request.getContentId())
                .passed(true)
                .reason("敏感词检测通过")
                .build();
    }
    
    private String buildTextToCheck(Content content) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(content.getTitle())) {
            sb.append(content.getTitle()).append(" ");
        }
        if (StrUtil.isNotBlank(content.getTextContent())) {
            sb.append(content.getTextContent());
        }
        return sb.toString();
    }
}