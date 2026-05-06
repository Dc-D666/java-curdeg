# 用户主页 - 实施计划

## [ ] 任务1: 创建用户主页数据模型
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在前端创建用户主页需要的数据模型（UserHomeProfile）
  - 包含用户基本信息和统计数据
  - 复用现有的User和Post模型
- **Acceptance Criteria Addressed**: AC1, AC3
- **Test Requirements**:
  - `programmatic`: 模型能正确序列化和反序列化JSON数据
- **Notes**: 参考现有User和Post模型的设计

## [ ] 任务2: 实现用户主页前端UI (FXML)
- **Priority**: P0
- **Depends On**: 任务1
- **Description**: 
  - 创建user-homepage.fxml文件
  - 设计页面布局：顶部用户信息区，中部发帖列表区，底部分页区
  - 保持与现有UI风格一致（蓝色主题#4a90e2）
  - 添加关注按钮和私信按钮
- **Acceptance Criteria Addressed**: AC1, AC3, AC5, AC7
- **Test Requirements**:
  - `human-judgment`: UI布局合理，风格一致
- **Notes**: 参考personal-center.fxml和my-posts.fxml的布局风格

## [ ] 任务3: 实现用户主页控制器
- **Priority**: P0
- **Depends On**: 任务2
- **Description**: 
  - 创建UserHomepageController.java
  - 实现加载用户信息功能
  - 实现加载用户发帖列表功能
  - 实现关注/取消关注功能
  - 实现分页功能
  - 实现私信按钮点击事件（显示"功能开发中"）
- **Acceptance Criteria Addressed**: AC1, AC2, AC3, AC6, AC7
- **Test Requirements**:
  - `programmatic`: 控制器方法能正确调用API并处理响应
  - `human-judgment`: 交互流程流畅，错误提示友好
- **Notes**: 参考PostDetailController和PersonalCenterController的实现

## [ ] 任务4: 扩展HttpRequestUtil工具类
- **Priority**: P0
- **Depends On**: 任务1
- **Description**: 
  - 添加获取用户公开信息的方法
  - 添加获取用户发帖列表的方法
  - 确保现有关注/取消关注方法可用
- **Acceptance Criteria Addressed**: AC1, AC2, AC3
- **Test Requirements**:
  - `programmatic`: API调用能正常工作并返回正确数据
- **Notes**: 检查后端是否已有相应API

## [ ] 任务5: 修改帖子详情页 - 添加头像点击事件
- **Priority**: P1
- **Depends On**: 任务3
- **Description**: 
  - 在PostDetailController中添加头像点击事件处理
  - 点击头像后在新标签页打开用户主页
  - 确保评论中的头像也能点击跳转
- **Acceptance Criteria Addressed**: AC4
- **Test Requirements**:
  - `programmatic`: 点击头像能正确跳转到相应用户主页
- **Notes**: 参考MainFrameController的标签页管理方式

## [ ] 任务6: 修改评论项 - 添加头像点击事件
- **Priority**: P1
- **Depends On**: 任务5
- **Description**: 
  - 在CommentItemController中添加头像点击事件
  - 点击评论者头像跳转到用户主页
- **Acceptance Criteria Addressed**: AC4
- **Test Requirements**:
  - `programmatic`: 点击评论头像能正确跳转
- **Notes**: 可能需要传递回调或使用AppStore

## [ ] 任务7: 集成到主框架 - 添加用户主页标签页管理
- **Priority**: P1
- **Depends On**: 任务3
- **Description**: 
  - 在MainFrameController中添加打开用户主页的方法
  - 确保用户主页可以在标签页中正确显示和关闭
- **Acceptance Criteria Addressed**: AC4
- **Test Requirements**:
  - `programmatic`: 能正确打开和关闭用户主页标签页
- **Notes**: 参考帖子详情页的标签页管理

## [ ] 任务8: 测试和验证
- **Priority**: P0
- **Depends On**: 任务5, 6, 7
- **Description**: 
  - 全面测试用户主页功能
  - 验证所有接受标准
  - 修复发现的问题
- **Acceptance Criteria Addressed**: AC1, AC2, AC3, AC4, AC5, AC6, AC7
- **Test Requirements**:
  - `programmatic`: 所有功能正常工作
  - `human-judgment`: 整体用户体验良好
- **Notes**: 进行端到端测试
