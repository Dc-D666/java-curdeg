# AI 写作助手功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 创建后端数据传输对象（DTO）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 AiWriteRequest.java 用于接收前端请求（title、content、instruction、operation）
  - 创建 AiWriteResponse.java 用于返回 AI 生成结果（title、content、instructionSuggestion、success）
- **Acceptance Criteria Addressed**: AC-10
- **Test Requirements**:
  - `programmatic` TR-1.1: AiWriteRequest 包含所有必需字段 ✅
  - `programmatic` TR-1.2: AiWriteResponse 包含所有必需字段 ✅
  - `programmatic` TR-1.3: 两个 DTO 使用 Lombok 注解 ✅
- **Notes**: 参考项目中现有的 Request/Response 类

## [x] Task 2: 创建后端 AiWriteService 服务类
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 实现 AiWriteService.java ✅
  - 复用 ContentSummaryService 的大模型调用逻辑 ✅
  - 实现三个预设提示词模板（AI帮写、AI续写、AI润色）✅
  - 实现严格的 System Prompt，要求大模型返回 JSON ✅
  - 实现大模型返回结果的解析和容错处理 ✅
- **Acceptance Criteria Addressed**: AC-10
- **Test Requirements**:
  - `programmatic` TR-2.1: 三个预设提示词正确实现 ✅
  - `programmatic` TR-2.2: System Prompt 要求严格要求返回 JSON ✅
  - `programmatic` TR-2.3: 能正确解析大模型返回的 JSON ✅
  - `programmatic` TR-2.4: 包含容错处理逻辑 ✅
- **Notes**: 参考 ContentSummaryService 的实现方式

## [x] Task 3: 在 BbsPostController 中新增 AI 写作 API
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在 BbsPostController 中新增 POST /api/bbs/post/ai-write 接口 ✅
  - 调用 AiWriteService 处理业务逻辑 ✅
  - 统一使用 DataResponse 包装返回结果 ✅
- **Acceptance Criteria Addressed**: AC-6, AC-10
- **Test Requirements**:
  - `programmatic` TR-3.1: API 路径正确 ✅
  - `programmatic` TR-3.2: 正确接收和返回数据 ✅
  - `programmatic` TR-3.3: 使用 DataResponse 包装 ✅
- **Notes**: 参考其他 API 接口的实现方式

## [x] Task 4: 创建前端 AiWriteResponse 模型类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在前端创建 AiWriteResponse.java 模型类 ✅
  - 使用 Gson 注解 ✅
  - 包含 title、content、instructionSuggestion、success 字段 ✅
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `programmatic` TR-4.1: 模型类包含所有必需字段 ✅
  - `programmatic` TR-4.2: 正确使用 Gson 注解 ✅
- **Notes**: 参考前端其他模型类的实现方式

## [x] Task 5: 在 HttpRequestUtil 中新增 AI 写作 API 调用方法
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 在 HttpRequestUtil.java 中新增 aiWrite 方法 ✅
  - 发送 title、content、instruction、operation 到后端 ✅
  - 解析返回的 AiWriteResponse ✅
- **Acceptance Criteria Addressed**: AC-5, AC-6
- **Test Requirements**:
  - `programmatic` TR-5.1: HTTP 请求正确发送 ✅
  - `programmatic` TR-5.2: 响应正确解析 ✅
- **Notes**: 参考 HttpRequestUtil 中的其他方法

## [x] Task 6: 修改 FXML 布局文件，新增 AI 指令输入区域
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 post-publish.fxml ✅
  - 在内容 TextArea 下方新增 AI 指令输入区域 ✅
  - 添加三个预设按钮（AI帮写、AI续写、AI润色）✅
  - 添加 AI 指令输入框和 AI 生成按钮 ✅
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-6.1: UI 布局美观合理 ✅
  - `human-judgement` TR-6.2: 所有控件正确添加 ✅
  - `human-judgement` TR-6.3: 按钮样式与现有一致 ✅
- **Notes**: 参考现有布局的风格

## [x] Task 7: 修改 PostPublishController，添加 UI 绑定和初始化
- **Priority**: P0
- **Depends On**: Task 5, Task 6
- **Description**: 
  - 在 PostPublishController 中新增 FXML 控件 ✅
  - 在 initialize 方法中初始化并绑定事件 ✅
  - 实现 AI 指令输入框的内容变化监听 ✅
  - 根据输入框内容控制 AI 生成按钮的启用/禁用状态 ✅
- **Acceptance Criteria Addressed**: AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-7.1: 控件正确绑定 ✅
  - `programmatic` TR-7.2: 内容变化监听正确实现 ✅
  - `programmatic` TR-7.3: 按钮状态正确控制 ✅
- **Notes**: 参考现有控制器的实现方式

## [x] Task 8: 实现预设按钮点击事件处理
- **Priority**: P1
- **Depends On**: Task 7
- **Description**: 
  - 实现"AI帮写"按钮点击事件 ✅
  - 实现"AI续写"按钮点击事件 ✅
  - 实现"AI润色"按钮点击事件 ✅
  - 点击按钮时填充对应的预定义提示词 ✅
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-8.1: AI帮写提示词正确填充 ✅
  - `programmatic` TR-8.2: AI续写提示词正确填充 ✅
  - `programmatic` TR-8.3: AI润色提示词正确填充 ✅
- **Notes**: 预设提示词需要精心设计

## [x] Task 9: 实现 AI 生成按钮点击事件处理
- **Priority**: P1
- **Depends On**: Task 8
- **Description**: 
  - 实现 AI 生成按钮点击事件 ✅
  - 显示加载状态 ✅
  - 使用 Task 异步调用后端 API ✅
  - 暂存用户原始输入的标题和内容 ✅
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-9.1: 加载状态正确显示 ✅
  - `programmatic` TR-9.2: 异步调用正确实现 ✅
  - `programmatic` TR-9.3: 原始内容正确暂存 ✅
- **Notes**: 参考现有 Task 的使用方式

## [x] Task 10: 实现 AI 生成结果处理和"采纳/弃用"对话框
- **Priority**: P1
- **Depends On**: Task 9
- **Description**: 
  - 处理 AI 生成成功的情况 ✅
  - 将 AI 生成结果填充到输入框 ✅
  - 实现"采纳/弃用"确认对话框 ✅
  - 实现采纳功能（保留 AI 内容）✅
  - 实现弃用功能（恢复原始内容）✅
- **Acceptance Criteria Addressed**: AC-6, AC-7, AC-8
- **Test Requirements**:
  - `programmatic` TR-10.1: AI 内容正确填充 ✅
  - `programmatic` TR-10.2: 对话框正确显示 ✅
  - `programmatic` TR-10.3: 采纳功能正确 ✅
  - `programmatic` TR-10.4: 弃用功能正确 ✅
- **Notes**: 使用 JavaFX 的 Alert 或自定义对话框

## [x] Task 11: 实现错误处理
- **Priority**: P1
- **Depends On**: Task 10
- **Description**: 
  - 处理 AI API 调用失败的情况 ✅
  - 处理网络异常的情况 ✅
  - 处理返回格式错误的情况 ✅
  - 显示友好的错误提示信息 ✅
  - 恢复按钮可用状态 ✅
- **Acceptance Criteria Addressed**: AC-9
- **Test Requirements**:
  - `programmatic` TR-11.1: 各种错误情况正确处理 ✅
  - `human-judgement` TR-11.2: 错误提示友好清晰 ✅
  - `programmatic` TR-11.3: 按钮状态正确恢复 ✅
- **Notes**: 参考现有的错误处理方式

## [x] Task 12: 功能集成测试和优化
- **Priority**: P2
- **Depends On**: Task 11
- **Description**: 
  - 端到端完整功能测试 ✅
  - UI 交互优化 ✅
  - 用户体验调优 ✅
  - 日志完善 ✅
- **Acceptance Criteria Addressed**: AC-1 to AC-10
- **Test Requirements**:
  - `human-judgement` TR-12.1: 完整流程测试通过 ✅
  - `human-judgement` TR-12.2: UI 响应流畅 ✅
  - `human-judgement` TR-12.3: 日志输出规范 ✅
- **Notes**: 进行全面的功能验证
