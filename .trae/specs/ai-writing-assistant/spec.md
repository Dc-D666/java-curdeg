# AI 写作助手功能 - Product Requirement Document

## Overview
- **Summary**: 在校园论坛的发布帖子页面新增 AI 写作助手功能，提供 AI 帮写、AI 续写、AI 润色三个预设功能，支持用户自定义指令，让 AI 辅助用户快速生成和优化帖子内容。
- **Purpose**: 解决用户发布帖子时内容创作困难、表达不够流畅等问题，降低内容创作门槛，提升用户发布帖子的效率和质量。
- **Target Users**: 校园论坛的所有注册用户（学生、管理员等）。

## Goals
- 在发布帖子页面新增 AI 指令输入区域
- 提供三个预设按钮：AI 帮写、AI 续写、AI 润色
- 用户可自定义 AI 指令
- 将标题、正文和指令发送给大模型 API
- 解析 AI 返回结果并填充到输入框
- 提供"采纳/弃用"确认机制
- 复用现有 AI 服务的技术栈

## Non-Goals (Out of Scope)
- 不实现流式 AI 生成（仅使用同步调用）
- 不实现历史指令记录功能
- 不实现多语言支持（仅中文）
- 不实现 AI 模型选择功能
- 不实现自定义提示词保存功能

## Background & Context
- 项目已有完整的 AI 内容审核和 AI 搜索功能，可复用 AI API 调用逻辑
- ContentSummaryService 已有严格的 JSON 格式输出控制经验
- 前端使用 JavaFX + FXML，已有 PostPublishController
- 后端使用 Spring Boot + Spring Data JPA
- AI 服务使用 Qwen3.5-Flash 模型
- 发布帖子页面已有完整的 UI 结构

## Functional Requirements
- **FR-1**: 在发布帖子页面的内容输入框下方新增 AI 指令输入区域
- **FR-2**: 提供三个预设按钮："AI 帮写"、"AI 续写"、"AI 润色"
- **FR-3**: 点击预设按钮时自动填充对应的预定义提示词
- **FR-4**: AI 指令输入框非空时显示"AI 生成"提交按钮
- **FR-5**: 点击"AI 生成"按钮时，将标题、正文和指令发送给后端
- **FR-6**: 后端调用大模型 API 并返回结构化结果
- **FR-7**: AI 返回结果包含优化后的标题和内容
- **FR-8**: 前端暂存原始内容，填充 AI 生成内容
- **FR-9**: 弹出"采纳/弃用"确认对话框
- **FR-10**: 用户选择"采纳"时保留 AI 生成内容
- **FR-11**: 用户选择"弃用"时恢复原始内容
- **FR-12**: 提供加载状态和完善的错误处理

## Non-Functional Requirements
- **NFR-1**: AI 生成请求响应时间应控制在 30 秒以内
- **NFR-2**: AI 生成内容应符合校园论坛风格，语言自然
- **NFR-3**: 前端界面响应流畅，无卡顿
- **NFR-4**: AI API 调用失败时应有友好的错误提示
- **NFR-5**: 大模型返回格式异常时应有容错处理

## Constraints
- **Technical**: 
  - 复用现有的 AI API 调用逻辑（参考 ContentSummaryService 和 AiSearchService）
  - 使用 JavaFX 进行前端扩展
  - 后端使用 Spring Boot
  - AI 模型：Qwen3.5-Flash
  - 要求大模型严格返回 JSON 格式
- **Business**: 
  - 控制 API 调用成本
  - 不影响现有发布帖子功能的稳定性
- **Dependencies**: 
  - 现有的 AI API 服务
  - 现有的 PostPublishController 和发布帖子功能

## Assumptions
- AI API 可用于写作任务（需调整 Prompt）
- 大模型能够按要求返回严格的 JSON 格式
- 用户对 AI 生成内容有合理的质量预期
- 现有 ModerationConfig 配置可复用

## Acceptance Criteria

### AC-1: AI 指令输入区域显示
- **Given**: 用户进入发布帖子页面
- **When**: 页面加载完成
- **Then**: 用户可以看到内容输入框下方新增的 AI 指令输入区域
- **Verification**: `human-judgment`
- **Notes**: 包含三个预设按钮、AI 指令输入框、提交按钮

### AC-2: 预设按钮功能
- **Given**: 用户看到 AI 指令输入区域
- **When**: 用户点击"AI 帮写"、"AI 续写"或"AI 润色"任意一个按钮
- **Then**: 对应的预定义提示词自动填充到 AI 指令输入框
- **Verification**: `programmatic`

### AC-3: 提交按钮状态控制
- **Given**: 用户在 AI 指令输入区域
- **When**: AI 指令输入框内容为空时
- **Then**: "AI 生成"按钮为禁用状态
- **Verification**: `programmatic`

### AC-4: 提交按钮启用
- **Given**: 用户在 AI 指令输入框中输入内容
- **When**: AI 指令输入框内容非空时
- **Then**: "AI 生成"按钮变为启用状态
- **Verification**: `programmatic`

### AC-5: AI 生成加载状态
- **Given**: 用户输入了 AI 指令并点击"AI 生成"按钮
- **When**: 请求发送中
- **Then**: 
  - 显示加载状态提示
  - "AI 生成"按钮变为禁用状态
  - 防止重复点击
- **Verification**: `human-judgment`

### AC-6: AI 生成结果展示
- **Given**: AI 生成请求成功
- **When**: 后端返回结构化结果
- **Then**: 
  - 暂存用户原始输入的标题和内容
  - 将 AI 生成的标题和内容填充到对应的输入框
  - 弹出"采纳/弃用"确认对话框
- **Verification**: `human-judgment`

### AC-7: 采纳功能
- **Given**: AI 生成内容已填充到输入框并显示确认对话框
- **When**: 用户点击"采纳"按钮
- **Then**: 
  - 保留 AI 生成的内容
  - 关闭确认对话框
  - 用户可以继续编辑或发布
- **Verification**: `programmatic`

### AC-8: 弃用功能
- **Given**: AI 生成内容已填充到输入框并显示确认对话框
- **When**: 用户点击"弃用"按钮
- **Then**: 
  - 恢复用户原始输入的标题和内容
  - 关闭确认对话框
- **Verification**: `programmatic`

### AC-9: 错误处理
- **Given**: AI API 调用失败、网络异常或返回格式错误
- **When**: 用户点击"AI 生成"按钮
- **Then**: 
  - 显示友好的错误提示信息
  - "AI 生成"按钮恢复可用状态
  - 不影响用户原始内容
- **Verification**: `programmatic`

### AC-10: 大模型输出格式
- **Given**: 后端收到 AI 生成请求
- **When**: 调用大模型 API
- **Then**: 
  - 使用严格的 System Prompt 要求大模型只返回 JSON
  - 返回格式包含：title、content、instructionSuggestion、success 字段
  - 对大模型返回进行容错解析
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要记录 AI 生成历史？（建议 MVP 版本不实现）
- [ ] 是否需要限制用户的 AI 生成使用频率？（建议暂时不限制，观察使用情况）
- [ ] 预设提示词是否需要支持用户自定义？（建议 MVP 版本不实现）
- [ ] 是否需要支持在编辑帖子页面也使用 AI 写作助手？（建议后续版本添加）
