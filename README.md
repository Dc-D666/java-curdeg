# 校园论坛系统（山东大学软件学院 2026 学年春季学期《高级程序开发》课程 2-48 组开发项目）

一个功能完整的校园论坛系统，采用前后端分离架构，集成 AI 内容审核功能。

## 项目概述

### 技术栈

**前端 (java-fx)**

- Java 21 + JavaFX 21 + FXML
- Gson 2.8.9 (JSON 序列化)
- 桌面客户端应用

**后端 (java-server)**

- Spring Boot 3.2.5
- Spring Data JPA + Spring Security + JWT
- MySQL 数据库
- Lombok 1.18.36
- Apache POI 5.2.2 (Excel 处理)
- RestTemplate (HTTP 请求)
- 异步任务处理 (@Async)
- Server-Sent Events (SSE) 流式传输

### 架构特点

- ✅ 前后端分离架构
- ✅ RESTful API 设计
- ✅ JWT 身份认证
- ✅ 基于角色的权限控制
- ✅ 完整的 BBS 功能模块
- ✅ AI 内容审核集成
- ✅ AI 智能搜索 + 流式输出
- ✅ AI 写作助手
- ✅ AI 帖子总结
- ✅ AI 一键配图
- ✅ 丰富的数据统计功能
- ✅ 敏感词过滤（可选）

***

## 功能特性

### 用户功能

- ✅ 用户注册与登录（含邮箱验证码验证）
- ✅ 记住密码功能
- ✅ 个人资料编辑
- ✅ 头像、昵称、个性签名
- ✅ 密码修改（旧密码 + 绑定邮箱验证码 + 新密码确认）
- ✅ 个人中心（我的帖子、我的收藏、我的关注、我的举报、我的通知、我的私信）
- ✅ 隐私设置
- ✅ 私信功能（互关用户可无限畅聊，非互关用户只能发送一条消息）
- ✅ 反馈与建议功能

### 管理功能

- ✅ 帖子管理（置顶、加精、删除）
- ✅ 板块管理
- ✅ 用户管理
- ✅ 举报审核处理（删除内容 / 驳回举报 / 清空违规资料）
- ✅ 举报流程可视化
- ✅ 内容审核管理（人工审核）
- ✅ 审核历史记录查看
- ✅ 数据统计分析（多维度统计、趋势图表、分布图、排行榜）
- ✅ 数据导出功能（帖子/用户/统计，支持 CSV/Excel/JSON 格式）
- ✅ 反馈管理

### 界面优化

- ✅ 菜单栏重新设计（首页、导出、设置、帮助、切换账号）
- ✅ 通知页面美化（卡片式布局、Tab 切换、圆点指示器）
- ✅ 关于页面（项目介绍、版本信息、GitHub 链接）

### 论坛功能

- ✅ 板块列表与导航
- ✅ 帖子浏览与分页
- ✅ 帖子发布、编辑、删除（自动触发 AI 审核）
- ✅ 图片上传支持
- ✅ 帖子与评论附件上传（支持文档、压缩包、音视频等常见文件）
- ✅ 评论与回复（支持嵌套回复、图片、附件、点赞）
- ✅ 帖子点赞与收藏
- ✅ 用户关注功能
- ✅ 帖子举报、评论举报、个人主页资料卡举报
- ✅ 通知系统
- ✅ 智能搜索（标题搜索、全文搜索、搜索结果高亮）
- ✅ AI 智能搜索（同步 + 流式 SSE 输出）
- ✅ AI 写作助手（帮写、续写、润色、自定义指令）
- ✅ AI 帖子总结功能
- ✅ AI 一键配图功能
- ✅ AI 内容审核（发帖自动审核）
- ✅ 审核状态显示（审核中、已通过、内容违规、待人工审核）
- ✅ 本地草稿保存功能
- ✅ 帖子预览功能
- ✅ 实时字数统计

### 管理功能

- ✅ 帖子管理（置顶、加精、删除）
- ✅ 板块管理
- ✅ 用户管理
- ✅ 举报审核处理（删除内容 / 驳回举报 / 清空违规资料）
- ✅ 举报流程可视化
- ✅ 内容审核管理（人工审核）
- ✅ 审核历史记录查看
- ✅ 数据统计分析（多维度统计、趋势图表、分布图、排行榜）

***

## AI 内容审核功能

### 审核流程

1. **用户发帖** → 自动设置为"审核中"状态
2. **异步审核** → 后台调用 AI 接口进行内容审核
3. **审核结果** → 根据 AI 判断更新帖子状态
4. **通知用户** → 审核完成后发送通知
5. **人工复核** → 如需要，管理员可进行人工审核

### 审核状态

| 状态        | 说明    | 可见性                 |
| --------- | ----- | ------------------- |
| `pending` | 审核中   | 仅作者和管理员可见           |
| `pass`    | 审核通过  | 所有用户可见              |
| `reject`  | 内容违规  | 仅作者和管理员可见，作者可编辑重新审核 |
| `manual`  | 待人工审核 | 仅作者和管理员可见           |

### 审核结果字段

- **审核状态**：审核通过/内容违规/待人工审核
- **违规等级**：严重/一般/无
- **违规类型**：具体违规分类
- **违规片段**：检测到的违规内容
- **审核建议**：AI 给出的修改建议
- **审核置信度**：0-100 分

***

## 举报流程

### 举报流转

1. **用户发起举报** → 支持举报帖子、评论、个人主页资料卡
2. **系统记录举报信息** → 资料卡举报会额外保存举报快照（昵称、头像、签名、抓取时间）
3. **人工审核** → 管理员在举报处理页查看举报详情与当前流转状态
4. **处理结果** → 支持删除内容、驳回举报、清空违规资料
5. **通知回执** → 举报人收到处理结果通知；资料卡被清理时，被举报用户也会收到通知

### 举报对象与处理方式

| 举报对象 | 支持处理方式 |
| --- | --- |
| 帖子 | 删除内容、驳回举报 |
| 评论 | 删除内容、驳回举报 |
| 个人主页资料卡 | 清空违规资料、驳回举报 |

### 举报流程可视化

- **管理员端**：举报处理页支持查看 `发起举报 -> 人工审核 -> 处理结果` 的流程卡片
- **用户端**：我的举报详情支持查看举报状态、处理方式、处理备注与资料卡举报快照

***

## AI 搜索与总结功能

### AI 搜索

- **同步搜索**：等待完整结果返回
- **流式搜索**：使用 SSE 协议，实时增量显示结果
- **搜索流程**：先搜索相关帖子，再调用 AI 进行智能分析和回答
- **引导发帖**：无论是否有搜索结果，都提供引导发帖功能，AI 自动生成建议内容

### AI 帖子总结

- 智能判断帖子内容长度，短内容提示无需总结
- 自动总结帖子内容和评论热点
- 支持异步加载，显示进度动画

### AI 写作助手

- 三个预设按钮：AI 帮写、AI 续写、AI 润色
- 支持自定义 AI 指令输入
- AI 生成结果与原始内容对比显示
- 提供"采纳"或"弃用"选择，用户可灵活决定

### AI 一键配图

- 自动从帖子标题和内容提取配图提示词
- 支持多种预设图片尺寸选择
- 实时进度条提示，友好的生成阶段状态
- 图片预览与重新生成功能
- 一键确认添加到帖子图片列表
- 图片自动下载保存到服务器本地

***

## 数据库设计

### 核心数据表

| 表名                   | 说明            |
| -------------------- | ------------- |
| `user`               | 用户表（包含社区相关字段） |
| `user_type`          | 用户类型表         |
| `person`             | 人员信息表         |
| `bbs_board`          | 板块表           |
| `bbs_post`           | 帖子表（含审核字段）    |
| `bbs_comment`        | 评论表           |
| `bbs_like`           | 帖子点赞表         |
| `bbs_comment_like`   | 评论点赞表         |
| `bbs_follow`         | 用户关注表         |
| `bbs_favorite`       | 用户收藏表         |
| `bbs_notification`   | 通知表           |
| `bbs_report`         | 举报表（含资料卡举报快照） |
| `bbs_sensitive_word` | 敏感词表          |
| `bbs_moderation_log` | 审核历史记录表       |
| `student`            | 学生信息表         |
| `course`             | 课程表           |
| `score`              | 成绩表           |
| `student_leave`      | 学生请假表         |
| `menu_info`          | 菜单表           |
| `email_verification` | 邮箱验证表         |
| `statistics_day`     | 每日统计表         |
| `feedback`           | 反馈表           |
| `bbs_conversation`   | 私信会话表         |
| `bbs_message`        | 私信消息表         |

### 数据库初始化脚本

按顺序执行以下 SQL 脚本：

| 脚本                                                      | 说明         |
| ------------------------------------------------------- | ---------- |
| `01_init_database.sql`                                  | 数据库基础初始化   |
| `02_add_like_table.sql`                                 | 添加点赞功能表    |
| `03_add_sensitive_word_report_notification.sql`         | 敏感词、举报、通知表 |
| `04_batch_import_sensitive_words.sql`                   | 导入敏感词库     |
| `05_add_comment_reply_fields.sql`                       | 评论回复功能     |
| `06_add_notification_type_title_fields.sql`             | 通知字段扩展     |
| `07_update_existing_notifications_safe.sql`             | 通知数据迁移     |
| `08_update_notification_titles_to_real_post_titles.sql` | 通知标题更新     |
| `09_add_post_fulltext_index.sql`                        | 全文索引       |
| `10_add_user_follow_feature.sql`                        | 用户关注功能     |
| `11_add_favorite_feature.sql`                           | 收藏功能       |
| `12_add_comment_image_field.sql`                        | 评论图片功能     |
| `13_personal_center_supplement.sql`                     | 个人中心补充     |
| `14_personal_center_menu.sql`                           | 个人中心菜单     |
| `15_fix_menu_table.sql`                                 | 菜单表修复      |
| `16_complete_menu_data.sql`                             | 菜单数据完善     |
| `17_add_person_privacy_fields.sql`                      | 隐私设置       |
| `20_post_search_init.sql`                               | 搜索功能初始化    |
| `21_add_ai_moderation_feature.sql`                      | AI 内容审核功能  |
| `22_add_moderation_menu.sql`                            | 内容审核菜单     |
| `23_add_email_verification_table.sql`                   | 邮箱验证表     |
| `24_enhanced_statistics_support.sql`                    | 增强统计支持     |
| `25_add_comment_like_table.sql`                         | 评论点赞表     |
| `26_add_bbs_attachment_fields.sql`                      | 帖子与评论附件字段 |
| `27_add_profile_report_support.sql`               | 举报流程升级（资料卡举报快照） |
| `28_system_summary_panel.sql`                    | 系统概览界面（可选优化） |
| `29_private_message_feature.sql`                 | 私信功能（会话表+消息表） |
| `30_private_message_menu.sql`                    | 私信功能菜单配置 |

***

## 快速开始

### 环境要求

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- （可选）Redis（后端配置中有，但不是必须的）

### 后端部署

1. **克隆项目**

```bash
git clone https://github.com/Dc-D666/java-curdeg
cd java-curdeg
```

2. **配置数据库**

编辑 `java-server/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/java_2_48?allowPublicKeyRetrieval=true&useSSL=false
    username: your_username
    password: your_password
```

3. **启动后端服务**

```bash
cd java-server
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:22223` 启动。

### 前端部署

1. **配置服务地址**（可选）

编辑 `java-fx/src/main/java/com/teach/javafx/request/HttpRequestUtil.java`，修改服务地址：

```java
public static String serverUrl = "http://localhost:22223";
```

2. **启动前端应用**

```bash
cd java-fx
mvn clean install
mvn javafx:run
```

***

## 项目结构

```
java-curdeg/
├── java-fx/                          # 前端 JavaFX 项目
│   ├── src/main/java/com/teach/javafx/
│   │   ├── controller/               # 控制器层
│   │   │   ├── base/                 # 基础控制器（登录、注册、主框架等）
│   │   │   ├── Post*Controller.java  # 帖子相关控制器
│   │   │   ├── Personal*Controller.java # 个人中心控制器
│   │   │   ├── Admin*Controller.java # 管理员控制器
│   │   │   └── ...                   # 其他业务控制器
│   │   ├── models/                   # 数据模型
│   │   ├── request/                  # HTTP 请求工具
│   │   └── util/                     # 工具类
│   └── src/main/resources/
│       └── com/teach/javafx/         # FXML 界面文件 + CSS 样式
├── java-server/                      # 后端 Spring Boot 项目
│   └── src/main/java/cn/edu/sdu/java/server/
│       ├── configs/                  # 配置类
│       ├── controllers/              # 控制器层（REST API）
│       ├── services/                 # 服务层（业务逻辑）
│       ├── repositorys/              # 数据访问层
│       ├── models/                   # 实体模型
│       └── util/                     # 工具类
└── .trae/                            # 项目开发文档（计划、规范等）
```

***

## API 接口文档

### 认证相关

| 方法   | 路径                              | 说明               |
| ---- | ------------------------------- | ---------------- |
| POST | `/api/auth/login`               | 用户登录            |
| POST | `/api/auth/getValidateCode`     | 获取验证码           |
| POST | `/api/auth/testValidateInfo`    | 验证验证码信息         |
| POST | `/api/auth/sendEmailCode`       | 发送邮箱验证码         |
| POST | `/api/auth/registerUser`        | 用户注册            |
| POST | `/api/auth/resetAdminPassword`  | 重置管理员密码（开发调试用） |
| POST | `/api/bbs/user/me/password/code` | 发送修改密码邮箱验证码（发送到当前账号绑定邮箱） |
| POST | `/api/bbs/user/me/password`     | 修改当前登录用户密码（旧密码 + 邮箱验证码） |

### 文件相关

| 方法   | 路径                              | 说明               |
| ---- | ------------------------------- | ---------------- |
| POST | `/api/bbs/file/upload-image`     | 上传帖子/评论图片       |
| POST | `/api/bbs/file/upload-attachment` | 上传帖子/评论通用附件     |

### 帖子相关

| 方法   | 路径                              | 说明                 |
| ---- | ------------------------------- | ------------------ |
| GET  | `/api/bbs/post/list`            | 获取帖子列表（支持分页、板块筛选） |
| GET  | `/api/bbs/post/{id}`            | 获取帖子详情            |
| POST | `/api/bbs/post`                 | 发布帖子（自动触发 AI 审核）  |
| PUT  | `/api/bbs/post/{id}`            | 修改帖子（编辑后重新触发 AI 审核） |
| DELETE | `/api/bbs/post/{id}`            | 删除帖子              |
| POST | `/api/bbs/post/{id}/top`        | 置顶/取消置顶帖子（管理员）     |
| POST | `/api/bbs/post/{id}/feature`    | 加精/取消加精帖子（管理员）     |
| GET  | `/api/bbs/post/{id}/like/status` | 获取点赞状态            |
| POST | `/api/bbs/post/{id}/like`       | 点赞/取消点赞           |
| GET  | `/api/bbs/post/{id}/favorite/status` | 获取收藏状态        |
| POST | `/api/bbs/post/{id}/favorite`   | 收藏/取消收藏           |
| GET  | `/api/bbs/post/search`          | 帖子搜索（支持关键词、搜索类型） |
| POST | `/api/bbs/post/ai-search`       | AI 搜索（同步）        |
| GET  | `/api/bbs/post/ai-search-stream` | AI 搜索（流式 SSE）    |
| POST | `/api/bbs/post/{postId}/summary` | 获取帖子总结（需要登录） |

### 评论相关

| 方法   | 路径                              | 说明           |
| ---- | ------------------------------- | ------------ |
| GET  | `/api/bbs/comment/post/{postId}` | 获取帖子的评论列表  |
| GET  | `/api/bbs/comment/detail/{id}`   | 获取评论详情     |
| POST | `/api/bbs/comment/post/{postId}` | 发布评论        |
| PUT  | `/api/bbs/comment/{id}`          | 修改评论        |
| DELETE | `/api/bbs/comment/{id}`          | 删除评论        |
| GET  | `/api/bbs/comment/{id}/like/status` | 获取评论点赞状态 |
| POST | `/api/bbs/comment/{id}/like`     | 评论点赞/取消点赞  |

### 板块相关

| 方法   | 路径                      | 说明          |
| ---- | ----------------------- | ----------- |
| GET  | `/api/bbs/board/list`   | 获取板块列表      |
| POST | `/api/bbs/board`        | 创建板块（管理员）  |
| PUT  | `/api/bbs/board/{id}`   | 修改板块（管理员）  |
| DELETE | `/api/bbs/board/{id}`   | 删除板块（管理员）  |

### 关注相关

| 方法   | 路径                                      | 说明        |
| ---- | --------------------------------------- | --------- |
| GET  | `/api/bbs/follow/check`                 | 检查关注状态    |
| POST | `/api/bbs/follow`                       | 关注/取消关注  |
| GET  | `/api/bbs/follow/followers/{userId}`    | 获取粉丝列表    |
| GET  | `/api/bbs/follow/following/{userId}`    | 获取关注列表    |

### 通知相关

| 方法   | 路径                              | 说明       |
| ---- | ------------------------------- | -------- |
| GET  | `/api/bbs/notification/unread-count` | 获取未读通知数  |
| GET  | `/api/bbs/notification/my-list`      | 获取我的通知列表 |
| POST | `/api/bbs/notification/{id}/read`    | 标记通知已读   |
| POST | `/api/bbs/notification/mark-all-read` | 全部标记已读 |

### 举报相关

| 方法   | 路径                          | 说明         |
| ---- | --------------------------- | ---------- |
| POST | `/api/bbs/report`            | 提交举报（支持帖子、评论、个人主页资料卡） |
| GET  | `/api/bbs/report/my-list`    | 获取我的举报记录   |
| GET  | `/api/bbs/report/admin-list` | 获取举报列表（管理员） |
| POST | `/api/bbs/report/{id}/handle` | 处理举报（管理员，支持删除内容 / 驳回举报 / 清空违规资料） |

### 文件相关

| 方法   | 路径                      | 说明    |
| ---- | ----------------------- | ----- |
| POST | `/api/bbs/file/upload`   | 文件上传  |

### 内容审核相关（管理员）

| 方法   | 路径                                      | 说明                 |
| ---- | --------------------------------------- | ------------------ |
| GET  | `/api/admin/moderation/pending`         | 获取待审核帖子列表         |
| GET  | `/api/admin/moderation/posts`           | 获取全部审核帖子（含历史）     |
| POST | `/api/admin/moderation/{postId}/review` | 人工审核帖子（decision: pass/reject） |

### 统计相关（管理员）

所有统计接口均需要管理员权限。

#### 数据概览

| 方法   | 路径                              | 说明      |
| ---- | ------------------------------- | ------- |
| GET  | `/api/bbs/statistics/overview`   | 获取系统概览  |

#### 用户统计

| 方法   | 路径                                | 说明              |
| ---- | --------------------------------- | --------------- |
| GET  | `/api/bbs/statistics/user-growth`   | 用户增长趋势（支持天数参数） |
| GET  | `/api/bbs/statistics/user-type`     | 用户类型分布          |
| GET  | `/api/bbs/statistics/active-users`  | 活跃用户列表（支持排序）    |
| GET  | `/api/bbs/statistics/user-activity` | 用户活跃度分布         |
| GET  | `/api/bbs/statistics/banned-users`  | 被封禁用户统计         |

#### 内容统计

| 方法   | 路径                                     | 说明                 |
| ---- | -------------------------------------- | ------------------ |
| GET  | `/api/bbs/statistics/post-trend`      | 发帖趋势（支持天数参数）      |
| GET  | `/api/bbs/statistics/board-distribution` | 板块分布             |
| GET  | `/api/bbs/statistics/hot-posts`       | 热门帖子（支持排序）        |
| GET  | `/api/bbs/statistics/post-status`     | 帖子状态分布            |
| GET  | `/api/bbs/statistics/image-post-ratio` | 图文贴比例            |

#### 互动统计

| 方法   | 路径                                  | 说明            |
| ---- | ----------------------------------- | ------------- |
| GET  | `/api/bbs/statistics/comment-trend`    | 评论趋势（支持天数参数）   |
| GET  | `/api/bbs/statistics/like-trend`       | 点赞趋势（支持天数参数）   |
| GET  | `/api/bbs/statistics/favorite-trend`   | 收藏趋势（支持天数参数）   |
| GET  | `/api/bbs/statistics/follow-trend`     | 关注趋势（支持天数参数）   |
| GET  | `/api/bbs/statistics/hot-comments`     | 热门评论          |

#### 内容安全统计

| 方法   | 路径                                      | 说明              |
| ---- | --------------------------------------- | --------------- |
| GET  | `/api/bbs/statistics/moderation-overview` | 审核概览（总数、待审核、通过率等） |
| GET  | `/api/bbs/statistics/moderation-trend`    | 审核趋势（支持天数参数）     |
| GET  | `/api/bbs/statistics/violation-types`     | 违规类型分布          |
| GET  | `/api/bbs/statistics/report-statistics`   | 举报统计            |

### 数据导出相关（管理员）

| 方法   | 路径                              | 说明                |
| ---- | ------------------------------- | ----------------- |
| POST | `/api/admin/export/posts`       | 导出帖子数据（支持范围、字段、格式选择） |
| POST | `/api/admin/export/users`       | 导出用户数据（支持字段、格式选择） |
| POST | `/api/admin/export/statistics`  | 导出统计数据（支持格式选择） |

### 反馈相关

| 方法   | 路径                              | 说明                |
| ---- | ------------------------------- | ----------------- |
| POST | `/api/bbs/feedback`             | 提交反馈（需要登录）      |
| GET  | `/api/bbs/feedback/list`        | 获取我的反馈列表        |
| GET  | `/api/admin/feedback/list`      | 获取全部反馈列表（管理员） |
| POST | `/api/admin/feedback/{id}/handle` | 处理反馈（管理员）        |

### 私信相关

| 方法   | 路径                              | 说明                |
| ---- | ------------------------------- | ----------------- |
| GET  | `/api/bbs/conversation/list`    | 获取我的会话列表        |
| POST | `/api/bbs/conversation/send`    | 发送私信（互关用户无限量、非互关仅1条） |
| GET  | `/api/bbs/conversation/{conversationId}/messages` | 获取会话的消息列表 |
| POST | `/api/bbs/conversation/{conversationId}/read` | 标记会话为已读 |


***

## 配置说明

### AI 审核配置

在 `application.yml` 中配置 AI 审核接口：

```yaml
moderation:
  enabled: true  # 是否启用 AI 审核
  api:
    url: https://xplt.sdu.edu.cn:4000/v1/chat/completions
    key: your-api-key-here
    model: SDU-AI/DeepSeek-V4-Flash
    timeout: 60000  # 超时时间（毫秒）
```

### AI 图片生成配置

在 `application.yml` 中配置 AI 图片生成接口：

```yaml
ai-image:
  enabled: true  # 是否启用 AI 图片生成
  api:
    url: https://open.bigmodel.cn/api/paas/v4/images/generations
    key: your-api-key-here
    model: cogview-3-flash
    timeout: 30000  # 超时时间（毫秒）
```

### 文件上传配置

```yaml
attach:
  folder: ./uploads/  # 上传文件保存目录
```

### 邮箱验证码配置

注册和修改密码都会使用邮箱验证码。修改密码验证码只会发送到当前登录用户在个人资料中绑定的邮箱，前端不能手动指定收件邮箱。

建议通过环境变量配置发件邮箱，避免把真实授权码写入仓库：

```bash
MAIL_USERNAME=your_qq_email@qq.com
MAIL_PASSWORD=your_qq_smtp_auth_code
```

`MAIL_PASSWORD` 需要填写 QQ 邮箱的 SMTP 授权码，不是 QQ 登录密码。若发送失败，后端会返回“邮箱授权码无效或 SMTP 服务未开启”等明确提示，并且不会保存验证码或占用发送冷却时间。

***

## 权限说明

### 用户角色

| 角色            | 权限范围                         |
| ------------- | ---------------------------- |
| `ROLE_STUDENT` | 普通学生：发帖、评论、点赞、收藏等基础功能        |
| `ROLE_ADMIN`   | 管理员：学生功能 + 帖子管理、举报处理、内容审核、统计查看 |
| `ROLE_SUPER`   | 超级管理员：管理员功能 + 板块管理、用户管理、系统配置、教务管理 |

***

## 开发说明

### 开发规范

1. **代码风格**
   - 遵循项目现有代码风格
   - 合理使用 Lombok 注解
   - 保持命名规范统一
2. **提交规范**
   - 清晰的提交信息
   - 功能提交：`feat: xxx`
   - 修复提交：`fix: xxx`
   - 文档提交：`docs: xxx`
3. **接口设计**
   - 统一使用 `DataRequest` 和 `DataResponse`
   - 保持 RESTful 风格
   - 合理使用 HTTP 状态码

***

## 常见问题

### Q: 后端启动失败，提示数据库连接错误？

A: 请检查 `application.yml` 中的数据库配置，确保 MySQL 服务已启动，且数据库名、用户名、密码正确。

### Q: 前端无法连接后端服务？

A: 请检查：

1. 后端服务是否正常启动
2. `HttpRequestUtil.java` 中的 `serverUrl` 配置是否正确
3. 防火墙是否阻止了连接

### Q: 通知列表无法加载？

A: 请确保已完成最新的修复：

- Notification 模型已添加 SerializedName 注解
- createTime 字段类型已调整为 String
- 检查控制台的调试日志

### Q: AI 审核没有生效？

A: 请检查：

1. `application.yml` 中的 `moderation.enabled` 是否为 `true`
2. AI API 的 url、key、model 配置是否正确
3. 查看后端控制台的调试输出

### Q: 流式搜索没有正常工作？

A: 请检查：

1. 后端是否正确发送 SSE 事件
2. 前端是否正确处理 SSE 流
3. 网络连接是否稳定

***

## 更新日志

### v2.9 (2026-05-17) - 菜单栏重构、数据导出与反馈系统
- 菜单栏重新设计：首页/导出/设置/帮助/切换账号
- 新增数据导出功能（帖子/用户/统计，支持 CSV/Excel/JSON 格式，管理员可见）
- 新增反馈系统（用户提交反馈，管理员处理反馈）
- 新增关于页面（项目信息、GitHub 链接）
- 通知页面美化（卡片式布局、Tab 切换、圆点指示器）
- 修复导出 API 返回格式与模块访问权限问题

### v2.8 (2026-05-16) - 私信功能与UI优化
- 新增私信功能：用户之间可以发送私信进行一对一交流
- 个人中心新增"我的私信"入口，显示未读消息红点提示
- 私信会话列表页面，支持刷新、未读标记、头像点击跳转用户主页
- 私信详情页面，支持文本消息和图片消息发送与展示
- 互关权限控制：互关用户可无限畅聊，非互关用户只能发送一条消息
- 修复多个页面的文字对比度问题，提升可读性
- 修复头像点击跳转到用户主页的功能
- 修复发送消息显示失败但实际成功的问题

### v2.7 (2026-05-13) - 附件上传与举报流程升级
- 新增通用附件上传接口，支持帖子和评论上传附件，单个附件最大 20MB、单次最多 5 个
- 帖子和评论新增附件元数据字段，详情页和评论区以附件列表展示并支持点击下载
- JavaFX 发帖页、评论输入区新增附件选择、预览和删除交互
- 新增个人主页资料卡举报，支持举报 `头像 / 昵称 / 个性签名`
- 举报流程升级为 `发起举报 -> 人工审核 -> 处理结果`，管理员页与我的举报详情支持流程可视化
- 管理员举报处理新增“清空违规资料”动作，资料卡举报会保存举报快照并通知相关用户
- 修正举报统计中的有效举报口径，并补充 `27_add_profile_report_support.sql` 数据库脚本

### v2.6 (2026-05-11) - 修改密码安全验证
- 修改个人中心“修改密码”流程，新增旧密码、绑定邮箱验证码、新密码确认三重校验
- 新增 `/api/bbs/user/me/password/code` 接口，验证码只发送到当前账号绑定邮箱
- 修改 `/api/bbs/user/me/password` 接口，提交时校验旧密码、新密码规则和邮箱验证码
- 邮件发送配置支持 `MAIL_USERNAME`、`MAIL_PASSWORD` 环境变量，SMTP 失败时返回明确中文提示
- 优化 JavaFX 修改密码页面，修复乱码，增加发送验证码倒计时、页面内状态提示和可滚动卡片式布局
- 修改密码成功后清空登录状态并返回登录页，要求用户使用新密码重新登录

### v2.5 (2026-05-06) - 用户主页功能
- 新增用户主页功能：点击帖子详情页或评论区的用户头像可跳转到用户主页
- 用户主页展示：头像、昵称、个性签名、发帖数、粉丝数、关注数
- 支持关注/取消关注功能
- 展示用户发布的公开帖子列表（支持分页）
- 私信功能入口预留（暂未实现）

### v2.4 (2026-04-25) - AI 一键配图、搜索优化
- 新增 AI 一键配图功能（支持尺寸选择、预览、重新生成）
- AI 搜索优化：无论是否有结果都提供引导发帖功能，AI 自动生成建议内容
- 优化流式搜索：修复卡死问题、增强 Markdown 解析、优化性能
- 移除界面 emoji，改用纯文本

### v2.3 (2026-04-25) - AI 写作助手、发布页优化
- 新增 AI 写作助手（帮写、续写、润色、自定义指令）
- 发布页面全面优化：实时字数统计、多图上传管理、本地草稿保存、帖子预览
- AI 模型升级为 SDU-AI/DeepSeek-V4-Flash，超时时间增至 60 秒

### v2.2 (2026-04-24) - AI 搜索与总结
- 新增 AI 流式搜索接口（SSE 协议）
- 新增 AI 帖子总结功能
- 修复帖子详情页滚动问题

### v2.1 (2026-04-24) - 数据统计完善
- 丰富数据统计功能（多维度趋势图表、分布图、排行榜）
- 完善审核统计和举报统计

### v2.0 (2026-04-24) - AI 内容审核
- 集成 AI 内容审核功能（自动审核、状态显示、历史记录）
- 新增智能搜索和结果高亮

### v1.x
- 基础功能：用户系统、发帖评论、关注收藏、通知举报、个人中心等

***

## 贡献指南

欢迎提交 Issue 和 Pull Request！

***

## 许可证

本项目仅供学习交流使用。

***

## 联系方式

如有问题，请提交 Issue。
