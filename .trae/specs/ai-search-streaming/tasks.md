# AI搜索流式输出 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 后端引入必要依赖
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 检查 Spring Boot 3.x 是否已经支持 SSE（应该已经内置在 spring-boot-starter-web 中）
  - 确认不需要引入额外的 Spring WebFlux 依赖
  - 检查并配置必要的线程池（如需要）
- **Acceptance Criteria Addressed**: [AC-6]
- **Test Requirements**:
  - `programmatic` TR-1.1: 项目能够正常编译和启动
  - `human-judgement` TR-1.2: 检查依赖配置是否合理
- **Notes**: 尽量使用现有技术栈，最小化新依赖
- **Status**: 已完成！Spring Boot 3.x 已内置 SSE 支持，无需额外依赖

## [x] Task 2: 后端实现AI API流式调用
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在 AiSearchService 中添加新的流式方法（保留原有非流式方法作为备份）
  - 添加 `stream: true` 参数到 AI API 请求
  - 使用 HttpClient 或改造 RestTemplate 来支持流式响应
  - 实现流式响应解析（处理 `data: { ...` 格式，过滤 `[DONE]` 标记）
  - 抽取文本内容（`choices[0].delta.content`）
  - 将流式文本通过回调或消费者传递给 SSE 接口
- **Acceptance Criteria Addressed**: [AC-1, AC-2]
- **Test Requirements**:
  - `programmatic` TR-2.1: 能够正确解析 OpenAI 兼容的流式响应
  - `programmatic` TR-2.2: 能够正确处理文本 delta 并提取内容
  - `programmatic` TR-2.3: 正确处理 `[DONE]` 结束标记
- **Notes**: 优先考虑使用 Java 11+ 原生 HttpClient 来实现流式响应
- **Status**: 已完成！使用 Java 原生 HttpClient 实现了流式调用，并通过回调传递数据

## [x] Task 3: 后端实现SSE接口
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在 BbsPostController 中添加 SSE 端点（如 GET /api/bbs/post/ai-search-stream）
  - 使用 SseEmitter 包装流式输出
  - 事件发送顺序：
    1. 先搜索相关帖子和评论
    2. 发送 `posts` 事件
    3. 然后流式调用 AI API，发送 `content` 事件
    4. 最后发送 `done` 事件
  - 异常时发送 `error` 事件
  - 确保超时时间设置合理
- **Acceptance Criteria Addressed**: [AC-1, AC-3]
- **Test Requirements**:
  - `programmatic` TR-3.1: SSE 端点可访问
  - `programmatic` TR-3.2: 能够按照定义的格式发送 SSE 事件
  - `programmatic` TR-3.3: posts 事件优先于 content 事件发送
  - `programmatic` TR-3.4: 错误处理正确，发送 error 事件
- **Notes**: 参照 spec.md 中定义的 SSE 事件格式
- **Status**: 已完成！实现了 GET /api/bbs/post/ai-search-stream 端点，并按正确顺序发送事件

## [x] Task 4: 前端实现 SSE 客户端
- **Priority**: P0
- **Depends On**: Task 3（可并行开发，但测试需要等后端接口）
- **Description**: 
  - 在 HttpRequestUtil 中添加 SSE 连接方法
  - 使用 Java 11+ 原生 HttpClient 实现流式响应读取
  - 实现 SSE 事件解析（解析 `data:` 前缀，按 `\n\n` 分割事件）
  - 按照 spec.md 定义解析各类型事件
  - 通过回调或消费者将事件传递给 UI
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `programmatic` TR-4.1: 能够建立 SSE 连接
  - `programmatic` TR-4.2: 能够正确解析 SSE 事件格式
  - `programmatic` TR-4.3: 能够区分不同类型的事件（posts、content、done、error）
- **Notes**: SSE 解析可手动实现，不需要额外库
- **Status**: 已完成！添加了 aiSearchStream 方法，支持 posts、content、done、error 事件类型

## [x] Task 5: 前端实现流式 UI 更新和 Markdown 渲染
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 在 PostSearchController 中实现流式处理逻辑
  - 维护完整文本缓冲区用于复制功能
  - 使用 Platform.runLater() 安全更新 UI
  - 实现缓冲策略：
    - 时间缓冲：每 200-300ms 更新一次
    - 或字符缓冲：每 20-50 个字符更新一次
  - 渐进式渲染 Markdown 格式：
    - 处理加粗、斜体、删除线、红色标记
    - 处理不完整的标签时保持状态
    - 新文本到达时重新渲染完整内容
  - 收到 posts 事件时立即显示相关帖子
  - 收到 done 事件时更新状态为完成
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-4, AC-7]
- **Test Requirements**:
  - `programmatic` TR-5.1: UI 能够随着流式数据更新
  - `human-judgement` TR-5.2: 更新流畅，没有过度闪烁
  - `human-judgement` TR-5.3: Markdown 格式正确渲染
  - `human-judgement` TR-5.4: 复制功能在流式过程中也能正常工作
- **Notes**: 参考现有代码中的 parseAndDisplayMarkdown 方法
- **Status**: 已完成！重写了 aiSearch 方法，加入了缓冲机制和状态切换（搜索中 → 思考中 → 生成中）

## [x] Task 6: 前端更新加载状态
- **Priority**: P1
- **Depends On**: Task 5
- **Description**: 
  - 优化加载动画和状态提示
  - 区分不同阶段：
    - "搜索中"（搜索相关帖子和评论）
    - "AI思考中"（等待 AI 首字）
    - "生成中"（AI 正在输出文本）
    - "完成"
  - 实现友好的加载指示器
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `human-judgement` TR-6.1: 加载状态清晰易懂
- **Notes**: 保持现有加载按钮状态管理，可以参考现有代码
- **Status**: 已完成！按钮文本会在不同阶段自动切换：搜索中 → 思考中 → 生成中 → 搜索

## [x] Task 7: 错误处理与降级
- **Priority**: P1
- **Depends On**: Task 3, Task 5
- **Description**: 
  - 实现 SSE 连接失败的处理
  - 实现流式过程中断的处理
  - 保留降级方案：流式失败时提示用户，并可以回退到非流式接口
  - 错误时清理状态，允许用户重新搜索
- **Acceptance Criteria Addressed**: [AC-5, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-7.1: 错误提示友好清晰
  - `programmatic` TR-7.2: 降级机制能正常工作
  - `programmatic` TR-7.3: 错误后可以重新搜索
- **Notes**: 确保用户知道发生了什么并能继续操作
- **Status**: 已完成！实现了 onError 回调，错误时显示提示并保留已接收到的答案

## [ ] Task 8: 整合与测试
- **Priority**: P0
- **Depends On**: Task 3, Task 5, Task 6, Task 7
- **Description**: 
  - 端到端整合测试
  - 边界情况测试：
    - 空搜索词
    - 长文本生成
    - 网络中断
  - 性能测试：
    - 首字响应时间
    - 内存占用
    - 多次连续搜索
  - 向后兼容性测试
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `human-judgement` TR-8.1: 完整流程测试通过
  - `programmatic` TR-8.2: 向后兼容性验证（原有非流式接口正常工作）
  - `programmatic` TR-8.3: 多次连续搜索测试通过，没有状态残留
  - `human-judgement` TR-8.4: 首字响应时间 < 3秒
- **Notes**: 确保复制功能也能正常工作，测试应贯穿整个开发过程