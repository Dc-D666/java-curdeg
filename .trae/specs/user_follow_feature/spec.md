# 用户关注功能 - Product Requirement Document

## Overview
- **Summary**: 为校园论坛系统添加用户关注功能，用户可以关注感兴趣的其他用户，并在这些用户发布新帖子时收到通知
- **Purpose**: 增强用户互动性，让用户能够及时获取关注用户的动态
- **Target Users**: 校园论坛的所有注册用户

## Goals
- 实现用户关注/取消关注功能
- 在用户头像旁显示关注状态
- 关注用户发布新帖子时推送通知
- 查询关注列表和粉丝列表

## Non-Goals (Out of Scope)
- 私信功能
- 用户分组管理
- 推荐算法
- 批量关注/取消关注

## Background & Context
- 项目已有完善的通知系统(BbsNotification)
- 已有用户信息管理系统
- 已有帖子发布和管理功能

## Functional Requirements
- **FR-1**: 用户可以关注/取消关注其他用户
- **FR-2**: 在用户头像旁显示"已关注"标识
- **FR-3**: 关注的用户发布新帖子时，向关注者发送通知
- **FR-4**: 用户可以查看自己的关注列表和粉丝列表
- **FR-5**: 可以查询是否已关注某个用户

## Non-Functional Requirements
- **NFR-1**: 关注/取消关注操作响应时间 < 500ms
- **NFR-2**: 通知发送延迟 < 1s
- **NFR-3**: 支持至少10000个关注关系
- **NFR-4**: 前端UI响应流畅

## Constraints
- **Technical**: Java 21, Spring Boot 3.2.5, MySQL数据库, JavaFX 21
- **Business**: 遵循现有项目代码风格和架构
- **Dependencies**: 依赖现有的用户系统、通知系统和帖子系统

## Assumptions
- 现有通知系统可以正常工作
- 数据库支持新增表
- 前端界面可以灵活调整

## Acceptance Criteria

### AC-1: 关注/取消关注用户
- **Given**: 用户已登录且未关注目标用户
- **When**: 用户点击关注按钮
- **Then**: 系统创建关注关系，按钮状态变为"已关注"
- **Verification**: `programmatic`

### AC-2: 显示关注状态
- **Given**: 用户已登录并访问包含其他用户信息的页面
- **When**: 页面加载时
- **Then**: 在已关注用户的头像旁显示"已关注"标识
- **Verification**: `human-judgment`

### AC-3: 新帖子通知
- **Given**: 用户A关注了用户B
- **When**: 用户B发布新帖子
- **Then**: 用户A收到新通知，内容包含"用户B发布了新帖子"
- **Verification**: `programmatic`

### AC-4: 查看关注列表
- **Given**: 用户已登录
- **When**: 用户查看关注列表
- **Then**: 显示所有已关注的用户列表
- **Verification**: `programmatic`

### AC-5: 查看粉丝列表
- **Given**: 用户已登录
- **When**: 用户查看粉丝列表
- **Then**: 显示所有关注自己的用户列表
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要在关注列表中显示用户的最新动态？
- [ ] 是否需要支持取消关注时确认弹窗？
