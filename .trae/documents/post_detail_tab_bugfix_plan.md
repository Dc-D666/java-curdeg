# 帖子详情 Tab 缓存问题修复计划

## 问题描述
在帖子列表中打开 A 帖子进入帖子详情页面，切换到帖子列表中打开 B 帖子进入的详情页面仍然为 A 帖子的详情。

## 问题原因
1. `PostListController.openPostDetail()` 方法中，每次都使用固定的 name "post-detail"
2. `MainFrameController.changeContentWithScene()` 方法会检查 tabMap 中是否已有该 name 的 tab
3. 如果 tab 已存在，直接选择已有 tab，不会重新加载新数据
4. 导致所有帖子详情共用同一个 tab 缓存

## 修复方案

## [x] Task 1: 修改 PostListController.openPostDetail() 方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `PostListController.openPostDetail()` 方法
  - 使用唯一的 name（如 "post-detail-" + postId）而不是固定的 "post-detail"
  - 这样每个帖子都有自己独立的 tab
- **Success Criteria**:
  - 每个帖子打开时都创建新的独立 tab
  - 不同帖子不会相互干扰
- **Test Requirements**:
  - `programmatic` TR-1.1: 代码无编译错误
  - `human-judgement` TR-1.2: 打开 A 帖子再打开 B 帖子，显示 B 帖子内容
