package com.xiaofuge.service.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPublishResponse {
    private Long contentId;
    private boolean published;
    private String reason;
    private LocalDateTime publishTime;
    private String publishUrl;
}