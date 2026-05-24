# 校园论坛个人中心 - 产品需求文档

## Overview
- **Summary**: 为校园论坛系统开发完整的个人中心功能，包括个人资料管理、我的帖子、我的收藏、我的关注/粉丝、数据统计和账号设置等模块。
- **Purpose**: 为用户提供一站式的个人信息和内容管理平台，提升用户体验和社区活跃度。
- **Target Users**: 校园论坛的所有注册用户（学生、教师、管理员）

## Goals
- 提供完整的个人资料查看和编辑功能
- 实现我的帖子、我的收藏等内容管理功能
- 实现关注/粉丝的社交关系管理
- 提供用户数据统计展示
- 实现密码修改等账号安全功能

## Non-Goals (Out of Scope)
- 不实现复杂的图表统计功能（可后续优化）
- 不实现高级隐私设置（可后续优化）
- 不实现第三方账号绑定功能
- 不实现账号注销功能

## Background & Context
- 现有系统已有基础的用户登录、注册功能
- 已有帖子管理、关注功能、收藏功能的基础实现
- 需要基于现有功能扩展个人中心模块
- 使用技术栈：Spring Boot 3.x + Spring Security + JPA + MySQL

## Functional Requirements
- **FR-1**: 用户可以查看和编辑个人基本信息（昵称、头像、个性签名）
- **FR-2**: 用户可以查看自己发布的所有帖子列表（支持分页）
- **FR-3**: 用户可以查看自己收藏的所有帖子列表（支持分页）
- **FR-4**: 用户可以查看自己关注的用户列表和粉丝列表
- **FR-5**: 用户可以查看个人数据统计（发帖数、评论数、获赞数、被收藏数、总浏览量等）
- **FR-6**: 用户可以修改登录密码

## Non-Functional Requirements
- **NFR-1**: 所有API需要进行身份验证（使用JWT）
- **NFR-2**: 列表类API需要支持分页功能
- **NFR-3**: API响应时间应小于500ms（数据库查询优化）
- **NFR-4**: 输入参数需要进行有效性验证

## Constraints
- **Technical**: 使用Java 21, Spring Boot 3.2.x, 现有项目架构
- **Business**: 不改变现有数据库表结构，仅扩展功能
- **Dependencies**: 依赖现有的BbsPostRepository, BbsCommentRepository, BbsLikeRepository, BbsFavoriteRepository, BbsFollowRepository等

## Assumptions
- 现有用户认证机制正常工作
- 数据库表结构已包含所需字段
- 前端将通过HTTP REST API调用后端接口

## Acceptance Criteria

### AC-1: 获取和更新用户基本信息
- **Given**: 用户已登录系统
- **When**: 用户调用获取个人信息API
- **Then**: 系统返回用户的完整基本信息（昵称、头像、个性签名、学号、发帖数等）
- **Verification**: `programmatic`

- **Given**: 用户已登录系统
- **When**: 用户调用更新个人信息API，传入昵称、头像或个性签名
- **Then**: 系统更新用户信息并返回更新后的数据
- **Verification**: `programmatic`

### AC-2: 获取我的帖子列表
- **Given**: 用户已登录系统
- **When**: 用户调用获取我的帖子API
- **Then**: 系统返回该用户发布的所有帖子，分页展示，包含帖子标题、内容摘要、发布时间、点赞数、评论数等
- **Verification**: `programmatic`

### AC-3: 获取我的收藏列表
- **Given**: 用户已登录系统
- **When**: 用户调用获取我的收藏API
- **Then**: 系统返回该用户收藏的所有帖子，分页展示
- **Verification**: `programmatic`

### AC-4: 获取关注和粉丝列表
- **Given**: 用户已登录系统
- **When**: 用户调用获取关注列表或粉丝列表API
- **Then**: 系统返回对应列表，分页展示
- **Verification**: `programmatic`

### AC-5: 获取用户统计数据
- **Given**: 用户已登录系统
- **When**: 用户调用获取统计数据API
- **Then**: 系统返回该用户的统计数据，包括发帖数、评论数、获赞总数、被收藏总数、总浏览量、关注数、粉丝数等
- **Verification**: `programmatic`

### AC-6: 修改密码
- **Given**: 用户已登录系统
- **When**: 用户调用修改密码API，传入原密码和新密码
- **Then**: 系统验证原密码正确后，更新为新密码
- **Verification**: `programmatic`

## Open Questions
- 暂无
