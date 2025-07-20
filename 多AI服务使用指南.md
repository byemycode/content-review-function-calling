# 🤖 多AI服务使用指南

## 🎯 系统架构

现在系统支持**三种AI服务**，用户可以根据需求选择：

### 1. **Claude AI Service** ⭐推荐
- 🧠 **最智能**：强大的意图识别和理解能力
- 🛡️ **零误触发**：智能过滤无关输入
- 🎨 **最佳体验**：个性化响应和优雅交互
- 🔧 **需要API Key**：需要配置Claude API Key

### 2. **OpenAI Service**
- 🔄 **经典Function Calling**：完整的GPT Function Calling实现
- 💬 **多轮对话**：支持复杂的对话流程
- 🔧 **需要API Key**：需要配置OpenAI API Key

### 3. **Simple AI Service**
- ⚡ **免配置**：无需任何API Key即可运行
- 🎯 **演示用途**：基于规则的智能判断
- 🚀 **快速启动**：立即体验Function Calling

## 🚀 API接口说明

### **统一入口**（默认使用Claude）
```bash
POST /api/content-review/process
```

### **多AI选择接口**
```bash
# Claude AI（推荐）
POST /api/multi-ai/claude

# OpenAI GPT
POST /api/multi-ai/openai  

# Simple AI（免配置）
POST /api/multi-ai/simple

# 比较各AI服务
GET /api/multi-ai/compare
```

## 🧪 测试用例

### **1. Claude AI - 智能意图识别**

#### 正确识别发布意图
```bash
curl -X POST http://localhost:8080/api/multi-ai/claude \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "我想发布一篇Java并发编程的技术文章"
  }'
```

#### 智能处理无关输入
```bash
curl -X POST http://localhost:8080/api/multi-ai/claude \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "今天天气怎么样？"
  }'
```

**Claude响应示例：**
```json
{
  "success": true,
  "result": "😊 抱歉，我专门负责内容发布和审核相关的工作。\n\n如果您需要：\n• 发布文章、图片、视频\n• 内容安全检测\n• 审核流程管理\n\n我很乐意为您提供帮助！",
  "aiService": "Claude"
}
```

### **2. OpenAI Service - 经典Function Calling**
```bash
curl -X POST http://localhost:8080/api/multi-ai/openai \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "我想发布一篇关于Spring Boot的技术文章"
  }'
```

### **3. Simple AI - 免配置体验**
```bash
curl -X POST http://localhost:8080/api/multi-ai/simple \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "我想发布一篇技术文章"
  }'
```

### **4. 服务对比**
```bash
curl http://localhost:8080/api/multi-ai/compare
```

## ⚙️ 配置说明

### **完整配置**（支持所有AI服务）
```yaml
ai:
  claude:
    api-key: ${CLAUDE_API_KEY:sk-ant-test}
    model: ${CLAUDE_MODEL:claude-3-haiku-20240307}
    base-url: ${CLAUDE_BASE_URL:https://api.anthropic.com}
  openai:
    api-key: ${OPENAI_API_KEY:sk-test}
    model: gpt-3.5-turbo
    base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
```

### **环境变量**
```bash
# Claude配置（推荐）
export CLAUDE_API_KEY=sk-ant-your-claude-key

# OpenAI配置（可选）
export OPENAI_API_KEY=sk-your-openai-key

# 无需配置Simple AI
```

### **快速启动**（仅使用Simple AI）
如果不想配置API Key，可以直接启动，只使用Simple AI：
```bash
mvn spring-boot:run
# 然后使用 /api/multi-ai/simple 接口
```

## 🎯 使用建议

### **开发阶段**
1. **初期体验**：使用Simple AI快速了解Function Calling流程
2. **功能验证**：使用Claude AI体验智能意图识别
3. **完整测试**：配置所有AI服务进行对比测试

### **生产环境**
- **推荐Claude AI**：最佳的用户体验和智能判断
- **备选OpenAI**：经典的Function Calling实现
- **Simple AI仅供演示**：不建议生产环境使用

## 📊 AI服务对比

| 特性 | Claude AI | OpenAI | Simple AI |
|------|-----------|--------|-----------|
| 意图识别 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| 误触发防护 | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐ |
| 用户体验 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| Function Calling | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 配置复杂度 | 简单 | 简单 | 无需配置 |
| 成本 | 低 | 中等 | 免费 |

## 🔄 切换AI服务

### **方法1：使用不同的接口**
```bash
# 使用Claude
curl -X POST http://localhost:8080/api/multi-ai/claude -d '{"prompt": "..."}'

# 使用OpenAI  
curl -X POST http://localhost:8080/api/multi-ai/openai -d '{"prompt": "..."}'

# 使用Simple
curl -X POST http://localhost:8080/api/multi-ai/simple -d '{"prompt": "..."}'
```

### **方法2：修改Controller默认服务**
可以修改`ContentReviewController`中注入的服务来改变默认行为：
```java
// 使用Claude（当前默认）
private final ClaudeAIService aiService;

// 或使用OpenAI
private final AIService aiService;

// 或使用Simple
private final AIServiceSimple aiService;
```

## 🎉 总结

现在你拥有了一个功能完整的多AI Function Calling系统：

- ✅ **Claude AI**：最智能的意图识别和用户体验
- ✅ **OpenAI**：经典可靠的Function Calling实现  
- ✅ **Simple AI**：免配置快速体验
- ✅ **灵活切换**：根据需求选择合适的AI服务

无论你是想要最佳的用户体验，还是经典的Function Calling，或者是免配置的快速体验，这个系统都能满足你的需求！🚀