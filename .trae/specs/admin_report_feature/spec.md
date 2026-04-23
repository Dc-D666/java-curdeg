# 管理员举报处理功能 - Product Requirement Document

## Overview
- **Summary**: 在管理后台新增"举报处理"栏目，用于管理员查看和处理所有用户举报
- **Purpose**: 让管理员能够方便地查看所有举报记录，并对举报进行处理（删除内容或驳回举报）
- **Target Users**: 管理员（ROLE_ADMIN）和超级管理员（ROLE_SUPER）

## Goals
- 在管理后台下拉菜单中添加"举报处理"选项
- 实现管理员举报列表展示（包含分页）
- 实现举报处理功能（删除内容/驳回举报）
- 支持按举报状态筛选（待处理/已处理）

## Non-Goals (Out of Scope)
- 修改后端举报接口（后端已完整支持）
- 修改用户举报流程
- 添加新的数据库表结构

## Background & Context
- 后端已有完整的举报管理接口：
  - `/api/bbs/report/admin-list` - 获取管理员举报列表
  - `/api/bbs/report/{id}/handle` - 处理举报
- 前端已有"我的举报"功能（MyReportController），可以复用部分代码
- 主框架已有菜单管理机制，可直接添加新菜单项

## Functional Requirements
- **FR-1**: 在管理后台菜单中添加"举报处理"选项
- **FR-2**: 展示所有举报记录的列表（分页）
- **FR-3**: 支持按举报状态筛选（待处理/已处理/全部）
- **FR-4**: 对每条待处理举报提供"删除内容"和"驳回举报"两个处理选项
- **FR-5**: 处理时可输入处理备注

## Non-Functional Requirements
- **NFR-1**: 界面风格与现有页面保持一致
- **NFR-2**: 举报列表加载响应时间 < 3秒
- **NFR-3**: 处理操作有明确的成功/失败提示

## Constraints
- **Technical**: 必须复用现有的后端接口和前端代码风格
- **Business**: 仅管理员和超级管理员可见此功能
- **Dependencies**: 依赖现有的 `BbsReportService` 后端服务

## Assumptions
- 后端举报管理接口已完整且正常工作
- 只有管理员和超级管理员可以访问此功能
- 处理方式：1=删除内容，2=驳回举报

## Acceptance Criteria

### AC-1: 管理后台显示"举报处理"菜单
- **Given**: 用户已登录且是管理员或超级管理员
- **When**: 点击"管理后台"菜单
- **Then**: 下拉菜单中显示"举报处理"选项
- **Verification**: `human-judgment`

### AC-2: 举报列表展示
- **Given**: 管理员点击"举报处理"菜单
- **When**: 页面加载完成
- **Then**: 显示所有举报记录列表，包含举报类型、原因、状态、举报人、举报时间、处理人、处理时间、处理备注
- **Verification**: `human-judgment`

### AC-3: 状态筛选
- **Given**: 管理员在举报处理页面
- **When**: 选择不同的状态筛选条件（全部/待处理/已处理）
- **Then**: 列表按所选状态筛选显示
- **Verification**: `human-judgment`

### AC-4: 处理待处理举报
- **Given**: 管理员选中一条待处理的举报
- **When**: 点击"删除内容"或"驳回举报"按钮
- **Then**: 弹出对话框输入处理备注，确认后提交处理请求
- **Verification**: `human-judgment`

### AC-5: 权限控制
- **Given**: 用户是普通学生或教师
- **When**: 查看管理后台菜单
- **Then**: 不显示"举报处理"选项
- **Verification**: `human-judgment`

## Open Questions
- 无
