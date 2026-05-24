# 记住密码功能 - 产品需求文档

## Overview
- **Summary**: 在登录页面的账号密码框底下新增"记住密码"复选框，实现自动保存和填充用户名密码的功能。
- **Purpose**: 提升用户体验，避免每次登录都需要重新输入账号密码。
- **Target Users**: 所有使用登录功能的学生用户。

## Goals
- 用户可以勾选"记住密码"复选框
- 勾选后登录成功会保存用户名和密码
- 下次打开登录页面时自动填充已保存的用户名和密码
- 复选框状态也会被保存
- 不勾选时不保存密码或清除已保存的密码

## Non-Goals (Out of Scope)
- 不实现密码加密存储（仅本地存储，简单处理）
- 不实现多账号密码管理
- 不实现密码自动过期功能

## Background & Context
- 使用 JavaFX 作为前端框架
- 使用 Java 原生 Preferences API 进行本地存储
- 已有完整的登录功能

## Functional Requirements
- **FR-1**: 登录页面显示"记住密码"复选框
- **FR-2**: 勾选"记住密码"后登录成功保存用户名和密码
- **FR-3**: 打开登录页面时自动填充已保存的用户名和密码
- **FR-4**: 复选框状态与保存状态同步
- **FR-5**: 取消勾选时清除已保存的密码

## Non-Functional Requirements
- **NFR-1**: 用户名密码保存和读取响应时间 < 100ms
- **NFR-2**: 使用 Java Preferences API 进行本地存储
- **NFR-3**: 与现有登录功能无缝集成

## Constraints
- **Technical**: 必须使用 Java Preferences API 进行本地存储
- **Business**: 密码以明文形式存储在本地（简单实现）
- **Dependencies**: 依赖现有的 LoginController 和登录功能

## Assumptions
- 用户设备信任，本地存储安全风险可接受
- 仅保存最新的一套用户名密码

## Acceptance Criteria

### AC-1: 复选框显示
- **Given**: 用户打开登录页面
- **When**: 页面加载完成
- **Then**: 在账号密码框下方显示"记住密码"复选框
- **Verification**: `human-judgment`

### AC-2: 保存密码
- **Given**: 用户勾选"记住密码"，输入用户名密码并登录成功
- **When**: 登录成功
- **Then**: 用户名和密码被保存到本地
- **Verification**: `programmatic`

### AC-3: 自动填充
- **Given**: 用户之前已保存过用户名密码
- **When**: 打开登录页面
- **Then**: 用户名和密码自动填充到输入框
- **Verification**: `programmatic`

### AC-4: 复选框状态同步
- **Given**: 用户之前已保存过密码
- **When**: 打开登录页面
- **Then**: "记住密码"复选框自动勾选
- **Verification**: `programmatic`

### AC-5: 取消保存
- **Given**: 用户之前已保存过密码，本次登录不勾选"记住密码"
- **When**: 登录成功
- **Then**: 已保存的密码被清除
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要加密存储密码？（本次不实现）
