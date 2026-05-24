
# AI 帖子总结功能 - Product Requirement Document

## Overview
- **Summary**: 在校园论坛的帖子详情页面添加 AI 总结功能，使用 AI 模型自动生成帖子内容和评论区讨论的摘要，帮助用户快速了解帖子核心要点。
- **Purpose**: 解决用户浏览长帖子或评论较多的帖子时耗时过长的问题，提升用户体验和信息获取效率。
- **Target Users**: 校园论坛的所有注册用户（学生、管理员等）。

## Goals
- 用户可以在帖子详情页点击"AI 总结"按钮获取帖子摘要
- AI 总结应包含帖子内容和评论区讨论两部分
- 提供良好的用户体验（加载动画、错误处理、降级方案）
- 复用现有 AI 审核服务的技术栈，确保实现的一致性

## Non-Goals (Out of Scope)
- 不实现总结结果的编辑功能
- 不实现总结结果的点赞/分享功能
- 不实现多语言支持（仅中文）
- 不实现 AI 模型选择功能

## Background & Context
- 项目已有完整的 AI 内容审核功能（ContentModerationService），可复用 AI API 调用逻辑
- 前端使用 JavaFX，已有异步任务处理机制
- 后端使用 Spring Boot + Spring Data JPA
- AI 服务使用 Qwen3.5-Flash 模型
- 帖子详情页已有完整的 UI 结构，可轻松扩展

## Functional Requirements
- **FR-1**: 用户可在帖子详情页看到"AI 总结"按钮
- **FR-2**: 点击按钮后，显示加载动画，异步请求后端
- **FR-3**: 后端判断内容长度，短内容提示无需总结
- **FR-4**: AI 总结结果包含帖子摘要和评论热点两部分
- **FR-5**: 总结结果以友好格式展示在页面上
- **FR-6**: 提供完善的错误处理和用户提示

## Non-Functional Requirements
- **NFR-1**: 总结请求响应时间应控制在 15 秒以内
- **NFR-2**: 总结内容应准确、简洁（帖子摘要 50-200 字，评论摘要 50-150 字）
- **NFR-3**: 前端界面响应流畅，无卡顿
- **NFR-4**: AI API 调用失败时应有降级处理

## Constraints
- **Technical**: 
  - 复用现有的 ContentModerationService 的 AI 调用逻辑
  - 使用 JavaFX 进行前端扩展
  - 后端使用 Spring Boot
  - AI 模型：Qwen3.5-Flash
- **Business**: 
  - 控制 API 调用成本
  - 不影响现有功能的稳定性
- **Dependencies**: 
  - 现有的 AI 审核 API 服务
  - 现有的帖子和评论数据接口

## Assumptions
- AI 审核 API 可用于总结任务（需调整 Prompt）
- 帖子和评论数据的获取接口稳定可用
- 用户对 AI 总结有合理的质量预期

## Acceptance Criteria

### AC-1: AI 总结按钮显示
- **Given**: 用户进入帖子详情页
- **When**: 页面加载完成
- **Then**: 用户可以看到"AI 总结"按钮，位置在操作按钮区域
- **Verification**: `human-judgment`
- **Notes**: 按钮样式应与现有按钮一致

### AC-2: 短内容提示
- **Given**: 帖子内容少于 100 字
- **When**: 用户点击"AI 总结"按钮
- **Then**: 显示提示信息："内容较短，无需总结"，不调用 AI API
- **Verification**: `programmatic`

### AC-3: AI 总结加载状态
- **Given**: 帖子内容超过 100 字
- **When**: 用户点击"AI 总结"按钮
- **Then**: 显示加载动画（进度指示器），按钮变为禁用状态
- **Verification**: `human-judgment`

### AC-4: AI 总结结果展示
- **Given**: AI 总结请求成功
- **When**: 后端返回总结结果
- **Then**: 
  - 页面显示帖子摘要和评论热点两部分
  - 内容格式清晰易读
  - 加载动画消失，按钮恢复可用
- **Verification**: `human-judgment`

### AC-5: 错误处理
- **Given**: AI API 调用失败或网络异常
- **When**: 用户点击"AI 总结"按钮
- **Then**: 显示友好的错误提示信息，按钮恢复可用
- **Verification**: `programmatic`

### AC-6: 评论数据处理
- **Given**: 帖子有评论
- **When**: 生成总结
- **Then**: 选取最重要的评论（点赞数前 5 或最新 3 条）进行总结
- **Verification**: `human-judgment`

## Open Questions
- [ ] 是否需要缓存总结结果？（建议 MVP 版本不实现，后续版本添加）
- [ ] 是否需要限制用户的使用频率？（建议暂时不限制，观察使用情况）
- [ ] 总结结果的 UI 展示形式是展开/收起还是直接显示？（建议直接显示，可折叠）
