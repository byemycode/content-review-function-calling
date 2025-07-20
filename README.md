# 智能内容审核发布系统 - Function Calling实战

## 项目简介

这是一个基于Spring Boot和Function Calling技术的智能内容审核发布系统。系统可以让AI智能地调用各种后端服务接口，自动化完成内容审核发布的完整流程。

## 核心特性

- **智能流程编排**: AI根据内容类型自动选择合适的审核流程
- **Function Calling**: 实现AI与后端服务的无缝集成
- **多重审核机制**: 敏感词检测、图像识别、人工审核
- **实时状态管理**: 完整的内容状态跟踪
- **异常处理机制**: 完善的错误处理和重试机制

## 系统架构

```
用户请求 → AI服务 → Function Calling引擎 → 业务服务
                    ↓
              Function Registry
                    ↓
        [内容上传、敏感词检测、图像识别、人工审核、内容发布、消息通知]
```

## 审核流程

### 文本内容流程
1. **内容上传** → 2. **敏感词检测** → 3. **内容发布** → 4. **推送通知**

### 图片内容流程  
1. **内容上传** → 2. **敏感词检测** → 3. **图像识别** → 4. **内容发布** → 5. **推送通知**

### 复杂内容流程
1. **内容上传** → 2. **敏感词检测** → 3. **图像识别** → 4. **人工审核** → 5. **内容发布** → 6. **推送通知**

## 技术栈

- **框架**: Spring Boot 3.2.0
- **AI集成**: OpenAI GPT-3.5-turbo
- **数据库**: H2 (内存数据库)
- **构建工具**: Maven
- **Java版本**: JDK 17

## 快速开始

### 1. 环境准备
```bash
# 确保安装了JDK 17+和Maven
java -version
mvn -version
```

### 2. 配置API Key
在 `application.yml` 中配置你的OpenAI API Key:
```yaml
ai:
  openai:
    api-key: ${OPENAI_API_KEY:your-openai-api-key}
```

或者设置环境变量:
```bash
export OPENAI_API_KEY=your-openai-api-key
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

### 4. 测试API

**健康检查:**
```bash
curl http://localhost:8080/api/content-review/health
```

**智能内容审核:**
```bash
curl -X POST http://localhost:8080/api/content-review/process \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "我想发布一篇技术文章，标题是Java并发编程最佳实践，内容包含线程池、锁机制等技术要点。"
  }'
```

## Function Calling函数列表

| 函数名 | 描述 | 参数 |
|--------|------|------|
| `upload_content` | 上传内容到系统 | title, textContent, imageUrl, videoUrl, authorId |
| `check_sensitive_words` | 敏感词检测 | contentId |
| `recognize_image` | 图像识别 | contentId |
| `submit_manual_review` | 提交人工审核 | contentId, reviewReason |
| `check_manual_review` | 检查人工审核结果 | contentId |
| `publish_content` | 发布内容 | contentId |
| `send_notification` | 发送通知 | contentId, type, recipient, message |

## 使用示例

### 示例1: 发布技术文章
```json
{
  "prompt": "帮我发布一篇关于Spring Boot的技术文章，标题是'Spring Boot微服务最佳实践'，作者ID是'author_001'"
}
```

AI会自动执行以下流程：
1. 上传内容
2. 敏感词检测
3. 发布内容  
4. 发送通知

### 示例2: 发布图片内容
```json
{
  "prompt": "我要发布一张产品宣传图片，图片地址是https://example.com/product.jpg，标题是'新产品发布'，作者ID是'author_002'"
}
```

AI会自动执行：
1. 上传内容
2. 敏感词检测
3. 图像识别
4. 发布内容
5. 发送通知

### 示例3: 复杂内容审核
```json
{
  "prompt": "这是一个包含争议性话题的内容，需要人工审核，标题是'社会热点讨论'，作者ID是'author_003'"
}
```

AI会智能判断需要人工审核：
1. 上传内容
2. 敏感词检测
3. 提交人工审核
4. 检查审核结果
5. 发布或拒绝

## 数据库模式

### Content表结构
```sql
CREATE TABLE content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    text_content TEXT,
    image_url VARCHAR(500),
    video_url VARCHAR(500), 
    type VARCHAR(20),
    status VARCHAR(20),
    author_id VARCHAR(100),
    review_result VARCHAR(500),
    reject_reason VARCHAR(500),
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    publish_time TIMESTAMP
);
```

## 自定义配置

### 敏感词配置
在 `application.yml` 中配置敏感词列表:
```yaml
content:
  review:
    sensitive-words:
      - "违法"
      - "暴力" 
      - "色情"
```

### 重试配置
```yaml
content:
  review:
    max-retries: 3
    timeout: 30000
```

## 扩展开发

### 添加新的审核服务
1. 实现服务接口
2. 在 `ContentReviewFunctions` 中注册新函数
3. AI会自动识别并使用新功能

### 集成第三方AI服务
```java
// 替换AIService中的OpenAI调用
// 支持百度文心、阿里通义等国产AI服务
```

## 监控和日志

系统提供详细的日志记录：
- 函数调用日志
- 审核流程日志  
- 异常处理日志
- 性能监控日志

查看日志：
```bash
tail -f logs/application.log
```

## 测试

运行所有测试：
```bash
mvn test
```

运行集成测试：
```bash
mvn test -Dtest=ContentReviewIntegrationTest
```

## 部署

### Docker部署
```bash
# 构建镜像
docker build -t content-review-system .

# 运行容器
docker run -p 8080:8080 -e OPENAI_API_KEY=your-key content-review-system
```

### 生产环境配置
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://localhost:3306/content_review
```

## 常见问题

**Q: AI不调用函数怎么办？**
A: 检查API Key配置和网络连接，确保prompt描述清晰。

**Q: 如何添加新的审核规则？**  
A: 在对应的Service中修改审核逻辑，或注册新的Function。

**Q: 支持哪些通知方式？**
A: 目前支持EMAIL、SMS、PUSH、WECHAT四种方式。

## 贡献

欢迎提交Issue和Pull Request！

## 许可证

MIT License