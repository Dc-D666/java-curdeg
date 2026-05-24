# 统计卡片可读性优化 - 产品需求文档

## Overview
- **Summary**: 修改统计卡片的背景颜色，让白色文字更清晰可见
- **Purpose**: 解决统计卡片文字看不清楚的问题
- **Target Users**: 所有使用个人中心功能的用户

## Goals
- 修改统计卡片背景色，确保白色文字清晰可见
- 保持美观的视觉效果

## Non-Goals (Out of Scope)
- 不修改其他页面
- 不修改布局结构

## Background & Context
- 用户反馈统计卡片的文字看不清楚
- 当前背景色对比度不够高
- 需要用更清晰的深色背景

## Functional Requirements
- **FR-1**: 修改统计卡片背景色，提高文字可读性

## Non-Functional Requirements
- **NFR-1**: 保持所有原有功能正常
- **NFR-2**: 文字清晰可见，无模糊

## Constraints
- **Technical**: JavaFX + CSS
- **Dependencies**: 必须与现有代码库完全兼容

## Assumptions
- 只修改CSS文件
- 不改FXML和Java代码

## Acceptance Criteria

### AC-1: 统计卡片文字清晰可见
- **Given**: 用户查看个人资料页面的统计卡片
- **When**: 正常查看
- **Then**: 发帖数、关注数、粉丝数的文字清晰可见，对比度足够高
- **Verification**: `human-judgment`

## Open Questions
- 无
