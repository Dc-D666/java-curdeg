
# AI内容审核功能 - 前端实现 The Implementation Plan

## [x] Task 1: 更新Post模型添加审核状态字段
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 查看现有的Post模型类
  - 添加moderationStatus字段及其getter/setter
  - 确保JSON反序列化能够正确处理新字段
- **Acceptance Criteria Addressed**: AC1
- **Test Requirements**:
  - `human-judgement`: Post类新增字段存在且正确
  - `programmatic`: 能够正确解析包含moderationStatus的JSON数据
- **Notes**: moderationStatus可能的值有: pending(审核中), pass(通过), reject(违规), manual(待人工)

## [x] Task 2: 实现帖子列表中的审核状态显示
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 查看PostListController
  - 添加审核状态列的显示逻辑
  - 实现不同状态的颜色映射
  - 更新post-list.fxml
- **Acceptance Criteria Addressed**: AC2
- **Test Requirements**:
  - `human-judgement`: 不同审核状态显示对应颜色
  - `human-judgement`: 状态文本显示正确
- **Notes**: 审核中/待人工审核-橙色，通过-绿色，违规-红色

## [x] Task 3: 在帖子详情中显示审核状态
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 查看PostDetailController
  - 在详情页面添加审核状态显示
  - 更新post-detail.fxml
- **Acceptance Criteria Addressed**: AC3
- **Test Requirements**:
  - `human-judgement`: 审核状态标签正确显示在帖子详情页面
- **Notes**: 与列表中的颜色保持一致

## [ ] Task 4: 创建管理员审核页面UI
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 参考AdminReportController和其FXML文件
  - 创建新的AdminModerationController
  - 创建对应的admin-moderation.fxml文件
  - 设计待审核帖子列表UI
- **Acceptance Criteria Addressed**: AC4
- **Test Requirements**:
  - `human-judgement`: 管理员审核页面UI布局合理
- **Notes**: 包括列表显示、审核操作按钮等

## [ ] Task 5: 实现管理员审核操作功能
- **Priority**: P1
- **Depends On**: Task 4
- **Description**: 
  - 实现获取待审核帖子列表的API调用
  - 实现审核通过/拒绝的API调用
  - 添加操作成功/失败的反馈
- **Acceptance Criteria Addressed**: AC5
- **Test Requirements**:
  - `programmatic`: 审核操作后列表正确更新
  - `human-judgement`: 用户操作反馈清晰

## [ ] Task 6: 集成管理员审核页面到主菜单
- **Priority**: P2
- **Depends On**: Task 4, Task 5
- **Description**: 
  - 查看MenuController和main-frame.fxml
  - 添加管理员审核入口（仅对管理员可见）
- **Acceptance Criteria Addressed**: AC4
- **Test Requirements**:
  - `human-judgement`: 管理员能在主菜单找到审核入口
  - `human-judgement`: 普通用户看不到审核入口

## [ ] Task 7: 更新我的帖子页面显示审核状态
- **Priority**: P2
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 查看MyPostsController
  - 在我的帖子列表中也显示审核状态
- **Acceptance Criteria Addressed**: AC2
- **Test Requirements**:
  - `human-judgement`: 我的帖子列表正确显示审核状态
