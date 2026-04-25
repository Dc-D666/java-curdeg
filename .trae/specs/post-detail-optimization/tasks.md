
# 帖子详情页面优化 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 创建评论独立组件
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 CommentItem.fxml 布局文件
  - 创建 CommentItemController.java 控制器
  - 实现评论数据的绑定和基本渲染
  - 支持回复列表的嵌套展示
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `human-judgement` TR-1.1: 评论组件能够正常渲染，包含头像、作者、内容、时间、操作按钮
  - `human-judgement` TR-1.2: 评论的回复列表能够正确嵌套显示
- **Notes**: 提取原有 PostDetailController 中的评论渲染逻辑到新组件

## [x] Task 2: 重构页面顶部信息栏
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 post-detail.fxml 的顶部布局
  - 增加板块面包屑
  - 增加返回和刷新按钮
  - 增加统计数据展示（浏览量、点赞数、评论数、收藏数）
  - 优化审核状态标签样式
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - `human-judgement` TR-2.1: 顶部信息栏布局美观，信息完整
  - `human-judgement` TR-2.2: 审核状态标签颜色正确（橙/绿/红/黄）
- **Notes**: 保持与原有数据结构的兼容性

## [x] Task 3: 重构底部操作区
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 重新设计 post-detail.fxml 的底部布局
  - 高频操作按钮（点赞、收藏、分享、举报）直接显示
  - 创建"更多"下拉菜单，包含低频操作
  - 优化评论输入区，独立一行，增加添加图片按钮
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `human-judgement` TR-3.1: 底部按钮布局合理，不拥挤
  - `human-judgement` TR-3.2: 下拉菜单功能正常，包含所有低频操作
- **Notes**: 按钮增加图标增强识别度

## [x] Task 4: 优化内容区域
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 使用 Label 替代 TextArea 展示帖子内容
  - AI 总结区域默认收起
  - 增加评论数量显示
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-4.1: 内容展示更美观，AI总结默认收起
- **Notes**: 保持内容的换行和格式化显示

## [x] Task 5: 实现图片预览功能
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 创建 ImagePreview.fxml 和控制器
  - 实现帖子图片点击放大
  - 实现评论图片点击放大
- **Acceptance Criteria Addressed**: AC-7
- **Test Requirements**:
  - `human-judgement` TR-5.1: 点击图片能够正常弹出预览窗口
  - `human-judgement` TR-5.2: 预览窗口能够正确显示大图
- **Notes**: 支持多图查看的左右切换（可选）

## [x] Task 6: 实现评论点赞功能
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 检查后端是否已有评论点赞API
  - 如无，需要补充后端API
  - 在 CommentItemController 中实现点赞功能
  - 更新点赞状态和数量显示
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-6.1: 点击点赞按钮能够发送请求并更新状态
  - `human-judgement` TR-6.2: 已点赞状态视觉反馈正确
- **Notes**: 复用现有帖子点赞的代码模式

## [x] Task 7: 实现评论图片展示
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 修改 Comment 模型（如需要）
  - 在 CommentItem 中展示评论图片
  - 图片支持点击预览（复用Task5）
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `human-judgement` TR-7.1: 评论图片能够正常显示
  - `human-judgement` TR-7.2: 图片点击可预览
- **Notes**: 检查数据库中评论图片字段的存储格式

## [x] Task 8: 实现分享功能
- **Priority**: P2
- **Depends On**: Task 3
- **Description**: 
  - 创建分享对话框
  - 生成帖子链接
  - 实现一键复制链接功能
- **Acceptance Criteria Addressed**: AC-8
- **Test Requirements**:
  - `human-judgement` TR-8.1: 分享对话框正常显示和工作
  - `programmatic` TR-8.2: 复制链接功能正常
- **Notes**: 分享链接需要包含帖子ID

## [x] Task 9: 实现刷新功能
- **Priority**: P2
- **Depends On**: Task 2
- **Description**: 
  - 在刷新按钮点击时重新加载帖子详情
  - 重新加载评论列表
  - 显示加载状态
- **Acceptance Criteria Addressed**: AC-9
- **Test Requirements**:
  - `programmatic` TR-9.1: 点击刷新按钮重新触发数据加载
  - `human-judgement` TR-9.2: 加载状态显示友好
- **Notes**: 复用现有的 loadPostDetail 和 loadCommentList 方法

## [x] Task 10: 优化违规帖展示
- **Priority**: P2
- **Depends On**: None
- **Description**: 
  - 违规帖显示醒目标识
  - 展示违规原因和审核建议
  - 优化编辑重新提交的引导
- **Acceptance Criteria Addressed**: AC-10
- **Test Requirements**:
  - `human-judgement` TR-10.1: 违规帖展示清晰，引导编辑
- **Notes**: 确保只有作者和管理员能看到编辑按钮

## [x] Task 11: 实现评论分页或加载更多（可选）
- **Priority**: P2
- **Depends On**: Task 1
- **Description**: 
  - 检查后端是否支持分页
  - 实现评论的分页加载或"加载更多"
- **Acceptance Criteria Addressed**: (PRD中提到但未单独列AC，属于补充优化)
- **Test Requirements**:
  - `human-judgement` TR-11.1: 评论较多时能分批加载
- **Notes**: 建议先实现"加载更多"方案，简单实用

