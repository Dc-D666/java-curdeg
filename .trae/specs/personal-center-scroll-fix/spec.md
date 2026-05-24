# 个人中心滚动和统计卡片优化 - 产品需求文档

## Overview
- **Summary**: 修复个人资料页面无法滚动的问题，将统计卡片改为莫兰迪配色
- **Purpose**: 解决用户无法滚动查看完整内容的问题，同时美化统计卡片
- **Target Users**: 所有使用个人中心功能的用户

## Goals
- 修复 personal-profile.fxml 的 ScrollPane 问题，确保可以正常滚动
- 将统计卡片改为莫兰迪配色方案，文字保持白色

## Non-Goals (Out of Scope)
- 不修改其他页面
- 不修改后端代码
- 不添加新功能

## Background & Context
- 虽然添加了 ScrollPane，但布局可能有问题导致无法滚动
- 当前统计卡片是深灰色，需要改为莫兰迪配色

## Functional Requirements
- **FR-1**: 修复 personal-profile.fxml 的 ScrollPane 布局
- **FR-2**: 将统计卡片样式改为莫兰迪配色

## Non-Functional Requirements
- **NFR-1**: 滚动功能流畅
- **NFR-2**: 莫兰迪配色美观，文字清晰可见

## Constraints
- **技术**: JavaFX + FXML + CSS

## Assumptions
- 只需要修改前端 UI 代码

## Acceptance Criteria

### AC-1: 个人资料页面可以正常滚动
- **Given**: 用户打开个人资料页面
- **When**: 内容超出可视区域时
- **Then**: 可以正常向下滚动查看所有内容
- **Verification**: `programmatic`

### AC-2: 统计卡片使用莫兰迪配色
- **Given**: 用户查看统计卡片
- **When**: 正常查看时
- **Then**: 统计卡片使用莫兰迪配色，文字为白色且清晰可见
- **Verification**: `human-judgment`

## Open Questions
- 无
