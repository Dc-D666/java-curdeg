# 严重 Bug 修复 - Product Requirement Document

## Overview
- **Summary**: 修复项目中发现的 3 个严重数据一致性 bug，确保数据库完整性
- **Purpose**: 解决删除操作导致的数据不一致问题，防止垃圾数据堆积
- **Target Users**: 所有社区系统用户和管理员

## Goals
- 修复删除帖子时的级联清理问题
- 修复删除评论时的级联删除子评论问题
- 增强统计数据的并发安全性

## Non-Goals (Out of Scope)
- 不重构数据库结构
- 不修改其他业务逻辑
- 不添加新功能

## Background & Context
- 经过代码审查，发现了 3 个数据一致性的严重问题
- 这些问题会导致数据库垃圾数据堆积和统计数据错误
- 需要在 @Transactional 事务中正确处理级联删除

## Functional Requirements
- **FR-1**: 删除帖子时，级联删除相关的评论和点赞记录
- **FR-2**: 删除评论时，级联删除所有子评论（回复）
- **FR-3**: 增强统计数据更新的安全性，确保不会出现负数

## Non-Functional Requirements
- **NFR-1**: 所有删除操作必须在同一事务中完成
- **NFR-2**: 修复后不影响现有功能的正常运行
- **NFR-3**: 保持良好的代码可读性

## Constraints
- **Technical**: Spring Boot 3 + JPA + MySQL
- **Business**: 必须保持数据一致性
- **Dependencies**: 不引入新的第三方库

## Assumptions
- Repository 层已有必要的查询方法
- 数据库关系正确配置
- 事务管理正常工作

## Acceptance Criteria

### AC-1: 删除帖子时正确级联清理
- **Given**: 存在一个有评论和点赞的帖子
- **When**: 管理员或作者删除该帖子
- **Then**: 帖子、所有相关评论、所有相关点赞都被删除
- **Verification**: `programmatic`
- **Notes**: 需要验证数据库中的数据确实被清理

### AC-2: 删除评论时正确级联删除子评论
- **Given**: 存在一个有子评论（回复）的父评论
- **When**: 管理员或作者删除该父评论
- **Then**: 父评论及其所有子评论都被删除
- **Verification**: `programmatic`
- **Notes**: 需要验证子评论确实被级联删除

### AC-3: 统计数据不会出现负数
- **Given**: 任何帖子或评论
- **When**: 更新或删除操作发生
- **Then**: postCount 和 commentCount 永远不会小于 0
- **Verification**: `programmatic`
- **Notes**: 需要添加安全检查，确保统计数 >= 0

## Open Questions
- [ ] 是否需要添加数据库外键约束？
