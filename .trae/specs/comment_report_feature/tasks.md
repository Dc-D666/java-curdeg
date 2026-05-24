# 评论举报功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 在评论底部添加举报按钮
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 `PostDetailController.java` 的 `loadCommentList()` 方法中，为每个评论添加举报按钮
  - 举报按钮放在"回复"按钮旁边
  - 根据用户权限控制按钮的可见性（登录、未禁言、非作者）
- **Acceptance Criteria Addressed**: [AC-1, AC-4]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 每个评论都有举报按钮
  - `human-judgement` TR-1.2: 举报按钮样式与回复按钮一致（小字体）
  - `human-judgement` TR-1.3: 未登录用户看不到举报按钮
  - `human-judgement` TR-1.4: 禁言用户看不到举报按钮
  - `human-judgement` TR-1.5: 评论作者看不到自己评论的举报按钮
- **Notes**: 参考帖子举报按钮的权限控制逻辑

## [ ] Task 2: 实现评论举报对话框方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建 `openCommentReportDialog(Comment comment)` 方法
  - 复用帖子举报对话框的实现逻辑
  - 对话框标题显示评论内容预览
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `human-judgement` TR-2.1: 点击举报按钮打开对话框
  - `human-judgement` TR-2.2: 对话框标题包含评论内容
  - `human-judgement` TR-2.3: 对话框包含原因输入框
- **Notes**: 参考 `openReportDialog()` 方法

## [ ] Task 3: 实现评论举报提交逻辑
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 调用 `HttpRequestUtil.submitReport(2, commentId, reason)` 提交评论举报
  - targetType 设置为 2（表示评论）
  - 显示成功或失败提示
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 提交成功显示提示
  - `human-judgement` TR-3.2: 提交失败显示错误提示
  - `human-judgement` TR-3.3: 空原因验证
- **Notes**: targetType=1 是帖子，targetType=2 是评论
