
# AI搜索UI优化 - Product Requirement Document

## Overview
- **Summary**: 优化 AI搜索功能的用户界面，包括三个主要改进：1) 添加搜索进度提示，2) 在无相关帖子时引导用户发帖，3) 删除底部冗余的翻页控制。
- **Purpose**: 提升用户体验，让用户了解搜索进度，并在找不到相关内容时引导用户主动发帖解决问题。
- **Target Users**: 使用 AI搜索功能的校园论坛用户。

## Goals
1. 添加 AI搜索进度提示，显示当前搜索阶段（正在理解、正在连接、正在生成、回复完成）
2. 当搜索结果为空或无相关内容时，展示引导发帖的UI，方便用户一键发布问题
3. 简化普通搜索界面，移除对普通搜索作用不大的底部翻页控制
4. 保持现有功能不变，只做增量改进

## Non-Goals (Out of Scope)
1. 不修改后端搜索逻辑
2. 不修改其他页面的功能
3. 不修改前端CSS样式框架
4. 不改变现有搜索按钮的行为逻辑

## Background & Context
- **现有界面**: 已有 post-search.fxml 和 PostSearchController.java，功能基本完整，但缺少进度提示和空状态引导
- **当前问题**: 用户点击搜索后不知道进度，空结果时用户不知道该做什么，底部翻页对普通搜索价值不大
- **技术栈**: JavaFX + FXML + Spring Boot

## Functional Requirements
- **FR-1**: 添加 AI搜索进度状态显示组件，在搜索过程中显示当前阶段
- **FR-2**: 实现搜索进度更新逻辑，在关键时间点更新状态显示
- **FR-3**: 添加空结果引导发帖UI组件，当无相关帖子时显示
- **FR-4**: 实现一键发帖功能，自动填充搜索关键词为标题或内容
- **FR-5**: 移除底部翻页组件，简化界面

## Non-Functional Requirements
- **NFR-1**: 进度提示应清晰可见，不影响现有布局
- **NFR-2**: 引导发帖UI应友好美观，引导性强
- **NFR-3**: 改动应向后兼容，不影响现有功能使用

## Constraints
- **Technical**: 只修改前端 JavaFX代码（PostSearchController.java 和 post-search.fxml）
- **Business**: 保持现有API不变，只做前端UI改进
- **Dependencies**: 需要依赖现有的发帖功能（PostPublishController）

## Assumptions
1. 用户点击搜索后希望看到明确的进度反馈
2. 用户遇到无结果时可能愿意主动发帖提问
3. 发帖功能已经存在，可以复用
4. 普通搜索用户很少使用翻页功能

## Acceptance Criteria

### AC-1: 搜索进度提示正常显示
- **Given**: 用户点击AI搜索按钮
- **When**: 搜索开始执行
- **Then**: 界面显示"正在理解用户意图..." → "正在连接AI..." → "AI正在回复..." → "回复完成"等状态变化
- **Verification**: human-judgment
- **Notes**: 状态变化应自然流畅

### AC-2: 无结果时显示引导发帖UI
- **Given**: AI搜索返回空结果或很少结果
- **When**: 搜索完成
- **Then**: 显示"未找到相关帖子，要不要发布一个问题？"，并有一键发帖按钮
- **Verification**: human-judgment

### AC-3: 一键发帖功能正常工作
- **Given**: 用户看到引导发帖UI
- **When**: 点击"快速发帖"按钮
- **Then**: 跳转到发帖页面，并自动填充搜索关键词作为标题或内容
- **Verification**: programmatic

### AC-4: 底部翻页已移除
- **Given**: 用户打开帖子搜索页面
- **When**: 页面加载完成
- **Then**: 底部不再显示翻页控件（上一页、下一页、每页显示等）
- **Verification**: programmatic

## Open Questions
1. 引导发帖UI是只在AI搜索无结果时显示，还是在普通搜索无结果时也显示？（建议：都显示）
2. 一键发帖是打开新窗口还是跳转到新标签页？（建议：跳转到新标签页，复用现有逻辑）
3. 进度提示是放在搜索按钮附近还是AI回答区域附近？（建议：搜索按钮下方、AI回答区域上方）
