# 双击举报查看原文功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 在后端添加获取单个评论详情的接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsCommentController 中添加 @GetMapping("/{id}") 接口
  - 在 BbsCommentService 中添加 getCommentDetail 方法
  - 该方法返回单个评论的完整信息（包含作者信息）
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 接口能正确返回评论详情
  - `human-judgement` TR-1.2: 接口包含作者昵称等信息
- **Notes**: 参考 getPostDetail 的实现

## [x] Task 2: 在 HttpRequestUtil 中添加获取评论详情的方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加 getCommentDetail(Long commentId) 方法
  - 调用后端 /api/bbs/comment/{id} 接口
  - 参考 getPostDetail 方法的实现
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-2.1: 方法能正确请求后端接口
  - `human-judgement` TR-2.2: 方法返回 Comment 对象
- **Notes**: 参考 getPostDetail 方法

## [x] Task 3: 在 AdminReportController 中添加双击事件监听
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在 initialize() 方法中为 reportTableView 添加双击事件监听
  - 双击时获取选中的举报记录
  - 根据 targetType 判断是帖子还是评论
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 双击举报记录能触发事件
  - `human-judgement` TR-3.2: 能正确获取举报记录信息
- **Notes**: 参考 PostListController 中双击打开帖子详情的实现

## [x] Task 4: 实现查看帖子详情弹窗
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 创建 openPostDetailDialog(Report report) 方法
  - 调用 getPostDetail() 获取帖子详情
  - 使用 Dialog 或 Alert 显示帖子信息
  - 显示标题、内容、作者、发布时间等
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `human-judgement` TR-4.1: 弹窗能正确显示帖子详情
  - `human-judgement` TR-4.2: 弹窗样式美观
- **Notes**: 使用 TextArea 显示内容，支持滚动

## [x] Task 5: 实现查看评论详情弹窗
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 创建 openCommentDetailDialog(Report report) 方法
  - 调用 getCommentDetail() 获取评论详情
  - 使用 Dialog 或 Alert 显示评论信息
  - 显示内容、作者、发布时间等
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-5.1: 弹窗能正确显示评论详情
  - `human-judgement` TR-5.2: 弹窗样式美观
- **Notes**: 与帖子详情弹窗保持一致的风格
