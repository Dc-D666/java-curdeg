# 二级评论回复功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 创建数据库修改SQL脚本
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在bbs_comment表新增字段：reply_to_comment_id（被回复的评论ID）、reply_to_user_id（被回复的用户ID）、reply_to_user_nickname（被回复的用户昵称）
  - 创建SQL脚本文件
- **Acceptance Criteria Addressed**: [AC-5]
- **Test Requirements**:
  - `programmatic` TR-1.1: SQL脚本可以成功执行，表结构正确更新
  - `programmatic` TR-1.2: 新字段可以正确保存和读取数据
- **Notes**: 向后兼容，现有数据不受影响

## [x] Task 2: 更新后端BbsComment实体模型
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在BbsComment.java中添加新字段映射
  - 添加@Transient临时字段用于显示回复关系
- **Acceptance Criteria Addressed**: [AC-5]
- **Test Requirements**:
  - `programmatic` TR-2.1: 实体类可以正确映射新字段
  - `programmatic` TR-2.2: JPA可以正确保存和读取新字段
- **Notes**: 保持Lombok注解风格一致

## [x] Task 3: 修改后端BbsCommentService业务逻辑
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 修改createComment方法，支持回复二级评论时设置正确的parentId（指向一级父评论）
  - 保存被回复的评论ID、用户ID和用户昵称
  - 在填充评论信息时，填充回复关系的显示信息
- **Acceptance Criteria Addressed**: [AC-3, AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `programmatic` TR-3.1: 回复二级评论时，parentId正确设置为该二级评论的一级父评论ID
  - `programmatic` TR-3.2: reply_to_comment_id、reply_to_user_id、reply_to_user_nickname正确保存
  - `programmatic` TR-3.3: 敏感词过滤正常工作
  - `programmatic` TR-3.4: 禁言用户无法评论
- **Notes**: 需要判断被回复的评论是否有父评论，如果有则使用父评论的ID作为新评论的parentId

## [x] Task 4: 更新前端Comment模型
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在前端Comment.java模型中添加对应字段
  - 添加replyToUserNickname字段用于显示
- **Acceptance Criteria Addressed**: [AC-4]
- **Test Requirements**:
  - `programmatic` TR-4.1: 前端模型可以正确解析JSON中的新字段
  - `human-judgment` TR-4.2: 字段命名与后端一致
- **Notes**: 保持与现有模型风格一致

## [x] Task 5: 修改前端HttpRequestUtil
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 新增publishComment方法，支持传递更多参数（被回复评论信息）
  - 或修改现有方法以支持新参数
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `programmatic` TR-5.1: HTTP请求可以正确传递新参数
  - `programmatic` TR-5.2: 响应可以正确解析
- **Notes**: 向后兼容现有调用

## [x] Task 6: 修改前端PostDetailController
- **Priority**: P0
- **Depends On**: Task 5
- **Description**: 
  - 为二级评论添加"回复"按钮
  - 修改openReplyDialog方法，支持回复二级评论
  - 修改评论显示逻辑，显示"回复者 回复 被回复者"格式
  - 为二级评论也添加举报按钮
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-4]
- **Test Requirements**:
  - `human-judgment` TR-6.1: 二级评论旁显示"回复"按钮
  - `human-judgment` TR-6.2: 点击回复弹出正确的对话框
  - `human-judgment` TR-6.3: 显示"回复者 回复 被回复者"格式
  - `programmatic` TR-6.4: 举报按钮功能正常
- **Notes**: 保持UI风格一致

## [x] Task 7: 端到端测试
- **Priority**: P1
- **Depends On**: Task 3, Task 6
- **Description**: 
  - 完整测试整个回复流程
  - 测试边界情况
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `human-judgment` TR-7.1: 完整流程测试通过
  - `human-judgment` TR-7.2: 向后兼容测试通过（现有评论正常显示）
  - `programmatic` TR-7.3: 所有单元测试通过
- **Notes**: 测试多种场景
