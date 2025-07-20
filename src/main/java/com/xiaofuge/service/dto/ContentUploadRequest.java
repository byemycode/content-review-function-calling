package com.xiaofuge.service.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentUploadRequest {
    private String title;
    private String textContent;
    private String imageUrl;
    private String videoUrl;
    private String authorId;
}