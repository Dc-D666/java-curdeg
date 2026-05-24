# 帖子详情页滚动 Bug 修复 - Product Requirement Document

## Overview
- **Summary**：修复帖子详情页面在内容较长时点击任意位置导致评论区闪现底部的 Bug
- **Purpose**：解决用户在浏览长帖子或多评论帖子时遇到的异常滚动问题，提升用户体验
- **Target Users**：所有使用校园论坛系统的用户

## Goals
1. 完全消除点击页面任意位置导致评论区异常滚动的问题
2. 保持原有功能完整性，不引入新的 Bug
3. 确保在各种内容长度情况下都能正常工作

## Non-Goals (Out of Scope)
- 不重构整个页面布局架构
- 不修改后端 API
- 不添加新的功能特性

## Background & Context
### 问题描述
在帖子详情页面，当评论数量较多或帖子正文足够长（超出窗口高度）时，点击页面任意位置会导致评论区闪现到底部，超出可见范围。这个问题严重影响用户浏览体验。

### 代码分析
通过代码审查，在 `PostDetailController.java` 的第 156-173 行发现了可疑的事件处理代码：

```java
mainScrollPane.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
    Object target = event.getTarget();
    if (target == mainScrollPane || target == contentVBox || target == commentVBox) {
        event.consume();
    }
});

contentVBox.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
    if (event.getTarget() == contentVBox) {
        event.consume();
    }
});

commentVBox.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
    if (event.getTarget() == commentVBox) {
        event.consume();
    }
});
```

### 推测的问题原因
1. 事件过滤器可能是为了修复之前的问题而添加的，但实际上它干扰了 ScrollPane 的正常布局行为
2. `event.consume()` 可能触发不必要的布局重新计算
3. ScrollPane 布局与点击事件处理产生竞争，当内容较长时尤为明显

## Functional Requirements
- **FR-1**：移除或优化事件处理代码
- **FR-2**：确保点击页面任意位置不会触发异常滚动
- **FR-3**：保持所有原有交互功能正常（点赞、评论、收藏等）

## Non-Functional Requirements
- **NFR-1**：修复后的代码保持与现有风格一致
- **NFR-2**：确保性能不下降
- **NFR-3**：修复必须经过充分测试

## Constraints
- **Technical**：仅修改前端 JavaFX 代码
- **Business**：必须向后兼容，不能破坏现有功能
- **Dependencies**：不依赖任何新的第三方库

## Assumptions
1. 问题确实是由上述事件处理代码引起的
2. 移除该代码不会导致之前修复的问题重现
3. 可以通过充分测试验证修复效果

## Acceptance Criteria

### AC-1：消除异常滚动问题
- **Given**：打开一个评论数量较多或内容较长的帖子
- **When**：用户点击页面任意位置
- **Then**：评论区不会闪现到底部，页面保持稳定
- **Verification**：programmatic（通过功能测试）

### AC-2：保持原有功能正常
- **Given**：帖子详情页面正常显示
- **When**：用户进行各种操作（点赞、评论、收藏、返回等）
- **Then**：所有功能都能正常工作
- **Verification**：programmatic（通过功能测试）

### AC-3：在各种内容长度情况下都正常
- **Given**：打开不同长度内容的帖子（短、中、长）
- **When**：用户正常浏览和交互
- **Then**：所有页面都不会出现异常滚动
- **Verification**：programmatic（通过功能测试）

## Open Questions
- [ ] 该事件处理代码最初是为了修复什么问题而添加的？
- [ ] 移除该代码是否会导致其他问题重现？
