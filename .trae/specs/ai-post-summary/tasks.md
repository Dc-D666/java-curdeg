# AI 帖子总结功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 后端服务 - ContentSummaryService 开发
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 ContentSummaryService 服务类
  - 复用 ContentModerationService 的 AI API 调用逻辑
  - 实现 summarizePost 方法（帖子总结）
  - 实现 summarizePostWithComments 方法（帖子+评论总结）
  - 设计 Prompt 确保输出格式为结构化 JSON
  - 实现内容长度判断逻辑（100 字阈值）
  - 实现评论数据筛选（点赞 Top 5）
- **Acceptance Criteria Addressed**: AC-2, AC-6
- **Test Requirements**:
  - programmatic TR-1.1: 内容少于 100 字时返回提示信息
  - programmatic TR-1.2: 超过 100 字时调用 AI 并返回结构化结果
  - human-judgement TR-1.3: Prompt 设计合理，输出稳定
- **Notes**: 参考 ContentModerationService 的实现模式

## [x] Task 2: 后端控制器 - 新增总结 API 接口
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在 BbsPostController 中添加总结接口
  - 接口路径：POST /api/bbs/post/{postId}/summary
  - 返回统一的 DataResponse 包装
  - 处理异常情况（帖子不存在、AI 调用失败等）
- **Acceptance Criteria Addressed**: AC-2, AC-4, AC-5
- **Test Requirements**:
  - programmatic TR-2.1: 接口返回正确的 HTTP 状态码
  - programmatic TR-2.2: 异常情况返回友好的错误信息
  - programmatic TR-2.3: 返回格式符合预期

## [x] Task 3: 前端 UI - 添加 AI 总结按钮
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 post-detail.fxml，在操作按钮区域添加"AI 总结"按钮
  - 在 PostDetailController 中添加按钮事件处理
  - 按钮位置建议在"举报"按钮旁边
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - human-judgement TR-3.1: 按钮显示位置合理
  - human-judgement TR-3.2: 按钮样式与现有按钮一致

## [x] Task 4: 前端 UI - 添加总结结果展示区域
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 在 post-detail.fxml 中添加总结结果展示区域（TitledPane 或 VBox）
  - 区域包含两部分：帖子摘要和评论热点
  - 添加加载动画（ProgressIndicator）
- **Acceptance Criteria Addressed**: AC-3, AC-4
- **Test Requirements**:
  - human-judgement TR-4.1: 展示区域布局合理
  - human-judgement TR-4.2: 加载动画正确显示

## [x] Task 5: 前端 - 实现异步请求和结果展示
- **Priority**: P0
- **Depends On**: Task 2, Task 4
- **Description**: 
  - 在 HttpRequestUtil 中添加 getPostSummary 方法
  - 在 PostDetailController 中实现异步调用逻辑（使用 Task + Platform.runLater）
  - 实现加载状态管理
  - 实现结果展示逻辑
  - 实现错误处理
- **Acceptance Criteria Addressed**: AC-3, AC-4, AC-5
- **Test Requirements**:
  - programmatic TR-5.1: 异步请求正确发送
  - programmatic TR-5.2: 结果正确解析和展示
  - programmatic TR-5.3: 错误情况正确处理

## [x] Task 6: 测试与优化
- **Priority**: P1
- **Depends On**: Task 1, Task 2, Task 5
- **Description**: 
  - 完整的端到端测试
  - 边界情况测试（短内容、长内容、无评论、多评论）
  - 异常情况测试（网络错误、AI 服务不可用）
  - UI 体验优化（加载动画、按钮状态、提示信息）
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6
- **Test Requirements**:
  - human-judgement TR-6.1: 所有功能正常工作
  - human-judgement TR-6.2: 用户体验流畅
  - programmatic TR-6.3: 异常情况有合理的降级处理
