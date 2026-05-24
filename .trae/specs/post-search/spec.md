# 帖子检索功能 - 产品需求文档

## Overview
- **Summary**: 创建一个独立的"帖子检索"页面，将现有的PostList中的搜索功能扩展并迁移到新页面，提供标题检索、全文检索、AI检索三种搜索模式，并保持与现有UI风格一致。
- **Purpose**: 为用户提供更强大、更智能的帖子搜索体验，分离搜索功能与浏览功能，使界面更清晰。
- **Target Users**: 所有校园论坛用户，包括学生、管理员等。

## Goals
- 将现有的搜索功能从PostList迁移到独立的"帖子检索"页面
- 提供标题检索、全文检索、AI检索三种搜索模式
- 设计直观、易用的搜索界面
- 保持与现有UI风格的一致性

## Non-Goals (Out of Scope)
- 不实现后端AI检索的逻辑（本次仅设计UI）
- 不修改PostList页面的现有功能
- 不实现高级搜索功能（如时间范围筛选、作者筛选等）

## Background & Context
- 现有系统在PostList页面已有简单的关键词搜索功能
- 项目使用JavaFX + FXML作为前端技术栈
- 数据库已包含bbs_post表的全文索引（根据SQL脚本09_add_post_fulltext_index.sql）
- 新菜单项"帖子检索"已在menu_info表中配置（id=202）

## Functional Requirements
- **FR-1**: 创建独立的"帖子检索"页面（post-search.fxml + PostSearchController.java）
- **FR-2**: 实现搜索模式切换（标题检索、全文检索、AI检索）
- **FR-3**: 实现搜索输入框和搜索按钮
- **FR-4**: 实现搜索结果展示表格（与PostList类似）
- **FR-5**: 实现搜索结果分页功能
- **FR-6**: 支持点击搜索结果打开帖子详情
- **FR-7**: 实现搜索历史记录（可选扩展）

## Non-Functional Requirements
- **NFR-1**: 界面响应时间 < 200ms
- **NFR-2**: 保持与现有UI风格一致
- **NFR-3**: 代码结构清晰，易于维护

## Constraints
- **Technical**: 使用JavaFX + FXML，不引入新的UI框架
- **Business**: 保持项目架构稳定
- **Dependencies**: 依赖现有的HttpRequestUtil和后端API

## Assumptions
- 后端API支持多种搜索模式（搜索类型参数）
- 数据库已有全文索引支持
- 用户已熟悉现有PostList的操作方式

## Acceptance Criteria

### AC-1: 页面布局完整
- **Given**: 用户进入"帖子检索"页面
- **When**: 页面加载完成
- **Then**: 用户能看到搜索模式选择、搜索输入框、搜索按钮、结果表格、分页控件
- **Verification**: `human-judgment`

### AC-2: 搜索模式切换
- **Given**: 用户在"帖子检索"页面
- **When**: 用户切换搜索模式（标题/全文/AI）
- **Then**: 界面正确显示当前选中的搜索模式，且搜索时使用对应的模式
- **Verification**: `human-judgment`

### AC-3: 搜索功能可用
- **Given**: 用户在"帖子检索"页面
- **When**: 用户输入关键词并点击搜索或按回车
- **Then**: 系统执行搜索并显示结果
- **Verification**: `programmatic`

### AC-4: 搜索结果展示
- **Given**: 搜索已执行
- **When**: 结果返回
- **Then**: 表格正确展示搜索结果（标题、作者、时间、点赞、评论等）
- **Verification**: `human-judgment`

### AC-5: 分页功能正常
- **Given**: 搜索结果有多页
- **When**: 用户点击上一页/下一页或切换每页条数
- **Then**: 结果正确更新
- **Verification**: `programmatic`

### AC-6: 打开帖子详情
- **Given**: 搜索结果表格中有帖子
- **When**: 用户双击某条帖子
- **Then**: 打开该帖子的详情页面
- **Verification**: `human-judgment`

### AC-7: UI风格一致
- **Given**: "帖子检索"页面
- **When**: 与PostList等现有页面对比
- **Then**: 风格保持一致
- **Verification**: `human-judgment`

## Open Questions
- [ ] AI检索的具体交互方式（是否需要显示AI生成的摘要或推荐理由）
- [ ] 是否需要保存搜索历史记录
- [ ] 是否需要添加更多筛选条件（板块、时间范围等）
