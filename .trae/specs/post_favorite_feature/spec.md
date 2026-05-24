# 收藏帖子功能 - 产品需求文档

## Overview
- **Summary**: 在学生交流社区中新增"收藏帖子"功能，用户可以收藏感兴趣的帖子，并在帖子详情页查看和管理收藏状态。
- **Purpose**: 方便用户保存和快速访问感兴趣的帖子，提升用户体验和社区活跃度。
- **Target Users**: 所有注册登录的学生用户。

## Goals
- 用户可以在帖子详情页收藏/取消收藏帖子
- 收藏按钮放置在点赞按钮旁边
- 收藏状态实时更新并正确显示
- 收藏数量在帖子上正确显示
- 功能参考现有点赞功能实现

## Non-Goals (Out of Scope)
- 不实现收藏列表页面（可以后续迭代）
- 不实现收藏分类和标签功能
- 不实现收藏通知功能

## Background & Context
- 项目已有点赞功能作为参考实现
- 使用 JavaFX 前端 + Spring Boot 后端架构
- 数据库使用 JPA 进行 ORM 映射
- 已有完整的用户认证和权限系统

## Functional Requirements
- **FR-1**: 用户可以收藏帖子
- **FR-2**: 用户可以取消收藏帖子
- **FR-3**: 帖子详情页显示当前用户的收藏状态
- **FR-4**: 帖子显示收藏数量
- **FR-5**: 收藏按钮样式根据收藏状态变化

## Non-Functional Requirements
- **NFR-1**: 收藏操作响应时间 < 500ms
- **NFR-2**: 收藏状态实时更新
- **NFR-3**: 与现有点赞功能保持一致的代码风格和架构

## Constraints
- **Technical**: 必须使用与点赞功能相同的技术栈（Java 21, Spring Boot, JavaFX, JPA）
- **Business**: 功能必须参考现有点赞功能的实现方式
- **Dependencies**: 依赖现有的用户认证系统和帖子服务

## Assumptions
- 后端服务运行在 http://localhost:22223
- 用户必须登录才能收藏帖子
- 帖子状态正常（未下架）时才能收藏

## Acceptance Criteria

### AC-1: 收藏按钮显示
- **Given**: 用户打开帖子详情页
- **When**: 页面加载完成
- **Then**: 在点赞按钮旁边显示收藏按钮
- **Verification**: `human-judgment`

### AC-2: 收藏帖子
- **Given**: 用户已登录，帖子未被收藏
- **When**: 用户点击收藏按钮
- **Then**: 帖子被收藏，收藏按钮状态更新，收藏数量+1
- **Verification**: `programmatic`

### AC-3: 取消收藏
- **Given**: 用户已登录，帖子已被收藏
- **When**: 用户点击收藏按钮
- **Then**: 帖子被取消收藏，收藏按钮状态更新，收藏数量-1
- **Verification**: `programmatic`

### AC-4: 收藏状态显示
- **Given**: 用户打开已收藏的帖子详情页
- **When**: 页面加载完成
- **Then**: 收藏按钮显示为已收藏状态
- **Verification**: `programmatic`

### AC-5: 收藏数量显示
- **Given**: 任意帖子
- **When**: 查看帖子信息
- **Then**: 正确显示该帖子的收藏数量
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要在帖子列表也显示收藏状态？（暂不实现）
- [ ] 是否需要收藏列表页面？（暂不实现）
