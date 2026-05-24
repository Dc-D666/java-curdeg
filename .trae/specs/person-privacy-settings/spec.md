# Person 表隐私设置 - Product Requirement Document

## Overview
- **Summary**: 为 Person 表的每个字段添加隐私设置功能，用户可以设置每个个人信息的可见范围（全员可见/相互关注用户可见/仅自己可见）
- **Purpose**: 增强用户隐私保护，允许用户精细控制个人信息的可见性
- **Target Users**: 所有校园论坛注册用户

## Goals
- 为 Person 表的 8 个字段（name, dept, gender, birthday, email, phone, address, introduce）添加隐私设置
- 用户可以设置每个字段的可见性："全员可见", "相互关注用户可见", "仅自己可见"
- 后端支持隐私设置的获取和更新
- 在查看其他用户个人信息时，根据隐私设置过滤显示内容

## Non-Goals (Out of Scope)
- 不修改照片（photo）的隐私设置（暂不实现）
- 不修改学号、昵称等 User 表字段的隐私设置（本次仅处理 Person 表字段）

## Background & Context
- 现有 Person 表包含字段：name, dept, gender, birthday, email, phone, address, introduce
- 需要为每个字段新增对应的可见性字段
- 隐私设置枚举值："PUBLIC" (全员可见), "FOLLOWING" (相互关注可见), "PRIVATE" (仅自己可见)

## Functional Requirements
- **FR-1**: Person 表新增 8 个字段，分别对应 8 个信息字段的隐私设置
- **FR-2**: 后端 API 支持获取当前用户的隐私设置
- **FR-3**: 后端 API 支持更新当前用户的隐私设置
- **FR-4**: 在获取其他用户的个人信息时，根据隐私设置过滤可见字段
- **FR-5**: 检查两个用户是否相互关注的功能

## Non-Functional Requirements
- **NFR-1**: 隐私设置默认值为 "全员可见"
- **NFR-2**: 隐私设置更新时要进行权限验证，只能修改自己的设置

## Constraints
- **Technical**: 基于 Spring Boot 3.2.x 和 JPA
- **Business**: 默认所有字段全员可见
- **Dependencies**: 依赖现有的 bbs_follow 表

## Assumptions
- bbs_follow 表可以正确判断用户关注关系
- 用户只能修改自己的隐私设置
- 未设置的隐私设置默认为 "PUBLIC"

## Acceptance Criteria

### AC-1: 数据库表结构更新
- **Given**: 数据库存在
- **When**: 执行 SQL 更新语句
- **Then**: Person 表成功添加 8 个隐私设置字段
- **Verification**: `programmatic`

### AC-2: 获取当前用户隐私设置
- **Given**: 用户已登录
- **When**: 调用获取隐私设置 API
- **Then**: 返回用户当前所有字段的隐私设置
- **Verification**: `programmatic`

### AC-3: 更新当前用户隐私设置
- **Given**: 用户已登录
- **When**: 调用更新隐私设置 API
- **Then**: 隐私设置成功保存到数据库
- **Verification**: `programmatic`

### AC-4: 查看其他用户信息时隐私过滤
- **Given**: 用户 A 查看用户 B 的个人信息
- **When**: 用户 B 的字段设置了不同的可见性
- **Then**: 根据隐私设置过滤显示字段
- **Verification**: `programmatic`

### AC-5: 判断相互关注关系
- **Given**: 两个用户之间存在关注关系
- **When**: 检查相互关注
- **Then**: 正确判断是否相互关注
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要为 User 表字段也添加隐私设置？(本次暂不实现)
