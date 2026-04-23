# Person 表个人资料字段实现 - 产品需求文档

## Overview
- **Summary**: 在现有校园论坛系统的个人资料页面中实现 Person 表的所有字段，包括姓名、性别、生日、邮箱、电话、地址、个人简介、身份证号、学院等字段的查看和编辑功能。
- **Purpose**: 完善用户个人资料，提供更完整的用户信息管理功能，使系统更符合校园场景的需求。
- **Target Users**: 校园论坛的所有注册用户

## Goals
- 在后端 API 中支持 Person 表字段的获取和更新
- 在前端 User 模型中添加 Person 表相关字段
- 在前端个人资料页面中展示所有 Person 表字段
- 实现 Person 表字段的编辑和保存功能

## Non-Goals (Out of Scope)
- 不实现照片上传功能（暂不处理 photo 二进制字段）
- 不修改人员编号（num）字段，该字段为只读
- 不修改人员类型（type）字段，该字段由系统管理

## Background & Context
- 现有系统有 User 表和 Person 表，User 表关联 Person 表
- Person 表包含字段：personId, num, name, type, dept, card, gender, birthday, email, phone, address, introduce, photo
- 当前个人资料页面只展示了 User 表的部分字段：学号、昵称、个性签名、头像
- 技术栈：Spring Boot 3.2.x + JavaFX 21 + MySQL

## Functional Requirements
- **FR-1**: 后端 API /api/bbs/user/me 应返回 User 和 Person 表的所有相关字段
- **FR-2**: 后端 API /api/bbs/user/me (PUT) 应支持更新 Person 表字段：name, gender, birthday, email, phone, address, introduce, dept
- **FR-3**: 前端 User 模型应包含 Person 表的所有相关字段
- **FR-4**: 前端个人资料页面应展示所有 Person 表字段（name, dept, gender, birthday, email, phone, address, introduce）
- **FR-5**: 前端个人资料页面应支持编辑并保存 Person 表字段

## Non-Functional Requirements
- **NFR-1**: 邮箱字段应进行格式验证
- **NFR-2**: 手机号字段应进行格式验证
- **NFR-3**: 生日字段应使用日期格式展示和编辑
- **NFR-4**: 性别字段应使用下拉选择（男/女）

## Constraints
- **Technical**: 基于现有项目架构，使用 Spring Boot 和 JavaFX
- **Business**: Person 表的 num（编号）和 type（类型）字段不可由用户编辑
- **Dependencies**: 依赖现有的 PersonRepository, UserRepository, BbsUserService

## Assumptions
- User 表和 Person 表通过 personId 正确关联
- 现有用户认证机制正常工作
- 前端已有的编辑/保存模式可以复用

## Acceptance Criteria

### AC-1: 获取用户信息时包含 Person 字段
- **Given**: 用户已登录系统
- **When**: 用户调用 GET /api/bbs/user/me
- **Then**: 系统返回的用户信息包含 Person 表字段：name, dept, gender, birthday, email, phone, address, introduce
- **Verification**: `programmatic`

### AC-2: 更新用户信息时支持 Person 字段
- **Given**: 用户已登录系统
- **When**: 用户调用 PUT /api/bbs/user/me，传入 Person 字段（name, dept, gender, birthday, email, phone, address, introduce）
- **Then**: 系统更新 Person 表对应字段并返回更新后的数据
- **Verification**: `programmatic`

### AC-3: 邮箱格式验证
- **Given**: 用户编辑个人资料
- **When**: 用户输入不符合格式的邮箱
- **Then**: 系统提示邮箱格式错误并不予保存
- **Verification**: `programmatic`

### AC-4: 前端展示所有 Person 字段
- **Given**: 用户打开个人资料页面
- **When**: 页面加载完成
- **Then**: 页面展示所有 Person 字段：姓名、学院、性别、生日、邮箱、电话、地址、个人简介
- **Verification**: `human-judgment`

### AC-5: 前端编辑保存 Person 字段
- **Given**: 用户在个人资料页面点击编辑
- **When**: 用户修改 Person 字段并保存
- **Then**: 数据成功保存到后端
- **Verification**: `programmatic`

## Open Questions
- 身份证号（card）字段是否需要展示和编辑？考虑隐私问题，建议暂不实现。
