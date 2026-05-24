# 校园论坛私信功能实现计划

## [x] Task 1: 数据库表设计与SQL脚本
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 设计并创建私信会话表（`bbs_conversation`）和私信消息表（`bbs_message`）
  - 包含必要的字段和索引
  - 确保与现有表结构兼容
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - programmatic: SQL脚本执行成功，表结构完整
- **Notes**: 确保SQL脚本可重复执行（使用`IF NOT EXISTS`）

## [ ] Task 2: 后端模型类与Repository
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建`BbsConversation`和`BbsMessage`模型类
  - 创建对应的Repository接口
  - 添加必要的查询方法
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - programmatic: 后端编译通过
- **Notes**: 使用JPA注解，保持与现有模型风格一致

## [ ] Task 3: 后端Service层实现
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建`BbsMessageService`服务类
  - 实现会话管理功能（创建、获取会话列表）
  - 实现消息发送、获取历史消息功能
  - 实现互关权限验证逻辑
  - 实现未读消息计数和标记已读
- **Acceptance Criteria Addressed**: AC-4, AC-5, AC-6, AC-7
- **Test Requirements**:
  - programmatic: 后端编译通过
  - human-judgment: 权限控制逻辑正确
- **Notes**: 互关逻辑：检查双方是否互关；未互关时限制1条消息

## [ ] Task 4: 后端Controller层API
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 创建`BbsMessageController`控制器
  - 实现以下API端点：
    - `GET /api/bbs/message/conversations` - 获取会话列表
    - `GET /api/bbs/message/conversation/{userId}` - 获取/创建与某用户的会话
    - `GET /api/bbs/message/history/{conversationId}` - 获取消息历史
    - `POST /api/bbs/message/send` - 发送消息
    - `POST /api/bbs/message/read/{conversationId}` - 标记已读
    - `GET /api/bbs/message/unread-count` - 获取未读总数
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-4, AC-5, AC-7
- **Test Requirements**:
  - programmatic: 后端编译通过，API可调用
- **Notes**: 添加合适的认证注解，确保安全

## [ ] Task 5: 前端模型与API封装
- **Priority**: P1
- **Depends On**: Task 4
- **Description**: 
  - 前端创建`Conversation`和`Message`模型类
  - 在`HttpRequestUtil`中添加对应的API调用方法
  - 在`AppStore`中添加临时状态管理（如果需要）
- **Acceptance Criteria Addressed**: AC-2, AC-4, AC-5
- **Test Requirements**:
  - programmatic: 前端编译通过

## [ ] Task 6: 数据库菜单配置SQL
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 在`menu`表中添加"我的私信"菜单项
  - 菜单位置：个人中心下
  - 菜单ID：确保不与现有菜单冲突
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - programmatic: SQL执行成功

## [ ] Task 7: 前端个人中心菜单更新
- **Priority**: P1
- **Depends On**: Task 5, Task 6
- **Description**: 
  - 在`personal-center.fxml`中添加"我的私信"按钮
  - 在`PersonalCenterController`中添加对应的事件处理
  - 实现未读消息红点显示逻辑
  - 定期（如5秒）更新未读消息数
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - human-judgment: 菜单显示正确，红点更新正常

## [ ] Task 8: 会话列表页面（FXML + Controller）
- **Priority**: P1
- **Depends On**: Task 5
- **Description**: 
  - 创建`my-messages.fxml`视图文件
  - 创建`MyMessagesController`控制器
  - 实现会话列表展示（头像、昵称、最后消息）
  - 实现点击会话进入聊天页面
  - 实现未读消息提示
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - programmatic: 前端编译通过
  - human-judgment: 界面显示正常，交互流畅

## [ ] Task 9: 聊天页面（FXML + Controller）
- **Priority**: P0
- **Depends On**: Task 8
- **Description**: 
  - 创建`chat-view.fxml`视图文件
  - 创建`ChatViewController`控制器
  - 实现历史消息加载和显示
  - 实现文字消息发送
  - 实现图片消息发送（复用现有的文件上传）
  - 实现消息区分（发送方/接收方）
  - 实现权限限制提示
  - 进入聊天时自动标记消息为已读
- **Acceptance Criteria Addressed**: AC-5, AC-6, AC-7
- **Test Requirements**:
  - programmatic: 前端编译通过
  - human-judgment: 界面美观，消息发送接收正常

## [ ] Task 10: 用户主页私信功能集成
- **Priority**: P1
- **Depends On**: Task 9
- **Description**: 
  - 修改`UserHomeController`：
    - 根据是否已关注，显示/隐藏"私信"按钮
    - 实现"私信"按钮点击事件
    - 跳转到与该用户的聊天页面
  - 确保在MainFrameController中正确打开聊天标签页
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - programmatic: 前端编译通过
  - human-judgment: 按钮显示正确，点击跳转到正确的会话

## [ ] Task 11: CSS样式优化
- **Priority**: P2
- **Depends On**: Task 8, Task 9
- **Description**: 
  - 为私信相关界面添加美观的CSS样式
  - 会话列表样式、聊天界面样式
  - 消息气泡样式
  - 红点提示样式
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - human-judgment: 界面美观，符合整体风格

## [ ] Task 12: 综合测试与调试
- **Priority**: P0
- **Depends On**: Task 10, Task 11
- **Description**: 
  - 端到端测试整个流程
  - 测试互关权限限制
  - 测试未读消息计数和更新
  - 测试图片消息发送和显示
  - 修复发现的bug
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4, AC-5, AC-6, AC-7
- **Test Requirements**:
  - human-judgment: 所有功能正常，无明显bug
- **Notes**: 确保不影响现有功能
