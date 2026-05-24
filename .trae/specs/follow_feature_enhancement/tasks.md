# 用户关注功能增强 - 实现计划

## [x] Task 1: 后端 - 关注时发送新增粉丝通知
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `BbsFollowService.toggleFollow()` 方法
  - 当用户关注另一用户时，为被关注用户创建一条类型为5的通知
  - 通知标题："新增粉丝通知"
  - 通知内容："用户【用户名】关注了你"
- **Acceptance Criteria Addressed**: [AC-2, AC-6]
- **Test Requirements**:
  - `programmatic` TR-1.1: 关注用户时，被关注用户的通知表中新增一条type=5的记录
  - `programmatic` TR-1.2: 通知标题和内容格式正确
  - `programmatic` TR-1.3: 取消关注时不发送通知
  - `human-judgement` TR-1.4: 现有关注/取消关注功能继续正常工作
- **Notes**: 需要注入 `BbsNotificationRepository` 和 `UserRepository`

## [ ] Task 2: 后端 - 关注用户发帖时发送通知
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `BbsPostService.createPost()` 方法中的通知发送逻辑
  - 将现有的通知类型从3改为6
  - 通知标题从"用户发帖通知"改为"关注用户发帖通知"
  - 通知内容从"用户 X 发布了新帖子：Y"改为"用户【X】发布帖子"
- **Acceptance Criteria Addressed**: [AC-3, AC-6]
- **Test Requirements**:
  - `programmatic` TR-2.1: 用户发布帖子时，所有粉丝收到type=6的通知
  - `programmatic` TR-2.2: 通知标题和内容格式正确
  - `programmatic` TR-2.3: 没有粉丝时不发送通知
  - `human-judgement` TR-2.4: 现有发帖功能继续正常工作
- **Notes**: 保持try-catch异常处理，确保通知失败不影响发帖

## [ ] Task 3: 前端 - 通知类型支持新增类型
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `MyNotificationController.initialize()` 中的类型映射
  - 添加 case 5: "新增粉丝通知"
  - 添加 case 6: "关注用户发帖通知"
  - 修改 `typeComboBox` 选项列表，添加"新增粉丝通知"和"关注用户发帖通知"
  - 更新类型选择逻辑
- **Acceptance Criteria Addressed**: [AC-4, AC-5, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 通知列表中类型列正确显示新类型名称
  - `human-judgement` TR-3.2: 筛选下拉框包含新类型选项
  - `human-judgement` TR-3.3: 筛选功能对新类型正常工作
  - `human-judgement` TR-3.4: 现有通知类型继续正常显示
- **Notes**: 注意保持现有类型的映射不变

## [ ] Task 4: 前端 - 关注状态同步机制
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 创建一个关注状态管理器类 `FollowStateManager`（单例模式）
  - 存储用户ID到关注状态的映射
  - 提供监听器机制，当关注状态改变时通知所有注册的监听器
  - 修改 `PostDetailController`，使用状态管理器并注册监听器
  - 修改 `PostListController`，使用状态管理器并注册监听器
  - 关注状态改变时，更新状态管理器并触发监听器
- **Acceptance Criteria Addressed**: [AC-1, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-4.1: 同一页面中多处同一用户的关注按钮同步变化
  - `human-judgement` TR-4.2: 不同页面中同一用户的关注按钮同步变化
  - `human-judgement` TR-4.3: 关注状态变化后刷新页面，状态保持正确
  - `human-judgement` TR-4.4: 现有关注功能继续正常工作
- **Notes**: 需要考虑页面关闭时注销监听器，避免内存泄漏

## [ ] Task 5: 集成测试和验证
- **Priority**: P0
- **Depends On**: [Task 1, Task 2, Task 3, Task 4]
- **Description**: 
  - 执行所有测试用例
  - 验证所有现有功能不受影响
  - 验证新增功能正常工作
  - 修复发现的问题
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-5.1: 所有功能按验收标准正常工作
  - `human-judgement` TR-5.2: 没有引入新的bug
  - `human-judgement` TR-5.3: 用户体验良好
- **Notes**: 进行全面的回归测试
