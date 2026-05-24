# 顶部菜单栏重新设计 Spec

## Why
当前顶部菜单栏（首页、工具、我的、切换账号）与论坛业务无关，"工具"菜单继承自通用框架，"我的"仅有一个退出功能，需要重新设计为面向论坛业务的全局操作入口。

## What Changes
- **BREAKING**: 移除"工具"菜单（新建/保存/删除/打印/导入/导出）和"我的"菜单
- 新增"导出"菜单（仅管理员可见），包含导出帖子数据、用户数据、统计报表
- 新增"设置"菜单，包含个人资料、修改密码、退出登录
- 新增"帮助"菜单，包含关于对话框和反馈与建议对话框
- 修改"首页"菜单，子项从"帖子列表"改为"帖子广场"
- 严格保留"切换账号"菜单，逻辑不变
- 新增"关于"对话框（about-dialog.fxml + AboutDialogController）
- 新增"反馈与建议"对话框（feedback-dialog.fxml + FeedbackDialogController）
- 新增"导出帖子数据"对话框（export-posts-dialog.fxml + ExportPostsDialogController）
- 后端新增反馈提交 API 和导出 API

## Impact
- Affected specs: 无
- Affected code:
  - `java-fx/src/main/resources/com/teach/javafx/base/main-frame.fxml` — 重写 MenuBar
  - `java-fx/src/main/java/com/teach/javafx/controller/base/MainFrameController.java` — 新增菜单事件方法、角色判断
  - `java-server` — 新增 FeedbackController、ExportController

## ADDED Requirements

### Requirement: 菜单栏结构重设计
系统 SHALL 将顶部菜单栏从"首页/工具/我的/切换账号"改为"首页/导出/设置/帮助/切换账号"。

#### Scenario: 普通用户看到菜单栏
- **WHEN** ROLE_STUDENT 用户登录
- **THEN** 菜单栏显示：首页、设置、帮助、切换账号（导出菜单不可见）

#### Scenario: 管理员用户看到菜单栏
- **WHEN** ROLE_SUPER 或 ROLE_ADMIN 用户登录
- **THEN** 菜单栏显示：首页、导出、设置、帮助、切换账号

### Requirement: 首页菜单
系统 SHALL 提供"首页"菜单，包含"帖子广场"子项，点击后调用 `changeContent("post-list", "帖子广场")`。

### Requirement: 导出菜单（管理员专用）
系统 SHALL 提供"导出"菜单，仅对 ROLE_SUPER 和 ROLE_ADMIN 可见，包含三个子项：
1. 导出帖子数据 — 打开导出帖子数据对话框
2. 导出用户数据 — 打开导出用户数据对话框
3. 导出统计报表 — 打开导出统计报表对话框

#### Scenario: 学生用户看不到导出菜单
- **WHEN** ROLE_STUDENT 用户登录
- **THEN** "导出"菜单不可见

#### Scenario: 管理员导出帖子数据
- **WHEN** 管理员点击"导出 → 导出帖子数据"
- **THEN** 弹出导出帖子数据对话框，可选择导出范围、字段、格式

### Requirement: 设置菜单
系统 SHALL 提供"设置"菜单，包含：
1. 个人资料 — 调用 `changeContent("personal-center", "个人中心")`
2. 修改密码 — 调用 `changeContent("password-change", "修改密码")`
3. 分隔线
4. 退出登录 — 调用 `logout()`

### Requirement: 帮助菜单
系统 SHALL 提供"帮助"菜单，包含：
1. 关于 — 打开关于对话框
2. 反馈与建议 — 打开反馈对话框

### Requirement: 关于对话框
系统 SHALL 提供"关于"对话框，展示以下信息：
- 项目名称：学生交流社区（校园论坛系统）
- 版本号：v2.7
- 项目简介
- GitHub 仓库链接（可点击）
- 技术栈信息
- AI 功能列表
- 开发团队信息（从 systemPrompt 获取）
- 开源协议：MIT License

#### Scenario: 用户查看关于信息
- **WHEN** 用户点击"帮助 → 关于"
- **THEN** 弹出对话框展示项目信息，包含可点击的 GitHub 链接

### Requirement: 反馈与建议对话框
系统 SHALL 提供"反馈与建议"对话框，包含：
- 反馈类型（下拉选择）：Bug 报告 / 功能建议 / 体验优化 / 其他
- 标题（文本输入，必填，最多 50 字符）
- 详细描述（多行文本，必填，最多 2000 字符）
- 联系方式（文本输入，可选）

#### Scenario: 用户提交反馈
- **WHEN** 用户填写反馈表单并点击提交
- **THEN** 系统调用 `POST /api/bbs/feedback` 提交反馈，成功后显示提示并关闭对话框

#### Scenario: 必填字段为空
- **WHEN** 用户未填写标题或详细描述就点击提交
- **THEN** 显示错误提示，不提交

### Requirement: 导出帖子数据对话框
系统 SHALL 提供"导出帖子数据"对话框，支持：
- 导出范围选择：全部帖子 / 指定板块 / 指定时间范围 / 指定用户
- 导出字段勾选：帖子ID、标题、内容、作者、板块、点赞数、评论数、创建时间、更新时间、状态
- 导出格式选择：Excel / CSV / JSON
- 其他选项：包含已删除帖子

#### Scenario: 管理员导出全部帖子
- **WHEN** 管理员选择"全部帖子"、勾选所有字段、选择 Excel 格式并点击导出
- **THEN** 系统调用 `POST /api/admin/export/posts`，返回文件并保存到本地

### Requirement: 切换账号菜单严格保留
系统 SHALL 严格保留"切换账号"菜单，包括：
- 菜单名称"切换账号"不变
- 子菜单项动态生成逻辑不变（super/admin1/admin2/user1/user2）
- 当前用户对应的菜单项 disable 逻辑不变
- 切换账号事件处理逻辑不变
- 对所有角色可见

## MODIFIED Requirements

### Requirement: MainFrameController 菜单初始化
MainFrameController.initialize() 方法 SHALL：
1. 移除 menuTool、menuMy 相关的 FXML 绑定
2. 新增 menuExport（Menu 类型）、menuSettings（Menu 类型）、menuHelp（Menu 类型）的 FXML 绑定
3. 在初始化时根据角色设置 menuExport 的可见性
4. 保留 menuSwitchAccount 的动态生成逻辑不变

## REMOVED Requirements

### Requirement: 工具菜单
**Reason**: "工具"菜单（新建/保存/删除/打印/导入/导出）继承自通用框架 ToolController，与论坛业务无关
**Migration**: 移除 FXML 中的 menuTool 及其子项，移除 MainFrameController 中对应的 FXML 绑定和事件方法

### Requirement: 我的菜单
**Reason**: "我的"菜单仅有一个"退出登录"功能，名不副实
**Migration**: 退出登录功能迁移到"设置"菜单中
