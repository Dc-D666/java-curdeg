# 用户注册功能 - Product Requirement Document

## Overview
- **Summary**: 在登录页面实现完整的用户注册功能，包含学号、昵称、邮箱和密码（需重复两次确认）的输入，以及完整的合法性校验和清晰的错误提示。
- **Purpose**: 为用户提供自助注册账号的功能，避免完全依赖管理员手动添加用户。
- **Target Users**: 需要注册使用学生交流社区的学生用户。

## Goals
- 创建独立的注册界面
- 实现完整的表单输入（学号、昵称、邮箱、密码、重复密码）
- 实现输入合法性校验
- 提供清晰的错误提示
- 注册成功后自动登录进入主界面

## Non-Goals (Out of Scope)
- 不实现用户信息修改功能
- 不实现密码找回功能
- 不实现邮箱验证码验证
- 不实现用户头像上传功能

## Background & Context
- 现有登录界面已有"注册"按钮，但点击后显示"注册功能开发中..."
- 后端已有 `/api/auth/registerUser` API接口
- 数据库已有完整的 User 和 Person 表结构
- User表包含社区业务扩展字段（studentId, nickname等）

## Functional Requirements
- **FR-1**: 点击注册按钮跳转到注册界面
- **FR-2**: 注册表单包含学号、昵称、邮箱、密码、重复密码等输入字段
- **FR-3**: 实现输入合法性校验
- **FR-4**: 非法输入时提供清晰的错误提示
- **FR-5**: 注册成功后自动登录并跳转到主界面

## Non-Functional Requirements
- **NFR-1**: 表单输入时支持Tab键切换焦点
- **NFR-2**: 错误提示使用MessageDialog显示
- **NFR-3**: 注册失败后用户可重新输入

## Constraints
- **Technical**: 使用JavaFX框架，保持现有代码风格
- **Business**: 仅支持学生角色注册，默认为ROLE_STUDENT
- **Dependencies**: 依赖后端 `/api/auth/registerUser` API

## Assumptions
- 后端API能够正确处理注册请求
- 学号作为userName和studentId使用
- 昵称作为perName和nickname使用
- 邮箱需要符合标准格式
- 密码长度至少6位

## Acceptance Criteria

### AC-1: 注册界面跳转
- **Given**: 用户在登录界面
- **When**: 用户点击"注册"按钮
- **Then**: 切换到注册界面
- **Verification**: `human-judgment`

### AC-2: 注册表单字段完整
- **Given**: 用户在注册界面
- **When**: 查看注册表单
- **Then**: 看到学号、昵称、邮箱、密码、重复密码输入框，以及注册和返回按钮
- **Verification**: `human-judgment`

### AC-3: 学号校验
- **Given**: 用户在注册界面输入学号
- **When**: 学号不符合要求
- **Then**: 显示清晰的错误提示
- **Verification**: `programmatic`
- **Notes**: 学号必须为12位，不能为空

### AC-4: 昵称校验
- **Given**: 用户在注册界面输入昵称
- **When**: 昵称不符合要求
- **Then**: 显示清晰的错误提示
- **Verification**: `programmatic`
- **Notes**: 昵称长度1-16位，不能为空

### AC-5: 邮箱校验
- **Given**: 用户在注册界面输入邮箱
- **When**: 邮箱不符合格式要求
- **Then**: 显示清晰的错误提示
- **Verification**: `programmatic`
- **Notes**: 邮箱不能为空，且需符合标准邮箱格式

### AC-6: 密码校验
- **Given**: 用户在注册界面输入密码
- **When**: 密码不符合要求
- **Then**: 显示清晰的错误提示
- **Verification**: `programmatic`
- **Notes**: 密码不能为空，长度至少6位

### AC-7: 重复密码校验
- **Given**: 用户在注册界面输入重复密码
- **When**: 两次密码不一致
- **Then**: 显示清晰的错误提示
- **Verification**: `programmatic`

### AC-8: 注册成功
- **Given**: 用户填写所有合法信息并点击注册
- **When**: 后端返回注册成功
- **Then**: 自动登录并跳转到主界面
- **Verification**: `programmatic`

### AC-9: 注册失败处理
- **Given**: 用户提交注册信息
- **When**: 后端返回注册失败
- **Then**: 显示后端返回的错误信息
- **Verification**: `programmatic`

### AC-10: 返回功能
- **Given**: 用户在注册界面
- **When**: 用户点击"返回"按钮
- **Then**: 返回登录界面
- **Verification**: `human-judgment`

## Open Questions
- [ ] 后端API是否需要调整以更好地支持社区用户注册（studentId, nickname字段）？
