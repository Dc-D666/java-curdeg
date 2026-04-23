
# AI内容审核功能 - Product Requirements Document

## Overview
- **Summary**: 为校园论坛系统添加AI内容审核功能，包括自动审核帖子内容、显示审核状态、通知用户审核结果以及管理员人工审核。
- **Purpose**: 提升论坛内容质量，快速识别和处理违规内容，同时减少人工审核成本。
- **Target Users**: 普通用户（发帖者）、管理员（审核者）

## Goals
1. 用户发帖后，帖子显示"审核中"状态（仅发帖者和管理员可见）
2. 后端自动调用AI API进行内容审核
3. 审核结果包括：pass（审核通过）、reject（内容违规）、manual（需人工审核）
4. 不同审核状态显示不同颜色的状态标识
5. 审核中/违规/待人工审核的帖子仅发布者本人可见
6. 审核结果通知用户
7. 需人工审核的帖子推送给管理员

## Non-Goals (Out of Scope)
- 评论内容的AI审核（本期仅实现帖子审核）
- 图片内容的深度审核（仅标注需人工复核）
- 用户申诉流程

## Background & Context
- 现有系统已有敏感词过滤功能
- 使用 Qwen3.5-Flash 模型进行AI审核
- API地址：https://xplt.sdu.edu.cn:4000/v1/chat/completions
- 已有用户通知系统和管理员角色系统

## Functional Requirements
- **FR1**: 数据库扩展 - 添加审核状态、审核结果、审核时间等字段
- **FR2**: AI审核服务 - 封装AI API调用，将Python代码移植为Java实现
- **FR3**: 发帖流程改造 - 发帖后先标记为审核中，再异步调用AI审核
- **FR4**: 帖子列表可见性控制 - 根据审核状态和用户角色控制可见性
- **FR5**: 审核状态UI显示 - 前端显示不同颜色的审核状态标签
- **FR6**: 审核结果通知 - 审核完成后通知用户
- **FR7**: 管理员审核功能 - 管理员查看和处理待人工审核的帖子
- **FR8**: 评论审核预留（可选）

## Non-Functional Requirements
- **NFR1**: 审核响应时间 &lt; 5秒
- **NFR2**: 审核API调用失败有降级方案（自动转为人工审核）
- **NFR3**: 审核状态更新实时性 &lt; 10秒
- **NFR4**: 系统稳定性 - 审核服务异常不影响发帖功能

## Constraints
- **Technical**: 必须使用Java 21 + Spring Boot 3.2.5
- **Business**: AI API密钥必须安全存储，不可硬编码
- **Dependencies**: 需要添加HTTP客户端依赖（如OkHttp或Spring WebClient）

## Assumptions
- AI API返回的JSON格式与example.py中描述一致
- 用户已登录才能发帖
- 管理员角色为ROLE_ADMIN或ROLE_SUPER

## Acceptance Criteria

### AC1: 发帖后显示审核中状态
- **Given**: 用户已登录并进入发帖页面
- **When**: 用户成功发帖并返回帖子广场
- **Then**: 新帖子在列表中显示橙色"审核中"标签，且仅发帖者和管理员可见
- **Verification**: programmatic
- **Notes**: 需验证数据库中审核状态字段已更新

### AC2: AI自动审核正常流程
- **Given**: 有新帖子处于审核中状态
- **When**: AI审核服务被调用并返回pass结果
- **Then**: 帖子状态更新为"审核通过"，变为全员可见，通知用户审核通过
- **Verification**: programmatic

### AC3: AI审核发现违规内容
- **Given**: 有新帖子处于审核中状态
- **When**: AI审核服务返回reject结果
- **Then**: 帖子状态更新为"内容违规"，仅作者可见，通知用户内容违规
- **Verification**: programmatic

### AC4: AI审核需人工复核
- **Given**: 有新帖子处于审核中状态
- **When**: AI审核服务返回manual结果或API调用失败
- **Then**: 帖子状态更新为"待人工审核"，仅作者和管理员可见，通知用户需等待，同时通知管理员
- **Verification**: programmatic

### AC5: 审核状态UI显示正确
- **Given**: 用户查看帖子列表
- **When**: 存在不同审核状态的帖子
- **Then**: 
  - 审核中：橙色文字
  - 审核通过：绿色文字
  - 内容违规：红色文字
  - 待人工审核：橙色文字
- **Verification**: human-judgment

### AC6: 管理员人工审核功能
- **Given**: 管理员登录系统
- **When**: 管理员查看待审核帖子列表并进行审核操作
- **Then**: 帖子状态更新，相关用户收到通知
- **Verification**: programmatic

## Open Questions
- [ ] 是否需要为评论也添加AI审核？（本期暂不做）
- [ ] 审核历史记录是否需要持久化？（建议是）
- [ ] 敏感词过滤和AI审核的优先级？（建议同时保留，AI审核作为补充）
