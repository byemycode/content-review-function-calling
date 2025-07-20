package com.xiaofuge.service.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualReviewResponse {
    private Long contentId;
    private boolean submitted;
    private boolean passed;
    private String reason;
    private String estimatedTime;
}