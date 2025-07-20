package com.xiaofuge.ai.intent;

public enum UserIntent {
    /**
     * 内容发布意图
     */
    CONTENT_PUBLISH("content_publish", "用户想要发布内容"),
    
    /**
     * 内容审核意图
     */
    CONTENT_REVIEW("content_review", "用户想要审核内容"),
    
    /**
     * 问候语
     */
    GREETING("greeting", "用户问候"),
    
    /**
     * 一般问题
     */
    QUESTION("question", "用户询问问题"),
    
    /**
     * 否定意图
     */
    NEGATIVE("negative", "用户表达否定"),
    
    /**
     * 内容相关但意图不明确
     */
    CONTENT_RELATED("content_related", "与内容相关但意图不明确"),
    
    /**
     * 完全不相关
     */
    UNRELATED("unrelated", "与业务无关"),
    
    /**
     * 未知意图
     */
    UNKNOWN("unknown", "无法识别意图");
    
    private final String code;
    private final String description;
    
    UserIntent(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}