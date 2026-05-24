# 统计卡片颜色调整 - 产品需求文档

## Overview
- **Summary**: 调整统计卡片（发帖数、关注数、粉丝数）的颜色，确保文字清晰可见
- **Purpose**: 解决用户反馈的统计卡片上文字看不清楚的问题
- **Target Users**: 所有使用个人中心功能的用户

## Goals
- 调整统计卡片的背景色，确保文字清晰可见
- 保持美观的视觉效果

## Non-Goals (Out of Scope)
- 不修改其他页面
- 不修改功能

## Background & Context
- 用户反馈统计卡片上的文字看不清楚
- 当前使用的莫兰迪配色对比度不够高，导致文字难以阅读
- 需要调整颜色以提高可读性

## Functional Requirements
- **FR-1**: 调整统计卡片的颜色，确保文字清晰可见

## Non-Functional Requirements
- **NFR-1**: 保持视觉美观
- **NFR-2**: 颜色对比度足够高，确保在各种环境下都能看清

## Constraints
- **Technical**: JavaFX + CSS
- **Dependencies**: 必须与现有代码库完全兼容

## Assumptions
- 只需要调整CSS样式
- 不需要修改FXML或Java代码

## Acceptance Criteria

### AC-1: 统计卡片文字清晰可见
- **Given**: 用户查看个人资料页面的统计卡片
- **When**: 正常查看
- **Then**: 发帖数、关注数、粉丝数的文字清晰可见，没有模糊
- **Verification**: `human-judgment`

## Open Questions
- 无
