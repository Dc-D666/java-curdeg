# 数据统计分析与可视化 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 后端修改 BbsPostRepository - 新增统计查询方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsPostRepository 接口中新增 countDailyPostLast7Days() 方法，使用 @Query 注解 + 原生 SQL 统计最近 7 天的发帖量
  - 在 BbsPostRepository 接口中新增 findTop10ByStatusOrderByLikeCountDescCommentCountDesc() 方法，查询热门帖子 Top10
- **Success Criteria**:
  - 新增方法能够正确编译
  - 方法签名符合 JPA 规范
- **Test Requirements**:
  - `programmatic` TR-1.1: 新增方法无编译错误
  - `human-judgement` TR-1.2: 方法命名规范，注解使用正确
- **Notes**: 注意 create_time 字段的格式，确保 SQL 能正确解析日期

## [x] Task 2: 后端修改 UserRepository - 新增活跃用户查询方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 UserRepository 接口中新增 findTop10ByIsBannedOrderByPostCountDescCommentCountDesc() 方法，查询活跃用户 Top10
- **Success Criteria**:
  - 新增方法能够正确编译
  - 方法签名符合 JPA 规范
- **Test Requirements**:
  - `programmatic` TR-2.1: 新增方法无编译错误
  - `human-judgement` TR-2.2: 方法命名规范
- **Notes**: isBanned 字段设为 0（未被禁言）

## [x] Task 3: 后端新建 StatisticsController - 数据统计接口
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 在 controller 包下新建 StatisticsController.java
  - 添加 @RequestMapping("/api/bbs/statistics") 注解
  - 新增 GET /api/bbs/statistics/daily-post 接口：统计最近 7 天发帖量
  - 新增 GET /api/bbs/statistics/hot-post 接口：查询热门帖子 Top10
  - 新增 GET /api/bbs/statistics/active-user 接口：查询活跃用户 Top10
  - 所有接口需要 ADMIN 或 SUPER 权限
- **Success Criteria**:
  - Controller 能够正确编译
  - 三个接口功能完整
  - 正确处理用户权限
- **Test Requirements**:
  - `programmatic` TR-3.1: Controller 无编译错误
  - `programmatic` TR-3.2: 三个接口都有正确的权限注解
  - `human-judgement` TR-3.3: 接口业务逻辑清晰，代码可读性好

## [ ] Task 4: 后端测试 - Postman 测试统计接口
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 用 Postman 测试统计最近 7 天的发帖量接口
  - 用 Postman 测试查询热门帖子 Top10 接口
  - 用 Postman 测试查询活跃用户 Top10 接口
- **Success Criteria**:
  - 三个接口都返回正确的数据
  - 数据格式符合预期
  - 权限控制正常工作
- **Test Requirements**:
  - `programmatic` TR-4.1: 三个接口都返回 200 状态码
  - `programmatic` TR-4.2: 返回的 JSON 数据格式正确
  - `human-judgement` TR-4.3: 数据内容合理，统计逻辑正确
- **Notes**: 需要使用管理员账号登录获取 token

## [x] Task 5: 前端修改 HttpRequestUtil - 新增统计请求方法
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 在 HttpRequestUtil 中新增 getDailyPostStatistics() 方法
  - 在 HttpRequestUtil 中新增 getHotPostStatistics() 方法
  - 在 HttpRequestUtil 中新增 getActiveUserStatistics() 方法
- **Success Criteria**:
  - 三个方法能够正确编译
  - 方法实现符合现有代码风格
- **Test Requirements**:
  - `programmatic` TR-5.1: 新增方法无编译错误
  - `human-judgement` TR-5.2: 代码风格与现有方法一致

## [x] Task 6: 前端修改管理后台菜单 - 新增数据统计入口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 MainFrameController 中，在"管理后台"菜单下新增"数据统计"子菜单
- **Success Criteria**:
  - 菜单能够正常显示
  - 点击菜单能够打开对应页面
- **Test Requirements**:
  - `programmatic` TR-6.1: 代码无编译错误
  - `human-judgement` TR-6.2: 菜单位置和命名正确

## [x] Task 7: 前端新建 statistics.fxml - 数据统计页面 UI
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 新建 statistics.fxml 文件
  - 使用 BorderPane 作为根组件
  - 顶部放"数据统计"Label
  - 中间放 TabPane，包含三个 Tab：
    - Tab 1: "每日发帖量"，内放 LineChart
    - Tab 2: "热门帖子 Top10"，内放 TableView
    - Tab 3: "活跃用户 Top10"，内放 TableView
- **Success Criteria**:
  - FXML 文件语法正确
  - UI 布局符合设计要求
- **Test Requirements**:
  - `human-judgement` TR-7.1: FXML 文件结构完整
  - `human-judgement` TR-7.2: UI 元素齐全

## [x] Task 8: 前端新建 StatisticsController - 数据统计页面逻辑
- **Priority**: P0
- **Depends On**: Task 5, Task 7
- **Description**: 
  - 新建 StatisticsController.java
  - 在 initialize 方法中异步调用三个统计接口
  - 成功后通过 Platform.runLater 更新 UI：
    - 每日发帖量数据填充到 LineChart
    - 热门帖子数据填充到 TableView
    - 活跃用户数据填充到 TableView
- **Success Criteria**:
  - Controller 能够正确编译
  - 三个图表/表格都能正确显示数据
  - 异步处理正确，不阻塞 UI 线程
- **Test Requirements**:
  - `programmatic` TR-8.1: Controller 无编译错误
  - `human-judgement` TR-8.2: 异步请求实现正确
  - `human-judgement` TR-8.3: 数据展示逻辑清晰

## [ ] Task 9: 前端测试 - 数据统计可视化完整测试
- **Priority**: P0
- **Depends On**: Task 8
- **Description**: 
  - 运行前端工程
  - 用管理员账号登录
  - 打开"数据统计"页
  - 测试"每日发帖量"折线图
  - 测试"热门帖子 Top10"表格
  - 测试"活跃用户 Top10"表格
- **Success Criteria**:
  - 三个统计模块都能正常工作
  - 数据正确显示
  - UI 响应流畅
- **Test Requirements**:
  - `human-judgement` TR-9.1: 折线图能正常显示数据
  - `human-judgement` TR-9.2: 热门帖子表格能正常显示
  - `human-judgement` TR-9.3: 活跃用户表格能正常显示
  - `human-judgement` TR-9.4: 整体 UI 流畅，无明显卡顿
