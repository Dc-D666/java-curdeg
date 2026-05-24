# 校园论坛个人中心前端 - 实现计划

## Task 1: 扩展 HttpRequestUtil（添加个人中心相关API调用）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加获取用户统计数据的方法
  - 添加获取我的帖子列表的方法
  - 添加获取我的收藏列表的方法
  - 添加修改密码的方法
  - 添加更新用户信息的方法
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4, AC-6, AC-7
- **Test Requirements**:
  - programmatic: 所有新增方法能正常调用后端API
  - programmatic: 正确处理API响应

## Task 2: 创建用户统计数据模型类（可选，如需要）
- **Priority**: P2
- **Depends On**: None
- **Description**: 
  - 创建UserStatistics类（如果需要独立模型）
  - 包含所有统计字段
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - programmatic: 模型类能正确存储数据

## Task 3: 创建个人中心主页面FXML
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 左侧导航栏（BorderPane左侧）
  - 右侧内容区（BorderPane右侧）
  - 导航按钮：个人资料、我的帖子、我的收藏、我的关注、我的粉丝、数据统计、账号设置
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - human-judgment: 页面布局合理，导航清晰

## Task 4: 创建个人资料页面FXML
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 用户头像显示
  - 昵称、学号、个性签名的显示/编辑区域
  - 基础统计信息卡片
  - 编辑/保存/取消按钮
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - human-judgment: 信息展示清晰，编辑功能完整

## Task 5: 创建我的帖子页面FXML
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 帖子列表（TableView或ListView）
  - 显示标题、内容摘要、发布时间、点赞数、评论数、板块名
  - 分页按钮（上一页、下一页、页码显示）
  - 帖子总数显示
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - human-judgment: 列表显示清晰，分页功能正常

## Task 6: 创建我的收藏页面FXML
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 收藏帖子列表
  - 显示标题、内容摘要、作者、发布时间、点赞数、评论数
  - 分页按钮
  - 收藏总数显示
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - human-judgment: 收藏列表展示正确

## Task 7: 创建关注/粉丝页面FXML
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 用户列表
  - 显示头像、昵称、个性签名、发帖数、粉丝数、关注时间
  - 关注/取消关注按钮
  - 切换关注/粉丝标签（或两个独立页面）
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - human-judgment: 用户列表展示完整，交互功能正常

## Task 8: 创建数据统计页面FXML
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 统计卡片布局
  - 展示发帖数、评论数、获赞总数、被收藏数、总浏览量、关注数、粉丝数
  - 数字醒目展示
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - human-judgment: 统计数据展示清晰美观

## Task 9: 创建密码修改页面FXML
- **Priority**: P1
- **Depends On**: Task 3
- **Description**: 
  - 原密码输入框
  - 新密码输入框
  - 确认新密码输入框
  - 提交/取消按钮
  - 验证提示信息
- **Acceptance Criteria Addressed**: AC-7
- **Test Requirements**:
  - human-judgment: 表单布局合理，验证提示清晰

## Task 10: 创建PersonalCenterController控制器
- **Priority**: P0
- **Depends On**: Task 1, Task 3
- **Description**: 
  - 继承ToolController基类
  - 导航切换逻辑
  - 加载和管理各个子页面
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - programmatic: 导航切换功能正常

## Task 11: 创建ProfileController（个人资料控制器）
- **Priority**: P0
- **Depends On**: Task 1, Task 4
- **Description**: 
  - 加载用户资料
  - 编辑模式切换
  - 保存用户资料修改
  - 数据验证
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - programmatic: 资料编辑和保存功能正常

## Task 12: 创建MyPostsController（我的帖子控制器）
- **Priority**: P0
- **Depends On**: Task 1, Task 5
- **Description**: 
  - 加载我的帖子列表
  - 分页功能
  - 点击帖子跳转到详情页
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - programmatic: 列表加载和分页功能正常

## Task 13: 创建MyFavoritesController（我的收藏控制器）
- **Priority**: P0
- **Depends On**: Task 1, Task 6
- **Description**: 
  - 加载我的收藏列表
  - 分页功能
  - 点击帖子跳转到详情页
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - programmatic: 收藏列表加载正常

## Task 14: 创建FollowController（关注/粉丝控制器）
- **Priority**: P0
- **Depends On**: Task 1, Task 7
- **Description**: 
  - 加载关注列表
  - 加载粉丝列表
  - 关注/取消关注操作
  - 列表切换
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - programmatic: 关注功能和列表展示正常

## Task 15: 创建StatisticsController（数据统计控制器）
- **Priority**: P0
- **Depends On**: Task 1, Task 8
- **Description**: 
  - 加载统计数据
  - 展示统计数据
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - programmatic: 统计数据加载和显示正常

## Task 16: 创建PasswordController（密码修改控制器）
- **Priority**: P1
- **Depends On**: Task 1, Task 9
- **Description**: 
  - 密码修改表单处理
  - 验证密码
  - 提交修改
  - 成功/失败提示
- **Acceptance Criteria Addressed**: AC-7
- **Test Requirements**:
  - programmatic: 密码修改功能正常

## Task 17: 集成到主菜单
- **Priority**: P1
- **Depends On**: Task 10
- **Description**: 
  - 在主菜单或MainFrame中添加入口
  - 点击入口跳转到个人中心页面
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - human-judgment: 入口可见且能正常跳转

## Task 18: 整体集成测试
- **Priority**: P0
- **Depends On**: Task 11-17
- **Description**: 
  - 所有功能模块集成测试
  - 用户流程测试
  - UI交互测试
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7
- **Test Requirements**:
  - human-judgment: 整个个人中心使用流程顺畅
  - programmatic: 所有API调用正常工作
