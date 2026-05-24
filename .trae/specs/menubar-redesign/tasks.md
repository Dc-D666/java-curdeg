# Tasks

- [x] Task 1: 修改 main-frame.fxml 菜单栏结构
  - [x] SubTask 1.1: 移除"工具"菜单（menuTool 及其子项 menuNew/menuSave/menuDelete/menuPrint/menuImport/menuExport）
  - [x] SubTask 1.2: 移除"我的"菜单（menuMy 及其子项 menuLogout）
  - [x] SubTask 1.3: 修改"首页"菜单子项文本从"帖子列表"改为"帖子广场"，事件改为 onPostSquareClick
  - [x] SubTask 1.4: 新增"导出"菜单（fx:id=menuExport, visible=false），包含三个子项：导出帖子数据、导出用户数据、导出统计报表
  - [x] SubTask 1.5: 新增"设置"菜单（fx:id=menuSettings），包含：个人资料、修改密码、分隔线、退出登录
  - [x] SubTask 1.6: 新增"帮助"菜单（fx:id=menuHelp），包含：关于、反馈与建议
  - [x] SubTask 1.7: 严格保留"切换账号"菜单（fx:id=menuSwitchAccount），不做任何修改

- [x] Task 2: 修改 MainFrameController.java 菜单逻辑
  - [x] SubTask 2.1: 移除 menuTool、menuMy 及其子项的 FXML 绑定字段
  - [x] SubTask 2.2: 新增 menuExport、menuSettings、menuHelp 的 FXML 绑定字段
  - [x] SubTask 2.3: 新增 onPostSquareClick 方法（调用 changeContent("post-list", "帖子广场")）
  - [x] SubTask 2.4: 新增 onExportPostsClick、onExportUsersClick、onExportStatsClick 方法
  - [x] SubTask 2.5: 新增 onProfileClick 方法（调用 changeContent("personal-center", "个人中心")）
  - [x] SubTask 2.6: 新增 onChangePasswordClick 方法（调用 changeContent("password-change", "修改密码")）
  - [x] SubTask 2.7: 新增 onAboutClick 方法（打开关于对话框）
  - [x] SubTask 2.8: 新增 onFeedbackClick 方法（打开反馈对话框）
  - [x] SubTask 2.9: 在 initialize() 中根据角色设置 menuExport 可见性
  - [x] SubTask 2.10: 移除 onNewMenuClick/onSaveMenuClick/onDeleteMenuClick/onPrintMenuClick/onImportMenuClick/onExportMenuClick 方法

- [x] Task 3: 创建"关于"对话框
  - [x] SubTask 3.1: 创建 about-dialog.fxml
  - [x] SubTask 3.2: 创建 AboutDialogController.java

- [x] Task 4: 创建"反馈与建议"对话框
  - [x] SubTask 4.1: 创建 feedback-dialog.fxml
  - [x] SubTask 4.2: 创建 FeedbackDialogController.java

- [x] Task 5: 创建"导出帖子数据"对话框
  - [x] SubTask 5.1: 创建 export-posts-dialog.fxml
  - [x] SubTask 5.2: 创建 ExportPostsDialogController.java

- [x] Task 6: 后端新增反馈 API
  - [x] SubTask 6.1: 创建 Feedback 实体类
  - [x] SubTask 6.2: 创建 FeedbackRepository
  - [x] SubTask 6.3: 创建 FeedbackService
  - [x] SubTask 6.4: 创建 FeedbackController（POST /api/bbs/feedback, GET /api/admin/feedback/list）

- [x] Task 7: 后端新增导出 API
  - [x] SubTask 7.1: 创建 ExportController（POST /api/admin/export/posts, /users, /statistics）
  - [x] SubTask 7.2: 在 pom.xml 中添加 Apache POI 依赖

- [x] Task 8: 编译验证
  - [x] SubTask 8.1: 编译前端项目
  - [x] SubTask 8.2: 编译后端项目

# Task Dependencies
- Task 2 depends on Task 1
- Task 3, Task 4, Task 5 depend on Task 2 (需要 onAboutClick/onFeedbackClick/onExportPostsClick 方法)
- Task 4 depends on Task 6 (前端提交反馈需要后端 API)
- Task 5 depends on Task 7 (前端导出数据需要后端 API)
- Task 8 depends on all other tasks
