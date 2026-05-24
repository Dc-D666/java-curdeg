# AI搜索MVP功能 - 实现任务清单

## 📋 任务概览

总任务数：6个（后端4 + 前端2）
预估时间：3-5天

---

## 🔧 后端任务

### [x] Task 1：创建 AiSearchService 服务类

**优先级**：P0
**依赖**：无
**描述**：核心服务类，负责AI搜索的完整流程

**实现要点**：
- 新建 `AiSearchService.java`
- 方法：`aiSearch(String keyword)`
- 内部流程：
  1. 调用 `bbsPostService.searchPosts()` 获取Top5帖子
  2. 对每个帖子，调用 `bbsCommentRepository` 获取Top5点赞评论
  3. 构建prompt
  4. 调用AI API
  5. 封装结果返回

**技术细节**：
- 复用 `ContentModerationService` 的AI调用逻辑
- 新建独立的prompt模板

**验收标准**：
- ✅ 能正确获取帖子和评论
- ✅ prompt格式正确
- ✅ AI调用正常

**测试要求**：
- `programmatic`：单元测试验证数据获取
- `human-judgment`：人工检查prompt质量

**文件**：
- `java-server/src/main/java/cn/edu/sdu/java/server/services/AiSearchService.java`

---

### [x] Task 2：创建 PromptBuilder 工具类

**优先级**：P0
**依赖**：无
**描述**：专门负责构建搜索用的prompt

**实现要点**：
- 新建 `AiPromptBuilder.java`（或作为AiSearchService的内部方法）
- 方法：`buildSearchPrompt(List<PostWithComments> posts, String question)`
- 提示词模板参考PRD文档
- 对长内容进行适当截断，避免Token超限

**技术细节**：
- 每个帖子内容建议截断到300-500字
- 每条评论截断到100字
- 添加token估算（可选）

**验收标准**：
- ✅ prompt格式规范
- ✅ 内容截断合理
- ✅ 包含所有必要信息

**测试要求**：
- `programmatic`：单元测试验证prompt构建
- `human-judgment`：人工阅读prompt质量

**文件**：
- （可内联在AiSearchService中，或单独工具类）

---

### [x] Task 3：创建 AI搜索 Controller

**优先级**：P0
**依赖**：Task 1, Task 2
**描述**：REST API接口

**实现要点**：
- 在 `BbsPostController` 中新增端点
- 端点：`POST /api/bbs/post/ai-search`
- 请求体：`{ "keyword": "..." }`
- 响应体：
  ```json
  {
    "success": true,
    "data": {
      "answer": "AI总结的答案...",
      "relatedPosts": [...]
    }
  }
  ```
- 异常处理：AI失败时返回错误信息

**技术细节**：
- 复用 `DataResponse` 格式
- 添加调用日志

**验收标准**：
- ✅ 接口返回正确格式
- ✅ 异常处理友好

**测试要求**：
- `programmatic`：Postman/API测试

**文件**：
- `java-server/src/main/java/cn/edu/sdu/java/server/controllers/BbsPostController.java`（新增方法）

---

### [x] Task 4：Repository 扩展 - 获取Top评论

**优先级**：P0
**依赖**：无
**描述**：在评论Repository中新增获取Top评论的方法

**实现要点**：
- 在 `BbsCommentRepository` 新增方法：
  - `List<BbsComment> findTop5ByPostIdOrderByLikeCountDesc(Long postId)`

**验收标准**：
- ✅ 能正确获取指定帖子的Top5点赞评论

**测试要求**：
- `programmatic`：数据库查询测试

**文件**：
- `java-server/src/main/java/cn/edu/sdu/java/server/repositorys/BbsCommentRepository.java`

---

## 🎨 前端任务

### [x] Task 5：搜索页面 - 添加"AI回答"按钮

**优先级**：P0
**依赖**：Task 3（后端接口）
**描述**：在JavaFX搜索页面添加AI搜索功能

**实现要点**：
- 在 `PostSearchController` 中添加"AI回答"按钮
- 点击后调用新的API
- 显示加载状态（进度条或动画）
- 接收并展示结果

**技术细节**：
- 复用现有的 `HttpRequestUtil`
- 加载时禁用按钮，防止重复点击

**验收标准**：
- ✅ 按钮存在且可点击
- ✅ 加载状态显示正确
- ✅ API调用正常

**测试要求**：
- `human-judgment`：人工交互测试

**文件**：
- `java-fx/src/main/java/com/teach/javafx/controller/PostSearchController.java`
- `java-fx/src/main/resources/com/teach/javafx/post-search.fxml`（修改）

---

### [x] Task 6：搜索页面 - 结果展示UI

**优先级**：P0
**依赖**：Task 5
**描述**：美观展示AI答案和相关帖子

**实现要点**：
- 添加AI答案展示区域（带边框/背景色）
- 添加相关帖子列表
- 帖子可点击跳转到详情
- 错误状态显示友好提示

**布局建议**：
```
┌─────────────────────────────┐
│  [搜索框] [搜索] [AI回答]    │
├─────────────────────────────┤
│  ┌───────────────────────┐  │
│  │ 🤖 AI智能回答         │  │
│  │                       │  │
│  │ (AI答案内容区域)      │  │
│  └───────────────────────┘  │
│                             │
│  📋 相关帖子：              │
│  • 帖子1 (可点击)           │
│  • 帖子2 (可点击)           │
│  ...                        │
└─────────────────────────────┘
```

**验收标准**：
- ✅ AI答案展示清晰
- ✅ 相关帖子可点击跳转
- ✅ 错误提示友好

**测试要求**：
- `human-judgment`：UI和交互测试

**文件**：
- `java-fx/src/main/java/com/teach/javafx/controller/PostSearchController.java`（新增UI逻辑）
- `java-fx/src/main/resources/com/teach/javafx/post-search.fxml`（修改布局）

---

## 📊 任务依赖图

```
Task 4 ─┐
        ├─→ Task 1 ─→ Task 3 ─→ Task 5 ─→ Task 6
Task 2 ─┘
```

---

## ⚠️ 注意事项

### Task 1 注意事项
- 可以先创建一个简单的DTO类来包装帖子+评论
- 参考 `ContentModerationService` 的调用方式

### Task 2 注意事项
- 中文内容截断时，注意不要在词语中间截断
- 可以添加简单的长度检查

### Task 3 注意事项
- 建议添加超时处理（配合RestTemplate配置）

### Task 6 注意事项
- 参考现有UI风格，保持一致

---

## 🎯 分阶段实施建议

**Day 1-2**：完成后端 Task 1-4
**Day 3**：完成前端 Task 5-6
**Day 4**：联调、测试、修复

---

## 🔗 相关文件参考

- 现有AI调用：`ContentModerationService.java`
- 现有搜索：`BbsPostController.java` + `BbsPostService.java`
- 现有前端：`PostSearchController.java`
