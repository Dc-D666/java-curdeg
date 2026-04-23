# 图片上传功能 - 产品需求文档

## Overview
- **Summary**: 为学生交流社区新增图片上传功能，用户可以在发布帖子和评论时上传本地图片
- **Purpose**: 提升用户发布内容的体验，方便用户分享图片，而不仅仅是输入URL
- **Target Users**: 所有使用学生交流社区论坛功能的用户

## Goals
- 用户发布帖子时可以上传本地图片
- 用户发布评论时可以上传本地图片
- 保持现有通过URL输入图片的功能
- 图片上传后返回可访问的URL
- 不修改或删除已有的任何功能

## Non-Goals (Out of Scope)
- 图片编辑功能（裁剪、滤镜等）
- 批量上传多张图片（先实现单张）
- 图片压缩优化
- 图片水印功能

## Background & Context
- 项目已有帖子表 `bbs_post` 包含 `imageUrls` 字段（TEXT类型）存储图片URL
- 项目已有评论表 `bbs_comment` 目前没有图片字段
- 项目配置了 `attach.folder` 用于文件存储：`/teach-2025/`
- 项目已配置 Spring Multipart 支持，最大文件 200MB
- 已有 BaseService 提供文件存储相关基础设施

## Functional Requirements
- **FR-1**: 后端新增图片上传API，接收图片文件并返回访问URL
- **FR-2**: 发布帖子页面新增图片上传按钮和预览功能
- **FR-3**: 为评论表添加图片字段，支持评论图片
- **FR-4**: 评论发布页面新增图片上传功能
- **FR-5**: 帖子详情页正确显示帖子和评论的图片

## Non-Functional Requirements
- **NFR-1**: 图片上传大小限制：最大 10MB
- **NFR-2**: 支持的图片格式：JPG, JPEG, PNG, GIF
- **NFR-3**: 图片上传响应时间 < 3秒
- **NFR-4**: 图片URL可通过HTTP访问

## Constraints
- **Technical**: 必须使用现有的文件存储机制（attach.folder）
- **Business**: 不能修改或删除已有功能
- **Dependencies**: 依赖现有 Multipart 配置和文件存储基础设施

## Assumptions
- 服务器文件存储目录 `/teach-2025/` 存在且有写权限
- 静态资源访问已配置，上传的文件可通过URL访问
- 现有URL输入图片功能保持不变

## Acceptance Criteria

### AC-1: 图片上传API
- **Given**: 用户选择本地图片文件
- **When**: 调用图片上传API
- **Then**: 图片保存到服务器，返回可访问的URL
- **Verification**: `programmatic`
- **Notes**: 返回格式示例：`{"url": "/uploads/20260410/xxx.jpg"}`

### AC-2: 帖子图片上传
- **Given**: 用户在发布帖子页面
- **When**: 用户点击上传图片按钮并选择图片
- **Then**: 图片上传成功，图片URL自动填入图片URL字段
- **Verification**: `human-judgment`
- **Notes**: 保持现有的URL输入框功能

### AC-3: 评论图片字段
- **Given**: 数据库表 `bbs_comment`
- **When**: 执行SQL脚本添加图片字段
- **Then**: 表结构包含 `image_urls` 字段
- **Verification**: `programmatic`
- **Notes**: 字段类型为 TEXT，可为空

### AC-4: 评论图片上传
- **Given**: 用户在帖子详情页
- **When**: 用户发布评论时上传图片
- **Then**: 图片URL保存到评论中
- **Verification**: `human-judgment`
- **Notes**: 评论可以包含图片

### AC-5: 图片显示
- **Given**: 帖子或评论包含图片
- **When**: 用户查看帖子详情
- **Then**: 图片正确显示在页面上
- **Verification**: `human-judgment`
- **Notes**: 支持多张图片显示

### AC-6: 不破坏现有功能
- **Given**: 所有已有的功能
- **When**: 图片上传功能添加后
- **Then**: 所有现有功能继续正常工作
- **Verification**: `human-judgment`
- **Notes**: 重点验证帖子发布、评论发布、URL输入图片等功能

## Open Questions
- [ ] 无
