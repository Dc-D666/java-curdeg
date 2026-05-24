# 严重 Bug 修复 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 修复删除帖子时的级联清理
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsPostService.deletePost() 中添加级联删除逻辑
  - 删除该帖子的所有评论
  - 删除该帖子的所有点赞记录
  - 确保所有操作在同一 @Transactional 事务中完成
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `programmatic` TR-1.1: 删除帖子后，相关评论也被删除
  - `programmatic` TR-1.2: 删除帖子后，相关点赞记录也被删除
  - `programmatic` TR-1.3: 所有操作在同一事务中完成
- **Notes**: 需要先检查 Repository 是否有必要的查询方法

## [ ] Task 2: 修复删除评论时的级联删除子评论
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsCommentService.deleteComment() 中添加级联删除逻辑
  - 删除该评论的所有子评论（递归）
  - 同时更新相关统计数据
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `programmatic` TR-2.1: 删除父评论后，子评论也被删除
  - `programmatic` TR-2.2: 删除评论时正确更新帖子的 commentCount
  - `programmatic` TR-2.3: 所有操作在同一事务中完成
- **Notes**: 需要递归删除所有层级的子评论

## [ ] Task 3: 增强统计数据的安全性
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 在 BbsPostService 和 BbsCommentService 中增强统计数检查
  - 确保 postCount 和 commentCount 永远不会小于 0
  - 使用 Math.max(0, count - 1) 来确保安全
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `programmatic` TR-3.1: 统计数更新时不会出现负数
  - `programmatic` TR-3.2: 即使在边界情况下也能正常工作
- **Notes**: 虽然已有 count > 0 检查，但再添加一层防护
