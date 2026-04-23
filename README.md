# 校园论坛系统

一个功能完整的校园论坛系统，采用前后端分离架构。

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

### 架构特点

- ✅ 前后端分离架构
- ✅ RESTful API 设计
- ✅ JWT 身份认证
- ✅ 基于角色的权限控制
- ✅ 完整的 BBS 功能模块

---

## 功能特性

### 用户功能

- ✅ 用户注册与登录
- ✅ 记住密码功能
- ✅ 个人资料编辑
- ✅ 头像、昵称、个性签名
- ✅ 密码修改
- ✅ 个人中心（我的帖子、我的收藏、我的关注、我的举报）
- ✅ 通知消息查看

### 论坛功能

- ✅ 板块列表与导航
- ✅ 帖子浏览与分页
- ✅ 帖子发布、编辑、删除
- ✅ 图片上传支持
- ✅ 评论与回复（支持嵌套回复）
- ✅ 帖子点赞与收藏
- ✅ 用户关注功能
- ✅ 帖子举报
- ✅ 通知系统

### 管理功能

- ✅ 帖子管理（置顶、加精、删除）
- ✅ 板块管理
- ✅ 用户管理
- ✅ 举报审核处理
- ✅ 数据统计分析

---

## 快速开始

### 环境要求

- JDK 21
- Maven 3.8+
- MySQL 8.0+
- （可选）Redis（后端配置中有，但不是必须的）

### 后端部署

1. **克隆项目**

```bash
git clone <your-repository-url>
cd java-curdeg-0422
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

---

## 项目结构

```
java-curdeg-0422/
├── java-fx/                          # 前端 JavaFX 项目
│   ├── src/main/java/com/teach/javafx/
│   │   ├── controller/               # 控制器层
│   │   │   ├── base/                 # 基础控制器
│   │   │   └── ...                   # 业务控制器
│   │   ├── models/                   # 数据模型
│   │   ├── request/                  # HTTP 请求工具
│   │   └── util/                     # 工具类
│   └── src/main/resources/
│       └── com/teach/javafx/         # FXML 界面文件
│
├── java-server/                      # 后端 Spring Boot 项目
│   └── src/main/java/cn/edu/sdu/java/server/
│       ├── configs/                  # 配置类
│       ├── controllers/              # 控制器层
│       ├── services/                 # 服务层
│       ├── repositorys/              # 数据访问层
│       ├── models/                   # 实体模型
│       └── util/                     # 工具类
│
└── 需求分析与可行性分析.md           # 项目需求文档
```

---

## 数据库设计

### 核心数据表

| 表名 | 说明 |
|------|------|
| `user` | 用户表（包含社区相关字段） |
| `user_type` | 用户类型表 |
| `person` | 人员信息表 |
| `bbs_board` | 板块表 |
| `bbs_post` | 帖子表 |
| `bbs_comment` | 评论表 |
| `bbs_follow` | 用户关注表 |
| `bbs_notification` | 通知表 |
| `bbs_report` | 举报表 |
| `bbs_sensitive_word` | 敏感词表 |
| `student` | 学生信息表 |
| `course` | 课程表 |
| `score` | 成绩表 |

---

## API 接口文档

### 认证相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/change-password` | 修改密码 |

### 论坛相关

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/bbs/board/list` | 获取板块列表 |
| POST | `/api/bbs/board` | 创建板块（管理员） |
| PUT | `/api/bbs/board/{id}` | 修改板块（管理员） |
| DELETE | `/api/bbs/board/{id}` | 删除板块（管理员） |
| GET | `/api/bbs/post/list` | 获取帖子列表 |
| GET | `/api/bbs/post/{id}` | 获取帖子详情 |
| POST | `/api/bbs/post` | 发布帖子 |
| PUT | `/api/bbs/post/{id}` | 修改帖子 |
| DELETE | `/api/bbs/post/{id}` | 删除帖子 |
| POST | `/api/bbs/post/{id}/top` | 置顶帖子（管理员） |
| POST | `/api/bbs/post/{id}/feature` | 加精帖子（管理员） |
| GET | `/api/bbs/post/{id}/like` | 获取点赞状态 |
| POST | `/api/bbs/post/{id}/like` | 点赞/取消点赞 |
| GET | `/api/bbs/post/{id}/favorite` | 获取收藏状态 |
| POST | `/api/bbs/post/{id}/favorite` | 收藏/取消收藏 |
| GET | `/api/bbs/comment/list/{postId}` | 获取评论列表 |
| POST | `/api/bbs/comment` | 发布评论 |
| PUT | `/api/bbs/comment/{id}` | 修改评论 |
| DELETE | `/api/bbs/comment/{id}` | 删除评论 |
| GET | `/api/bbs/comment/{id}/like` | 评论点赞状态 |
| POST | `/api/bbs/comment/{id}/like` | 评论点赞/取消点赞 |
| GET | `/api/bbs/follow/check` | 检查关注状态 |
| POST | `/api/bbs/follow` | 关注/取消关注 |
| GET | `/api/bbs/follow/followers/{userId}` | 获取粉丝列表 |
| GET | `/api/bbs/follow/following/{userId}` | 获取关注列表 |
| GET | `/api/bbs/notification/unread-count` | 获取未读通知数 |
| GET | `/api/bbs/notification/my-list` | 获取我的通知列表 |
| POST | `/api/bbs/notification/{id}/read` | 标记通知已读 |
| POST | `/api/bbs/notification/mark-all-read` | 全部标记已读 |
| POST | `/api/bbs/report` | 提交举报 |
| GET | `/api/bbs/report/my-list` | 获取我的举报记录 |
| GET | `/api/bbs/report/list` | 获取举报列表（管理员） |
| POST | `/api/bbs/report/{id}/handle` | 处理举报（管理员） |
| POST | `/api/bbs/file/upload` | 文件上传 |
| GET | `/api/statistics/summary` | 系统统计数据 |
| GET | `/api/statistics/user` | 用户统计数据 |

---

## 权限说明

### 用户角色

| 角色 | 权限范围 |
|------|----------|
| ROLE_STUDENT | 普通学生：发帖、评论、点赞、收藏等基础功能 |
| ROLE_ADMIN | 管理员：学生功能 + 帖子管理、举报处理等 |
| ROLE_SUPER | 超级管理员：管理员功能 + 板块管理、用户管理、系统配置 |

---

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

---

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

---

## 更新日志

### 最近更新

- 修复了数据加载问题（帖子详情、评论列表、板块列表、通知列表）
- 完善了 Comment 模型字段（imageUrls、likeCount、status）
- 完善了 Board 模型字段（createTime、updateTime）
- 修复了 Notification 模型的序列化问题
- 增强了调试日志输出

---

## 贡献指南

欢迎提交 Issue 和 Pull Request！

---

## 许可证

本项目仅供学习交流使用。

---

## 联系方式

如有问题，请提交 Issue。
