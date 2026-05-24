# AI搜索流式输出 - Product Requirement Document

## Overview
- **Summary**: 将现有的AI搜索功能改造为流式输出模式，用户可以在等待完整AI答案生成的同时，看到答案逐字/逐段地展现在屏幕上，提升用户体验。
- **Purpose**: 解决当前用户需要等待完整AI响应返回的问题，提供实时、流畅的搜索体验。
- **Target Users**: 校园论坛所有使用AI搜索功能的用户。

## Goals
- 实现AI搜索结果的流式输出
- 保持现有非流式接口的兼容性（向后兼容）
- 提供流畅的加载动画和用户体验

## Non-Goals (Out of Scope)
- 不改变AI搜索的算法或质量
- 不实现多轮对话功能
- 不修改其他搜索模式（标题/全文搜索）
- 不实现向量检索功能
- 不实现中途取消功能（作为未来优化考虑）
- 不实现流式输出中的进度条显示（作为未来优化考虑）

## Background & Context
- 现有系统：基于OpenAI兼容API的AI搜索功能已实现
- 当前实现：同步阻塞式调用，等待完整响应后显示
- API兼容性：已确认使用OpenAI格式，支持 `stream: true` 参数
- 前端技术栈：JavaFX + Task异步处理

## SSE 事件格式定义
后端将通过 SSE 发送以下事件类型：
- `posts`: 相关帖子列表（优先发送）
  ```json
  {
    "type": "posts",
    "data": [ ... ]
  }
  ```
- `content`: AI 文本增量
  ```json
  {
    "type": "content",
    "data": "文本片段"
  }
  ```
- `done`: 流式输出完成标记
  ```json
  {
    "type": "done"
  }
  ```
- `error`: 错误信息
  ```json
  {
    "type": "error",
    "message": "错误描述"
  }
  ```

## Functional Requirements
- **FR-1**: 后端实现SSE流式接口，按照定义的事件格式发送数据
- **FR-2**: 后端实现AI API流式调用，正确解析 OpenAI 兼容的流式格式
- **FR-3**: 后端优先发送相关帖子列表，再流式发送 AI 答案
- **FR-4**: 前端实现SSE连接和数据接收
- **FR-5**: 前端实现流式UI更新，包含缓冲策略避免过度刷新
- **FR-6**: 前端实现Markdown格式的渐进式渲染
- **FR-7**: 保留原有非流式接口的兼容性

## Non-Functional Requirements
- **NFR-1**: 首字响应时间 < 3秒
- **NFR-2**: 流式更新频率合理（>100ms/次更新以避免过度闪烁）
- **NFR-3**: 错误处理友好，网络异常时给出提示
- **NFR-4**: 性能不劣于现有非流式模式

## Constraints
- **Technical**: 
  - 必须使用Spring Boot 3.x现有技术栈
  - 前端必须使用JavaFX
  - AI API必须兼容OpenAI格式
- **Business**:
  - 保持API调用成本与现有方案一致
  - 开发周期控制在2周内
- **Dependencies**: 
  - 依赖OpenAI兼容的AI API（已存在）

## Assumptions
- AI API支持 `stream: true` 参数（已确认兼容性）
- 网络环境稳定
- 用户可以忍受轻微的延迟
- JavaFX 客户端有足够的性能处理流式更新
- 相关帖子搜索速度足够快，不会拖慢首字响应时间

## Acceptance Criteria

### AC-1: 流式输出正常工作
- **Given**: 用户输入搜索关键词并选择AI搜索模式
- **When**: 用户点击搜索按钮
- **Then**: 搜索结果以流式方式逐字/逐段显示在界面上
- **Verification**: `human-judgment`

### AC-2: 首字响应快
- **Given**: 用户进行AI搜索
- **When**: 等待第一个字显示
- **Then**: 首字显示时间 < 3秒（包含搜索帖子和获取评论的时间）
- **Verification**: `programmatic`

### AC-3: 相关帖子优先显示
- **Given**: 用户进行AI搜索
- **When**: 等待搜索结果
- **Then**: 相关帖子列表在AI开始生成答案之前或同时显示
- **Verification**: `human-judgment`

### AC-5: 错误处理友好
- **Given**: 网络异常或API调用失败
- **When**: 用户进行AI搜索
- **Then**: 显示友好的错误提示信息
- **Verification**: `human-judgment`

### AC-6: 向后兼容
- **Given**: 旧版客户端
- **When**: 调用原有的非流式接口
- **Then**: 接口仍然正常工作
- **Verification**: `programmatic`

### AC-7: 复制功能正常
- **Given**: AI搜索流式输出过程中或完成后
- **When**: 用户点击复制答案按钮
- **Then**: 已接收到的完整答案被正确复制到剪贴板
- **Verification**: `human-judgment`

## Open Questions
- 无，已在 Non-Goals 中明确不包含的功能