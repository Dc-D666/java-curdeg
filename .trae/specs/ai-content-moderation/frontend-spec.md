
# AI内容审核功能 - 前端实现 Product Requirement Document

## Overview
- **Summary**: 为校园论坛的JavaFX前端添加AI内容审核相关的UI展示和功能，包括审核状态显示、管理员审核页面等。
- **Purpose**: 让用户能够看到帖子的审核状态，管理员能够处理待人工审核的帖子。
- **Target Users**: 普通用户（查看审核状态）、管理员（人工审核帖子）

## Goals
1. 更新Post模型，支持审核状态字段
2. 在帖子列表和详情中显示不同颜色的审核状态标签
3. 添加管理员审核页面，展示待审核的帖子
4. 实现管理员审核操作（通过/拒绝）
5. 确保审核状态的显示逻辑与后端一致

## Non-Goals (Out of Scope)
- 实现前端审核逻辑（审核逻辑完全在后端）
- 添加通知弹出窗口（使用现有通知系统）
- 评论的审核UI显示（本期仅实现帖子审核）

## Background &amp; Context
- 后端已经完成AI审核功能的开发
- 使用JavaFX作为前端技术栈
- 已有PostListController、PostDetailController和AdminReportController等现有组件可参考

## Functional Requirements
- **FR1**: 更新Post模型，添加审核状态字段
- **FR2**: 在帖子列表中显示审核状态标签（颜色区分）
- **FR3**: 在帖子详情中显示审核状态
- **FR4**: 创建管理员审核页面
- **FR5**: 实现管理员审核操作（通过/拒绝）

## Non-Functional Requirements
- **NFR1**: UI响应时间 &lt; 100ms
- **NFR2**: 审核状态显示准确，与后端一致

## Constraints
- **Technical**: Java 21 + JavaFX
- **Dependencies**: 依赖后端API的更新

## Assumptions
- 后端API已经准备好
- 管理员角色能够访问管理员审核页面
- 审核状态字段名为moderationStatus

## Acceptance Criteria

### AC1: Post模型包含审核状态字段
- **Given**: 前端获取到帖子数据
- **When**: 后端返回的帖子数据包含moderationStatus字段
- **Then**: Post模型能够正确解析和存储该字段
- **Verification**: programmatic

### AC2: 帖子列表显示审核状态标签
- **Given**: 用户查看帖子列表
- **When**: 存在不同审核状态的帖子
- **Then**:
  - 审核中：橙色文字
  - 审核通过：绿色文字
  - 内容违规：红色文字
  - 待人工审核：橙色文字
- **Verification**: human-judgment

### AC3: 帖子详情显示审核状态
- **Given**: 用户查看帖子详情
- **When**: 帖子有审核状态
- **Then**: 审核状态标签正确显示在帖子详情页面
- **Verification**: human-judgment

### AC4: 管理员审核页面功能正常
- **Given**: 管理员登录并进入审核页面
- **When**: 有待审核的帖子
- **Then**: 列表显示待审核的帖子，管理员能够进行审核操作
- **Verification**: programmatic

### AC5: 管理员审核操作生效
- **Given**: 管理员对待审核的帖子进行操作
- **When**: 管理员点击通过或拒绝
- **Then**: 帖子状态更新，操作成功反馈显示
- **Verification**: programmatic

## Open Questions
- [ ] 审核历史记录是否需要在前端显示？（本期暂不做）
