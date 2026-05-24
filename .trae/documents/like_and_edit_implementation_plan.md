# 点赞和编辑功能实现计划

## [x] 任务 1: 实现前端编辑功能
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 HttpRequestUtil 中添加 updatePost() 方法调用后端编辑接口
  - 在 PostDetailController 中实现编辑按钮逻辑
  - 创建编辑对话框/界面让用户修改帖子标题和内容
  - 编辑成功后刷新帖子详情
- **Success Criteria**:
  - 用户可以点击编辑按钮打开编辑界面
  - 只有作者和管理员可以编辑帖子
  - 编辑成功后帖子内容更新
- **Test Requirements**:
  - `programmatic` TR-1.1: 点击编辑按钮弹出编辑对话框
  - `programmatic` TR-1.2: 编辑内容验证正确（标题5-100字符，内容至少10字符）
  - `human-judgement` TR-1.3: 编辑界面清晰易用

## [x] 任务 2: 实现前端点赞功能（简化版）
- **Priority**: P1
- **Depends On**: None
- **Description**:
  - 在 HttpRequestUtil 中添加一个简单的 toggleLike() 方法（暂时只增加计数）
  - 在 PostDetailController 中实现点赞按钮逻辑
  - 更新点赞按钮文本显示当前点赞数
  - 由于后端没有完整的点赞接口，此为简化版实现
- **Success Criteria**:
  - 用户可以点击点赞按钮
  - 点赞数显示增加
  - 按钮状态有视觉反馈
- **Test Requirements**:
  - `programmatic` TR-2.1: 点击点赞按钮后点赞数增加
  - `human-judgement` TR-2.2: 点赞按钮有状态变化反馈
- **Notes**: 这是一个临时简化方案，完整的点赞功能需要后端新增点赞表和接口

## [x] 任务 3: 更新 post-detail.fxml UI
- **Priority**: P1
- **Depends On**: None
- **Description**:
  - 优化点赞按钮显示，显示当前点赞数
  - 确保编辑/删除等按钮正确显示
  - 改进整体UI布局
- **Success Criteria**:
  - 点赞按钮显示"点赞 (X)"格式
  - 所有按钮按权限正确显示
- **Test Requirements**:
  - `human-judgement` TR-3.1: UI布局美观，信息清晰
