package com.xiaofuge.service;

import com.xiaofuge.domain.Content;
import com.xiaofuge.repository.ContentRepository;
import com.xiaofuge.service.dto.ImageRecognitionRequest;
import com.xiaofuge.service.dto.ImageRecognitionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageRecognitionService {
    
    private final ContentRepository contentRepository;
    
    // 模拟违规图片类型
    private static final List<String> FORBIDDEN_TYPES = Arrays.asList(
            "violence", "porn", "politics", "terrorism"
    );
    
    @Transactional
    public ImageRecognitionResponse recognizeImage(ImageRecognitionRequest request) {
        log.info("开始图像识别，内容ID: {}", request.getContentId());
        
        Optional<Content> contentOpt = contentRepository.findById(request.getContentId());
        if (contentOpt.isEmpty()) {
            return ImageRecognitionResponse.builder()
                    .contentId(request.getContentId())
                    .passed(false)
                    .reason("内容不存在")
                    .build();
        }
        
        Content content = contentOpt.get();
        content.setStatus(Content.ReviewStatus.IMAGE_REVIEWING);
        contentRepository.save(content);
        
        // 检查是否有图片需要识别
        if (StrUtil.isBlank(content.getImageUrl()) && StrUtil.isBlank(content.getVideoUrl())) {
            log.info("无图片/视频内容，跳过图像识别，内容ID: {}", request.getContentId());
            return ImageRecognitionResponse.builder()
                    .contentId(request.getContentId())
                    .passed(true)
                    .reason("无图片内容")
                    .build();
        }
        
        // 模拟AI图像识别API调用
        String recognitionResult = simulateImageRecognition(content);
        
        if (FORBIDDEN_TYPES.contains(recognitionResult)) {
            log.warn("图像识别发现违规内容: {} 在内容ID: {}", recognitionResult, request.getContentId());
            
            content.setStatus(Content.ReviewStatus.REJECTED);
            content.setRejectReason("图像包含违规内容: " + recognitionResult);
            contentRepository.save(content);
            
            return ImageRecognitionResponse.builder()
                    .contentId(request.getContentId())
                    .passed(false)
                    .reason("图像包含违规内容: " + recognitionResult)
                    .recognitionResult(recognitionResult)
                    .build();
        }
        
        log.info("图像识别通过，内容ID: {}, 识别结果: {}", request.getContentId(), recognitionResult);
        return ImageRecognitionResponse.builder()
                .contentId(request.getContentId())
                .passed(true)
                .reason("图像识别通过")
                .recognitionResult(recognitionResult)
                .build();
    }
    
    private String simulateImageRecognition(Content content) {
        // 模拟调用第三方AI图像识别API
        // 这里用随机结果模拟，实际项目中会调用真实的AI服务
        List<String> normalTypes = Arrays.asList(
                "landscape", "portrait", "food", "animal", "object", "text"
        );
        
        // 90%概率返回正常内容，10%概率返回违规内容
        if (RandomUtil.randomInt(100) < 10) {
            return FORBIDDEN_TYPES.get(RandomUtil.randomInt(FORBIDDEN_TYPES.size()));
        } else {
            return normalTypes.get(RandomUtil.randomInt(normalTypes.size()));
        }
    }
}