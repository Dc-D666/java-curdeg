# 个人中心Bug修复 - 产品需求文档

## Overview
- **Summary**: 修复个人资料页面的滚动问题和统计卡片样式问题
- **Purpose**: 解决用户无法查看完整个人资料内容的问题，以及统计卡片文字看不清的问题
- **Target Users**: 所有使用个人中心功能的用户

## Goals
- 修复个人资料页面无法向下滚动的问题，确保所有内容都能正常查看
- 优化统计卡片（发帖数、关注数、粉丝数）的样式，提高可读性

## Non-Goals (Out of Scope)
- 不修改后端业务逻辑
- 不添加新功能
- 不修改其他个人中心子页面

## Background & Context
- 个人资料页面当前使用 VBox 布局，没有 ScrollPane 包裹，导致内容超出可视区域时无法滚动
- 统计卡片的渐变背景色和文字对比度不够，导致文字看不清

## Functional Requirements
- **FR-1**: 在 personal-profile.fxml 中添加 ScrollPane 包裹所有内容，确保可以滚动查看
- **FR-2**: 优化统计卡片的样式，提高文字可读性

## Non-Functional Requirements
- **NFR-1**: 保持所有现有功能正常工作
- **NFR-2**: 滚动操作流畅，无卡顿
- **NFR-3**: 样式修改不影响其他页面功能

## Constraints
- **技术**: 使用 JavaFX + FXML + CSS，保持现有技术栈
- **向后兼容**: 保持与现有代码完全兼容

## Assumptions
- 现有功能代码无需修改
- 只修改 UI 布局和样式相关代码

## Acceptance Criteria

### AC-1: 个人资料页面可正常滚动
- **Given**: 用户打开个人资料页面
- **When**: 内容超出可视区域时
- **Then**: 可以正常向下滚动查看所有内容
- **Verification**: `programmatic`

### AC-2: 统计卡片文字清晰可见
- **Given**: 用户查看统计卡片（发帖数、关注数、粉丝数）
- **When**: 正常查看时
- **Then**: 文字清晰可见，对比度足够
- **Verification**: `human-judgment`

## Open Questions
- 无
