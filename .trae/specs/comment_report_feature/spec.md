# 评论举报功能 - Product Requirement Document

## Overview
- **Summary**: 在现有的校园论坛系统中添加评论举报功能，让用户可以举报违规评论
- **Purpose**: 完善论坛的内容管理机制，让用户可以对违规评论进行举报，维护良好的社区环境
- **Target Users**: 所有已登录的论坛用户（非禁言用户）

## Goals
- 为每个评论添加举报按钮
- 实现评论举报的完整流程
- 复用现有的帖子举报后端接口和前端逻辑

## Non-Goals (Out of Scope)
- 修改后端举报接口（后端已支持评论举报）
- 添加新的数据库表结构
- 修改举报处理流程

## Background & Context
- 后端已经完全支持评论举报功能：`targetType=1` 表示帖子，`targetType=2` 表示评论
- 前端已经有帖子举报的完整实现
- 评论列表在 `PostDetailController.java` 中动态生成

## Functional Requirements
- **FR-1**: 每个评论显示举报按钮
- **FR-2**: 点击评论举报按钮打开举报对话框
- **FR-3**: 提交评论举报请求到后端
- **FR-4**: 只有登录且未禁言且非评论作者的用户可以看到举报按钮

## Non-Functional Requirements
- **NFR-1**: 评论举报按钮样式与帖子举报按钮保持一致
- **NFR-2**: 举报对话框的用户体验与帖子举报保持一致
- **NFR-3**: 举报操作响应时间 < 3秒

## Constraints
- **Technical**: 必须复用现有的后端接口和前端举报逻辑
- **Business**: 功能实现必须符合现有的权限控制规则
- **Dependencies**: 依赖现有的 `HttpRequestUtil.submitReport()` 方法

## Assumptions
- 后端的评论举报功能已经完整且正常工作
- 评论对象有唯一的 ID 字段
- 当前用户信息在 `PostDetailController` 中可获取

## Acceptance Criteria

### AC-1: 评论显示举报按钮
- **Given**: 用户已登录且未被禁言
- **When**: 查看帖子详情页的评论列表
- **Then**: 每个评论（非用户自己发布的）都显示举报按钮
- **Verification**: `human-judgment`

### AC-2: 打开评论举报对话框
- **Given**: 用户点击某条评论的举报按钮
- **When**: 用户点击举报按钮
- **Then**: 打开举报对话框，标题包含评论内容预览
- **Verification**: `human-judgment`

### AC-3: 提交评论举报
- **Given**: 用户在举报对话框中输入举报原因
- **When**: 用户点击确认提交
- **Then**: 调用后端接口提交评论举报（targetType=2），并显示成功提示
- **Verification**: `human-judgment`

### AC-4: 权限控制正确
- **Given**: 用户未登录或已被禁言或是评论作者
- **When**: 查看评论
- **Then**: 不显示举报按钮
- **Verification**: `human-judgment`

## Open Questions
- 无
