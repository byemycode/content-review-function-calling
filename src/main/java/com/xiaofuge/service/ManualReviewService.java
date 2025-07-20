package com.xiaofuge.service;

import com.xiaofuge.domain.Content;
import com.xiaofuge.repository.ContentRepository;
import com.xiaofuge.service.dto.ManualReviewRequest;
import com.xiaofuge.service.dto.ManualReviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.RandomUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManualReviewService {
    
    private final ContentRepository contentRepository;
    
    @Transactional
    public ManualReviewResponse submitForManualReview(ManualReviewRequest request) {
        log.info("提交人工审核，内容ID: {}", request.getContentId());
        
        Optional<Content> contentOpt = contentRepository.findById(request.getContentId());
        if (contentOpt.isEmpty()) {
            return ManualReviewResponse.builder()
                    .contentId(request.getContentId())
                    .submitted(false)
                    .reason("内容不存在")
                    .build();
        }
        
        Content content = contentOpt.get();
        content.setStatus(Content.ReviewStatus.MANUAL_REVIEWING);
        contentRepository.save(content);
        
        log.info("内容已提交人工审核队列，内容ID: {}", request.getContentId());
        
        return ManualReviewResponse.builder()
                .contentId(request.getContentId())
                .submitted(true)
                .reason("已提交人工审核")
                .estimatedTime("24小时内")
                .build();
    }
    
    @Transactional
    public ManualReviewResponse checkManualReviewResult(Long contentId) {
        log.info("检查人工审核结果，内容ID: {}", contentId);
        
        Optional<Content> contentOpt = contentRepository.findById(contentId);
        if (contentOpt.isEmpty()) {
            return ManualReviewResponse.builder()
                    .contentId(contentId)
                    .submitted(false)
                    .reason("内容不存在")
                    .build();
        }
        
        Content content = contentOpt.get();
        
        if (content.getStatus() != Content.ReviewStatus.MANUAL_REVIEWING) {
            return ManualReviewResponse.builder()
                    .contentId(contentId)
                    .submitted(false)
                    .reason("内容未在人工审核中")
                    .build();
        }
        
        // 模拟人工审核结果（80%通过率）
        boolean passed = RandomUtil.randomInt(100) < 80;
        
        if (passed) {
            content.setStatus(Content.ReviewStatus.APPROVED);
            content.setReviewResult("人工审核通过");
            log.info("人工审核通过，内容ID: {}", contentId);
        } else {
            content.setStatus(Content.ReviewStatus.REJECTED);
            content.setRejectReason("人工审核未通过：内容质量不达标");
            log.info("人工审核未通过，内容ID: {}", contentId);
        }
        
        contentRepository.save(content);
        
        return ManualReviewResponse.builder()
                .contentId(contentId)
                .submitted(true)
                .passed(passed)
                .reason(passed ? "人工审核通过" : "人工审核未通过：内容质量不达标")
                .build();
    }
}