package com.xiaofuge.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String textContent;
    
    private String imageUrl;
    
    private String videoUrl;
    
    @Enumerated(EnumType.STRING)
    private ContentType type;
    
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;
    
    private String authorId;
    
    private String reviewResult;
    
    private String rejectReason;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private LocalDateTime publishTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = ReviewStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    public enum ContentType {
        TEXT, IMAGE, VIDEO, MIXED
    }
    
    public enum ReviewStatus {
        PENDING,           // 待审核
        TEXT_REVIEWING,    // 文本审核中
        IMAGE_REVIEWING,   // 图像审核中 
        MANUAL_REVIEWING,  // 人工审核中
        APPROVED,          // 审核通过
        REJECTED,          // 审核拒绝
        PUBLISHED          // 已发布
    }
}