package com.xiaofuge.exception;

import lombok.Getter;

@Getter
public class ContentReviewException extends RuntimeException {
    private final String code;
    
    public ContentReviewException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public ContentReviewException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    // 常用的异常类型
    public static ContentReviewException contentNotFound(Long contentId) {
        return new ContentReviewException("CONTENT_NOT_FOUND", "内容不存在: " + contentId);
    }
    
    public static ContentReviewException sensitiveWordDetected(String word) {
        return new ContentReviewException("SENSITIVE_WORD", "检测到敏感词: " + word);
    }
    
    public static ContentReviewException imageViolation(String type) {
        return new ContentReviewException("IMAGE_VIOLATION", "图像违规: " + type);
    }
    
    public static ContentReviewException reviewNotPassed(String reason) {
        return new ContentReviewException("REVIEW_FAILED", "审核未通过: " + reason);
    }
}