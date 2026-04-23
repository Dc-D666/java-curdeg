# 帖子列表每页条数选择功能 - Product Requirement Document

## Overview
- **Summary**: 在帖子列表页面的翻页按钮旁新增下拉选择器，允许用户选择每页显示 10/20/50 条帖子，默认为 20 条。
- **Purpose**: 提升用户体验，让用户可以根据自己的浏览习惯调整每页显示的帖子数量。
- **Target Users**: 所有使用帖子列表功能的用户。

## Goals
- 在帖子列表页面底部翻页区域新增每页条数选择下拉框
- 提供 10、20、50 三个选项
- 默认选择 20 条每页
- 切换选项后自动刷新列表并回到第一页
- 调整后端默认 pageSize 为 20

## Non-Goals (Out of Scope)
- 不实现用户偏好持久化（下次打开恢复选择）
- 不新增其他分页功能（如跳转到指定页）
- 不修改其他页面的分页逻辑

## Background & Context
- 当前前端默认 pageSize 为 10，后端默认也是 10，最大 50
- 后端 API 已支持 pageSize 参数，无需修改
- FXML 布局已有翻页按钮区域，可直接添加控件

## Functional Requirements
- **FR-1**: 翻页按钮旁新增"每页显示"下拉选择框
- **FR-2**: 下拉框包含 10、20、50 三个选项
- **FR-3**: 默认选择 20 条
- **FR-4**: 选择新值后立即刷新列表并回到第一页
- **FR-5**: 调整前端和后端的默认 pageSize 为 20

## Non-Functional Requirements
- **NFR-1**: 切换操作响应迅速，无明显延迟
- **NFR-2**: UI 布局合理，与现有控件风格一致

## Constraints
- **Technical**: 必须使用 JavaFX 原生控件，保持与现有代码风格一致
- **Business**: 无需修改后端 API，仅前端和后端默认值调整

## Assumptions
- 用户选择后不需要持久化保存
- 切换后自动回到第一页是合理行为

## Acceptance Criteria

### AC-1: 新增每页条数选择控件
- **Given**: 用户打开帖子列表页面
- **When**: 查看底部翻页区域
- **Then**: 能看到"每页显示"标签和下拉选择框，位于翻页按钮旁
- **Verification**: `human-judgment`

### AC-2: 下拉框包含正确选项
- **Given**: 用户查看每页条数下拉框
- **When**: 点击展开下拉框
- **Then**: 能看到 10、20、50 三个选项
- **Verification**: `programmatic`

### AC-3: 默认选中 20 条
- **Given**: 用户首次打开帖子列表页面
- **When**: 查看下拉框选中状态
- **Then**: 默认选中 20 条选项
- **Verification**: `programmatic`

### AC-4: 切换选项后刷新列表
- **Given**: 用户在帖子列表页面
- **When**: 在下拉框中选择新的条数（如从 20 改为 50）
- **Then**: 列表立即刷新，显示新条数的帖子，并回到第一页
- **Verification**: `programmatic`

### AC-5: 后端默认值调整为 20
- **Given**: 前端不传 pageSize 参数调用 API
- **When**: 后端处理请求
- **Then**: 默认使用 20 作为 pageSize
- **Verification**: `programmatic`

## Open Questions
- 无
