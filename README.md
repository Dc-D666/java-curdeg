# 校园论坛系统

一个功能完整的校园论坛系统，采用前后端分离架构，集成 AI 内容审核功能，同时包含完整的教务管理功能。

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
- ✅ AI 写作助手
- ✅ AI 智能搜索 + 流式输出
- ✅ AI 帖子总结
- ✅ AI 一键配图
- ✅ 完整的教务管理功能（学生、课程、成绩、请假）
- ✅ 丰富的数据统计功能
- ✅ 敏感词过滤（可选）

***

## 功能特性

### 用户功能

- ✅ 用户注册与登录（含邮箱验证码验证）
- ✅ 记住密码功能
- ✅ 个人资料编辑
- ✅ 头像、昵称、个性签名
- ✅ 密码修改
- ✅ 个人中心（我的帖子、我的收藏、我的关注、我的举报、我的通知）
- ✅ 隐私设置

### 论坛功能

- ✅ 板块列表与导航
- ✅ 帖子浏览与分页
- ✅ 帖子发布、编辑、删除（自动触发 AI 审核）
- ✅ 图片上传支持
- ✅ 评论与回复（支持嵌套回复、图片、点赞）
- ✅ 帖子点赞与收藏
- ✅ 用户关注功能
- ✅ 帖子举报
- ✅ 通知系统
- ✅ 智能搜索（标题搜索、全文搜索、搜索结果高亮）
- ✅ AI 智能搜索（同步 + 流式 SSE 输出）
- ✅ AI 帖子总结功能
- ✅ AI 写作助手 - 在帖子发布页集成 AI 写作助手
  - 预设按钮：AI 帮写、AI 续写、AI 润色
  - 自定义指令输入
  - AI 生成结果对比和采纳/弃用功能
- ✅ **AI 一键配图功能** - 在帖子发布页面集成智能配图生成
  - 自动从帖子标题和内容提取配图提示词
  - 支持 8 种预设图片尺寸选择
  - 实时进度条提示，友好的生成状态
  - 图片预览与重新生成功能
  - 一键确认添加到帖子图片列表
  - 图片自动下载保存到服务器
  - 使用智谱 AI Cogview-3-Flash 模型
- ✅ 发布帖子页面优化
  - 清晰的区域划分（基本信息、内容编辑、AI 助手、图片附件）
  - 实时字数统计（标题/内容）
  - 多图上传、预览、删除功能
  - 草稿保存和恢复功能（本地存储）
  - 帖子预览功能
  - AI 助手可折叠，不干扰主流程
- ✅ AI 内容审核（发帖自动审核）
- ✅ 审核状态显示（审核中、已通过、内容违规、待人工审核）

### 管理功能

- ✅ 帖子管理（置顶、加精、删除）
- ✅ 板块管理
- ✅ 用户管理
- ✅ 举报审核处理
- ✅ 内容审核管理（人工审核）
- ✅ 审核历史记录
- ✅ 教务管理（学生信息、课程管理、成绩管理、请假管理）
- ✅ 数据统计分析（多维度统计、趋势图、分布图、排行榜）

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

## AI 搜索与总结功能

### AI 搜索

- **同步搜索**：等待完整结果返回
- **流式搜索**：使用 SSE 协议，实时增量显示结果
- **搜索流程**：先搜索相关帖子，再调用 AI 进行智能分析和回答

### AI 帖子总结

- 智能判断帖子内容长度，短内容提示无需总结
- 自动总结帖子正文和评论热点
- 支持异步加载，显示进度动画

***

## AI 写作助手功能

### 功能特性

- **预设操作按钮**：AI 帮写、AI 续写、AI 润色
  - AI 帮写：根据标题或简短描述自动生成完整的帖子内容
  - AI 续写：基于已有内容继续扩展
  - AI 润色：优化已有内容的表达，提升可读性

- **自定义指令**：支持用户输入自定义的 AI 指令

- **结果对比**：
  - 显示原始内容和 AI 生成内容的对比
  - 支持"采纳"AI 生成的内容
  - 支持"弃用"保留原始内容

- **可折叠界面**：AI 写作助手区域默认折叠，不干扰主要发布流程

### 工作流程

1. 点击"发布帖子"进入发布页
2. （可选）展开 AI 写作助手区域
3. 选择预设按钮或输入自定义指令
4. 点击"AI 生成"按钮
5. 查看 AI 生成结果，与原始内容对比
6. 选择"采纳"或"弃用"
7. 继续编辑或直接发布

### 草稿保存功能

- **自动保存**：编辑过程中每 60 秒自动保存草稿到本地
- **手动保存**：可随时点击"保存草稿"按钮手动保存
- **草稿恢复**：下次打开时，自动检测草稿并提示恢复
- **发布清理**：发布成功后自动清理草稿文件

### 优化的图片上传体验

- **多图预览**：网格布局显示已上传的图片
- **单图删除**：每个图片都有独立的删除按钮
- **上传进度**：清晰的上传状态提示

***

## AI 一键配图功能

### 功能特性

- **一键启动**：在发布帖子页面的"图片附件"区域点击"一键配图"按钮
- **智能提示词生成**：自动从帖子标题和内容中提取配图描述
- **可编辑提示词**：用户可以手动编辑或补充配图描述
- **多种图片尺寸**：支持 8 种预设图片尺寸选择
  - 1024x1024（默认）
  - 1280x1280
  - 1568x1056、1056x1568
  - 1472x1088、1088x1472
  - 1728x960、960x1728
- **实时进度条**：生成过程显示美观的进度条和阶段提示
  - 0-20%：正在连接 AI 服务...
  - 20-50%：AI 正在生成图片...
  - 50-80%：正在保存图片...
  - 80-95%：即将完成...
- **图片预览**：生成后显示清晰的图片预览
- **重新生成**：对结果不满意可以重新生成
- **一键添加**：确认后自动添加到帖子图片列表
- **本地保存**：生成的图片自动下载保存到服务器本地

### 使用流程

1. **进入发布页**：点击"发布帖子"或"编辑帖子"
2. **点击一键配图**：在"图片附件"区域点击"一键配图"按钮
3. **确认或编辑提示词**：检查自动生成的配图描述，可手动编辑
4. **选择尺寸**：从下拉菜单中选择想要的图片尺寸
5. **生成图片**：点击"生成图片"按钮，等待进度完成
6. **预览与确认**：查看生成结果，满意后点击"确认添加"
7. **图片已添加**：图片自动出现在预览区，继续编辑或发布

### 技术实现

- **后端**：
  - AiImageService：核心业务逻辑
  - AiImageController：API 入口
  - 扩展 BbsFileService：新增 downloadAndSaveImage 方法
- **前端**：
  - AiImageDialogController：配图对话框控制器
  - HttpRequestUtil：新增 aiImageGenerate 方法
  - 集成到 PostPublishController
- **图片流程**：
  1. 调用智谱 Cogview-3-Flash API 生成图片
  2. 获取图片 URL
  3. 下载图片到服务器本地
  4. 返回本地访问路径
  5. 前端预览并添加到帖子

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
| `bbs_report`         | 举报表           |
| `bbs_sensitive_word` | 敏感词表          |
| `bbs_moderation_log` | 审核历史记录表       |
| `student`            | 学生信息表         |
| `course`             | 课程表           |
| `score`              | 成绩表           |
| `student_leave`      | 学生请假表         |
| `menu_info`          | 菜单表           |
| `email_verification` | 邮箱验证表         |
| `statistics_day`     | 每日统计表         |

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
| POST | `/api/bbs/post/ai-write` | AI 写作助手（需要登录） |
| POST | `/api/bbs/ai-image/generate` | AI 图片生成（需要登录） |

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

| 方法   | 路径                                  | 说明       |
| ---- | ----------------------------------- | -------- |
| GET  | `/api/bbs/notification/unread-count` | 获取未读通知数  |
| GET  | `/api/bbs/notification/my-list`      | 获取我的通知列表 |
| POST | `/api/bbs/notification/{id}/read`    | 标记通知已读   |
| POST | `/api/bbs/notification/mark-all-read` | 全部标记已读 |

### 举报相关

| 方法   | 路径                           | 说明         |
| ---- | ---------------------------- | ---------- |
| POST | `/api/bbs/report`            | 提交举报       |
| GET  | `/api/bbs/report/my-list`    | 获取我的举报记录   |
| GET  | `/api/bbs/report/list`       | 获取举报列表（管理员） |
| POST | `/api/bbs/report/{id}/handle` | 处理举报（管理员） |

### 文件相关

| 方法   | 路径                        | 说明    |
| ---- | ------------------------- | ----- |
| POST | `/api/bbs/file/upload`     | 文件上传  |

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

| 方法   | 路径                                   | 说明                 |
| ---- | ------------------------------------ | ------------------ |
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

### 教务管理相关

| 方法   | 路径                              | 说明      |
| ---- | ------------------------------- | ------- |
| GET  | `/api/student/list`              | 学生列表    |
| POST | `/api/student`                  | 添加学生    |
| PUT  | `/api/student/{id}`              | 修改学生    |
| DELETE | `/api/student/{id}`              | 删除学生    |
| GET  | `/api/course/list`               | 课程列表    |
| POST | `/api/course`                   | 添加课程    |
| PUT  | `/api/course/{id}`               | 修改课程    |
| DELETE | `/api/course/{id}`               | 删除课程    |
| GET  | `/api/score/list`                | 成绩列表    |
| POST | `/api/score`                    | 添加成绩    |
| PUT  | `/api/score/{id}`                | 修改成绩    |
| DELETE | `/api/score/{id}`                | 删除成绩    |
| GET  | `/api/student-leave/list`        | 请假列表    |
| POST | `/api/student-leave`            | 申请请假    |
| PUT  | `/api/student-leave/{id}`        | 审批请假    |
| DELETE | `/api/student-leave/{id}`        | 删除请假    |

***

## 配置说明

### AI 审核配置

在 `application.yml` 中配置 AI 审核接口：

```yaml
# AI 内容审核配置
moderation:
  enabled: true  # 是否启用 AI 审核
  api:
    url: https://xplt.sdu.edu.cn:4000/v1/chat/completions
    key: your-api-key-here
    model: Ali-dashscope/Qwen3.5-Flash
    timeout: 30000  # 超时时间（毫秒）
```

### AI 图片生成配置

在 `application.yml` 中配置 AI 图片生成接口：

```yaml
# AI 图片生成配置
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

### v2.4 (2026-04-25) - AI 一键配图功能

**新增功能：**
- ✅ **AI 一键配图功能** - 在发布帖子页面集成智能配图生成
  - 自动从帖子标题和内容提取配图提示词
  - 支持 8 种预设图片尺寸选择（1024x1024、1280x1280 等）
  - 实时进度条提示，友好的生成阶段状态
  - 图片预览与重新生成功能
  - 一键确认添加到帖子图片列表
  - 图片自动下载保存到服务器本地
  - 使用智谱 AI Cogview-3-Flash 模型
- ✅ **配图对话框** - 美观的独立对话框设计
  - 尺寸选择下拉菜单
  - 可编辑的提示词文本区
  - 进度条显示区
  - 图片预览区
  - 确认/取消/重新生成按钮

**Bug 修复：**
- ✅ **修复对话框显示问题** - 使用 Stage 替代 Dialog，按钮显示正常
- ✅ **修复图片预览问题** - 使用 StackPane 布局，占位提示更友好
- ✅ **修复图片 URL 加载** - 正确处理相对路径与绝对路径

**技术优化：**
- 新增 AiImageConfig 配置类
- 新增 AiImageRequest 和 AiImageResponse DTO（前后端都有）
- 新增 AiImageService 服务类
- 新增 AiImageController 控制器
- 扩展 BbsFileService：新增 downloadAndSaveImage 方法
- 新增前端 AiImageDialogController
- 扩展 HttpRequestUtil：新增 aiImageGenerate 方法
- 完善的错误处理和用户提示
- 统一使用 DataRequest/DataResponse 包装格式
- 使用 Task 异步处理，不阻塞 UI
- 完善的日志输出便于调试

### v2.3 (2026-04-25) - AI 写作助手 + 发布页全面优化

**新增功能：**
- ✅ **AI 写作助手** - 在发布帖子页面集成强大的 AI 写作助手
  - 三个预设按钮：AI 帮写、AI 续写、AI 润色
  - 支持自定义 AI 指令输入
  - AI 生成结果与原始内容对比显示
  - 提供"采纳"或"弃用"选择，用户可灵活决定
  - AI 助手区域可折叠，默认隐藏不干扰主流程
- ✅ **发布页面全面优化**
  - 使用 TitledPane 重新组织布局，区域划分清晰
  - 分为：基本信息、帖子内容、AI 写作助手、图片附件
  - 优化控件间距和视觉层次
- ✅ **实时字数统计**
  - 标题和内容都显示实时字数统计
  - 格式：当前字数/最大限制
  - 超过限制时显示红色警告提示
- ✅ **多图上传管理优化**
  - 网格布局预览多张图片
  - 每张图片都有独立的删除按钮
  - 上传过程显示状态提示
  - 图片 URL 管理更直观（隐藏式管理）
- ✅ **本地草稿保存功能**
  - 自动定期保存（每 60 秒）
  - 支持手动保存草稿按钮
  - 下次打开时自动检测并提示恢复草稿
  - 发布成功后自动清理草稿文件
  - 草稿保存在用户本地目录（~/.trae_bbs/）
- ✅ **帖子预览功能**
  - 添加"预览"按钮
  - 显示帖子的实际发布效果预览
  - 包含标题、板块、内容和图片数量
- ✅ **AI 生成进度条** - 在 AI 生成内容时显示美观的进度条
  - 模仿浏览器加载效果，分阶段推进
  - 4 个阶段的状态提示：
    - 0-20%：正在连接 AI 服务...
    - 20-50%：AI 正在思考中...
    - 50-80%：正在生成内容...
    - 80-95%：即将完成...
  - 成功时快速走到 100%，显示"完成！"
  - 使用橙色进度条与 AI 生成按钮颜色一致
  - 添加 25px 高度确保清晰可见
  - 使用后台线程确保稳定运行

**Bug 修复：**
- ✅ **修复 AI 写作接口数据传递问题** - 后端接收 DataRequest 包装格式的数据
  - 从直接接收 AiWriteRequest 改为接收 DataRequest
  - 从 data 字段中解析 title、content、instruction、operation

**技术优化：**
- 新增 AiWriteService 服务类，处理 AI 写作逻辑
- 新增 AiWriteRequest 和 AiWriteResponse DTO
- 新增前端 AiWriteResponse 模型
- 完善的错误处理和用户提示
- AI 进度条：使用后台线程 + Platform.runLater 确保 UI 线程安全
- AI 进度条：预估总时间 6 秒，按时间比例推进进度条
- 使用 JavaFX Task 异步处理 AI 调用，不阻塞 UI
- 增强后端日志输出，便于调试

### v2.2 (2026-04-24) - AI 搜索与总结功能 + Bug 修复

**新增功能：**
- ✅ AI 搜索接口重构，增加流式 API (`/api/bbs/post/ai-search-stream`)
- ✅ 后端支持 Server-Sent Events (SSE) 协议，流式调用 AI API
- ✅ 前端支持流式 UI 更新，实时显示 AI 生成内容
- ✅ 保持原有非流式接口 (`/api/bbs/post/ai-search`) 向后兼容
- ✅ **AI 帖子总结功能** - 在帖子详情页新增 AI 智能总结按钮
  - 智能判断内容长度，短内容提示无需总结
  - 自动总结帖子内容和评论热点
  - 支持异步加载，显示进度动画
  - 完善的错误处理和用户提示
- ✅ 后端新增 ContentSummaryService 和相关 API
- ✅ 前端新增 AI 总结 UI 组件

**Bug 修复：**
- ✅ **帖子详情页异常滚动/闪现问题** - 评论区或正文过长导致发送功能区消失
  - 移除了初始化中的事件过滤器，避免干扰布局
  - 使用三重 Platform.runLater 确保布局稳定后，强制焦点设置到 ScrollPane 并滚动到顶部
  - 彻底解决了点击页面闪现到底部的问题
- ✅ **帖子正文过长导致功能区不可见** - 给内容区域包裹 ScrollPane 并设置最大高度 500 像素
  - 确保点赞、评论、收藏等功能区始终可见
  - 正文过长时可独立滚动，不影响整体布局

### v2.1 (2026-04-24) - 数据统计功能完善与优化

**新增功能：**
- ✅ 数据统计功能丰富（数据概览、用户统计、内容统计、互动统计、内容安全统计）
- ✅ 多维度数据趋势图表（用户增长、发帖趋势、点赞趋势、关注趋势）
- ✅ 饼图分布统计（用户类型分布、板块分布、帖子状态分布、违规类型分布）
- ✅ 热门内容排行榜（活跃用户、热门帖子、热门评论）
- ✅ 审核统计概览卡片（总审核数、待审核数、通过率、拒绝率）
- ✅ 举报统计卡片（总举报、待处理、举报处理率、有效举报）

**布局优化：**
- ✅ 互动统计页面重构（去掉收藏趋势图表，保留点赞和关注趋势）
- ✅ 热门评论表格跨两列显示，更美观

**Bug 修复：**
- ✅ 修复用户类型分布名称不匹配（1-超级管理员、2-管理员、3-普通用户）
- ✅ 重新设计帖子状态分布（审核通过、审核中、违规，去掉下架状态）
- ✅ 修复审核通过率和拒绝率保留两位小数
- ✅ 修复举报统计卡片格式化显示
- ✅ 修复违规类型中英文混杂问题
- ✅ 修复线图线条颜色与端点颜色不对应
- ✅ 修复数据概览关键指标使用整数显示
- ✅ 修复帖子状态分布布尔类型值安全转换
- ✅ 修复审核趋势线图图例颜色正确显示
- ✅ 修复收藏趋势图表加载问题
- ✅ 修复举报处理率百分比格式化

**技术优化：**
- 统计数据类型安全转换
- 图表颜色与图例同步
- 百分比格式化统一处理
- 数据概览整数显示优化

### v2.0 (2026-04-24) - AI 内容审核功能

**新增功能：**
- ✅ AI 内容审核集成
- ✅ 发帖自动触发 AI 审核
- ✅ 编辑帖子重新触发 AI 审核
- ✅ 审核状态显示（审核中、已通过、内容违规、待人工审核）
- ✅ 审核结果通知用户
- ✅ 管理员内容审核管理界面
- ✅ 审核历史记录查看
- ✅ 违规帖子仅显示编辑/删除按钮
- ✅ 智能搜索功能（标题搜索、全文搜索）
- ✅ 搜索结果高亮显示（匹配内容红色加粗）
- ✅ 审核日志审计

**数据库变更：**
- 扩展 `bbs_post` 表，添加审核相关字段
- 新增 `bbs_moderation_log` 表（审核历史记录）
- 添加内容审核菜单项

**技术优化：**
- 使用 @Async 异步处理审核任务
- 事务同步机制确保数据一致性
- 完善的调试日志输出
- 搜索功能优化（LIKE 模糊匹配）

### v1.x (之前版本)

- 修复了数据加载问题（帖子详情、评论列表、板块列表、通知列表）
- 完善了 Comment 模型字段（imageUrls、likeCount、status）
- 完善了 Board 模型字段（createTime、updateTime）
- 修复了 Notification 模型的序列化问题
- 增强了调试日志输出
- 用户关注、收藏功能
- 评论图片功能
- 个人中心完善
- 隐私设置
- 系统统计
- 邮箱验证注册功能
- 评论点赞功能

***

## 贡献指南

欢迎提交 Issue 和 Pull Request！

***

## 许可证

本项目仅供学习交流使用。

***

## 联系方式

如有问题，请提交 Issue。
