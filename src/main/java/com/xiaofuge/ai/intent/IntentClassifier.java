package com.xiaofuge.ai.intent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class IntentClassifier {
    
    // 内容发布相关关键词
    private static final List<String> PUBLISH_KEYWORDS = Arrays.asList(
        "发布", "上传", "提交", "投稿", "分享", "推送", "post", "upload", "submit", "publish"
    );
    
    // 内容类型关键词
    private static final List<String> CONTENT_KEYWORDS = Arrays.asList(
        "文章", "内容", "图片", "视频", "照片", "博客", "帖子", "资讯", "新闻", "教程",
        "article", "content", "image", "video", "photo", "blog", "post", "tutorial"
    );
    
    // 审核相关关键词
    private static final List<String> REVIEW_KEYWORDS = Arrays.asList(
        "审核", "检查", "检测", "验证", "review", "check", "verify", "moderate"
    );
    
    // 问候语关键词
    private static final List<String> GREETING_KEYWORDS = Arrays.asList(
        "你好", "hello", "hi", "您好", "早上好", "下午好", "晚上好", "嗨"
    );
    
    // 否定词
    private static final List<String> NEGATIVE_KEYWORDS = Arrays.asList(
        "不想", "不要", "不需要", "别", "不", "没有", "not", "don't", "no"
    );
    
    // 问题询问关键词
    private static final List<String> QUESTION_KEYWORDS = Arrays.asList(
        "什么", "怎么", "如何", "为什么", "哪里", "谁", "什么时候", "多少",
        "what", "how", "why", "where", "who", "when", "which"
    );
    
    public UserIntent classifyIntent(String userInput) {
        if (StrUtil.isBlank(userInput)) {
            return UserIntent.UNKNOWN;
        }
        
        String normalizedInput = userInput.toLowerCase().trim();
        log.debug("分析用户意图: {}", normalizedInput);
        
        // 1. 检查是否包含否定词 - 优先级最高
        if (containsAny(normalizedInput, NEGATIVE_KEYWORDS)) {
            log.debug("检测到否定意图");
            return UserIntent.NEGATIVE;
        }
        
        // 2. 检查问候语
        if (containsAny(normalizedInput, GREETING_KEYWORDS)) {
            log.debug("检测到问候意图");
            return UserIntent.GREETING;
        }
        
        // 3. 检查是否为问题
        if (containsAny(normalizedInput, QUESTION_KEYWORDS) && 
            (normalizedInput.contains("?") || normalizedInput.contains("？"))) {
            log.debug("检测到问题询问意图");
            return UserIntent.QUESTION;
        }
        
        // 4. 检查内容发布意图
        boolean hasPublishAction = containsAny(normalizedInput, PUBLISH_KEYWORDS);
        boolean hasContentType = containsAny(normalizedInput, CONTENT_KEYWORDS);
        boolean hasIntent = containsIntent(normalizedInput);
        
        if (hasPublishAction && (hasContentType || hasIntent)) {
            log.debug("检测到内容发布意图: action={}, content={}, intent={}", 
                     hasPublishAction, hasContentType, hasIntent);
            return UserIntent.CONTENT_PUBLISH;
        }
        
        // 5. 检查内容审核意图
        if (containsAny(normalizedInput, REVIEW_KEYWORDS)) {
            log.debug("检测到内容审核意图");
            return UserIntent.CONTENT_REVIEW;
        }
        
        // 6. 特殊模式匹配
        if (matchesPublishPattern(normalizedInput)) {
            log.debug("通过模式匹配检测到发布意图");
            return UserIntent.CONTENT_PUBLISH;
        }
        
        // 7. 业务相关但不明确
        if (containsAny(normalizedInput, CONTENT_KEYWORDS)) {
            log.debug("检测到内容相关但意图不明确");
            return UserIntent.CONTENT_RELATED;
        }
        
        log.debug("未检测到明确意图，归类为不相关");
        return UserIntent.UNRELATED;
    }
    
    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }
    
    private boolean containsIntent(String text) {
        List<String> intentWords = Arrays.asList(
            "想", "要", "需要", "希望", "打算", "准备", "计划",
            "want", "need", "would like", "plan to", "going to"
        );
        return containsAny(text, intentWords);
    }
    
    private boolean matchesPublishPattern(String text) {
        // 匹配"我要XXX文章"这样的模式
        List<Pattern> patterns = Arrays.asList(
            Pattern.compile("我.*?(写|创建|做|搞).*?(文章|内容|博客)"),
            Pattern.compile("帮我.*?(发|传|放).*?(内容|文章|图片)"),
            Pattern.compile("(创建|新建|添加).*?(内容|文章|帖子)"),
            Pattern.compile("我有.*?(文章|内容|图片).*?(想|要).*?(发|传|分享)")
        );
        
        return patterns.stream().anyMatch(pattern -> pattern.matcher(text).find());
    }
    
    public double getConfidenceScore(String userInput, UserIntent intent) {
        if (intent == UserIntent.UNKNOWN || intent == UserIntent.UNRELATED) {
            return 0.0;
        }
        
        String normalizedInput = userInput.toLowerCase().trim();
        double score = 0.0;
        int factors = 0;
        
        switch (intent) {
            case CONTENT_PUBLISH:
                if (containsAny(normalizedInput, PUBLISH_KEYWORDS)) {
                    score += 0.4;
                    factors++;
                }
                if (containsAny(normalizedInput, CONTENT_KEYWORDS)) {
                    score += 0.3;
                    factors++;
                }
                if (containsIntent(normalizedInput)) {
                    score += 0.3;
                    factors++;
                }
                break;
                
            case CONTENT_REVIEW:
                if (containsAny(normalizedInput, REVIEW_KEYWORDS)) {
                    score += 0.6;
                    factors++;
                }
                if (containsAny(normalizedInput, CONTENT_KEYWORDS)) {
                    score += 0.4;
                    factors++;
                }
                break;
                
            case GREETING:
                if (containsAny(normalizedInput, GREETING_KEYWORDS)) {
                    score += 0.8;
                    factors++;
                }
                break;
        }
        
        return factors > 0 ? score : 0.1;
    }
}