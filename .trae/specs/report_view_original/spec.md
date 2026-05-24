# 双击举报查看原文功能 - Product Requirement Document

## Overview
- **Summary**: 在举报处理页面，双击某个举报后，能打开对应的帖子/评论原文供管理员查看详情
- **Purpose**: 让管理员在处理举报前能够先查看被举报内容的详情，做出更准确的判断
- **Target Users**: 管理员（ROLE_ADMIN）和超级管理员（ROLE_SUPER）

## Goals
- 双击举报列表中的某条举报记录
- 根据举报类型（帖子/评论）打开对应的详情弹窗
- 弹窗中展示被举报内容的完整信息

## Non-Goals (Out of Scope)
- 修改举报处理流程
- 修改后端数据库结构

## Background & Context
- 前端已有 getPostDetail() 方法可以获取帖子详情
- 后端缺少获取单个评论详情的接口，需要先添加
- 举报记录包含 targetType（1=帖子，2=评论）和 targetId

## Functional Requirements
- **FR-1**: 双击举报列表中的某条记录触发详情查看
- **FR-2**: 根据举报类型打开对应的内容（帖子/评论）
- **FR-3**: 弹窗中展示内容的完整信息（作者、内容、时间等）

## Non-Functional Requirements
- **NFR-1**: 弹窗样式与现有弹窗保持一致
- **NFR-2**: 内容加载响应时间 < 3秒

## Constraints
- **Technical**: 需复用现有的前端代码风格
- **Business**: 仅管理员和超级管理员可用此功能

## Assumptions
- 需要在后端添加获取单个评论详情的接口
- 帖子详情获取接口已存在且正常工作

## Acceptance Criteria

### AC-1: 双击触发详情查看
- **Given**: 管理员在举报处理页面
- **When**: 双击某条举报记录
- **Then**: 打开详情弹窗
- **Verification**: `human-judgment`

### AC-2: 帖子举报显示帖子详情
- **Given**: 双击的举报是帖子类型（targetType=1）
- **When**: 弹窗打开
- **Then**: 显示帖子的标题、内容、作者、发布时间等信息
- **Verification**: `human-judgment`

### AC-3: 评论举报显示评论详情
- **Given**: 双击的举报是评论类型（targetType=2）
- **When**: 弹窗打开
- **Then**: 显示评论的内容、作者、发布时间等信息
- **Verification**: `human-judgment`

## Open Questions
- 无
