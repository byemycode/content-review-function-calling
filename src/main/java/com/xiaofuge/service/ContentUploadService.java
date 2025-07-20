package com.xiaofuge.service;

import com.xiaofuge.domain.Content;
import com.xiaofuge.repository.ContentRepository;
import com.xiaofuge.service.dto.ContentUploadRequest;
import com.xiaofuge.service.dto.ContentUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentUploadService {
    
    private final ContentRepository contentRepository;
    
    @Transactional
    public ContentUploadResponse uploadContent(ContentUploadRequest request) {
        log.info("开始上传内容: {}", request.getTitle());
        
        Content.ContentType contentType = determineContentType(request);
        
        Content content = Content.builder()
                .title(request.getTitle())
                .textContent(request.getTextContent())
                .imageUrl(request.getImageUrl())
                .videoUrl(request.getVideoUrl())
                .type(contentType)
                .authorId(request.getAuthorId())
                .status(Content.ReviewStatus.PENDING)
                .build();
        
        Content savedContent = contentRepository.save(content);
        log.info("内容上传成功，ID: {}", savedContent.getId());
        
        return ContentUploadResponse.builder()
                .contentId(savedContent.getId())
                .status(savedContent.getStatus().name())
                .message("内容上传成功，等待审核")
                .build();
    }
    
    private Content.ContentType determineContentType(ContentUploadRequest request) {
        boolean hasText = StrUtil.isNotBlank(request.getTextContent());
        boolean hasImage = StrUtil.isNotBlank(request.getImageUrl());
        boolean hasVideo = StrUtil.isNotBlank(request.getVideoUrl());
        
        if (hasText && (hasImage || hasVideo)) {
            return Content.ContentType.MIXED;
        } else if (hasVideo) {
            return Content.ContentType.VIDEO;
        } else if (hasImage) {
            return Content.ContentType.IMAGE;
        } else {
            return Content.ContentType.TEXT;
        }
    }
}