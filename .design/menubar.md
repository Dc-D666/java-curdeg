# 顶部菜单栏重新设计需求文档

## 1. 概述

本项目为校园论坛系统（学生交流社区），采用前后端分离架构（前端 JavaFX 21 + 后端 Spring Boot 3.2.5）。当前界面包含两个导航区域：

| 区域 | 位置 | 当前内容 |
|------|------|----------|
| **菜单栏** | 顶部（MenuBar） | 首页、工具、我的、切换账号 |
| **导航栏** | 左侧（TreeView） | 校园论坛、个人中心、系统管理（动态加载） |

导航栏已经承载了大部分功能入口（帖子广场、个人中心、系统管理等），因此菜单栏应重新定位为**全局操作入口**，避免与导航栏功能重复。

---

## 2. 当前菜单栏存在的问题

| 菜单项 | 子菜单项 | 问题描述 |
|--------|----------|----------|
| **首页** | 帖子列表 | 与导航栏"帖子广场"重复，仅一个子项层级过深 |
| **工具** | 新建、保存、删除、打印、导入、导出 | 通用框架功能（继承自 ToolController），与论坛业务无关 |
| **我的** | 退出登录 | 名不副实，仅有一个退出功能 |
| **切换账号** | super、admin1、admin2、user1、user2（动态） | 开发调试功能，但**必须完整保留** |

---

## 3. 设计目标

1. **菜单栏与导航栏职责分离**：菜单栏负责全局操作，导航栏负责功能导航
2. **精简菜单项数量**：从 4 个增加到 5 个（含切换账号），每个菜单下有 1-3 个子项
3. **角色差异化显示**："导出"菜单仅对管理员（ROLE_SUPER / ROLE_ADMIN）可见
4. **严格保留"切换账号"**：此菜单用于开发和测试环境快速切换用户，必须原样保留
5. **新增功能**：导出数据、关于、反馈与建议

---

## 4. 新菜单栏结构

```
┌─────────────────────────────────────────────────────────────────────┐
│   首页    📤 导出    ️ 设置    ❓ 帮助     切换账号              │
└─────────────────────────────────────────────────────────────────────┘
```

### 4.1 首页

| 子菜单 | 功能 | 触发事件 | 说明 |
|--------|------|----------|------|
| 帖子广场 | 打开帖子列表页 | `onPostSquareClick` | 快捷回到首页，调用 `changeContent("post-list", "帖子广场")` |

**实现方式**：
- 在 `MainFrameController` 中新增 `onPostSquareClick()` 方法
- 调用 `changeContent("post-list", "帖子广场")`

---

### 4.2 导出（仅管理员可见）

| 子菜单 | 功能 | 触发事件 | 说明 |
|--------|------|----------|------|
| 导出帖子数据 | 导出帖子广场数据 | `onExportPostsClick` | 核心需求，详见第 5 节 |
| 导出用户数据 | 导出用户列表数据 | `onExportUsersClick` | 新增 |
| 导出统计报表 | 导出系统统计数据 | `onExportStatsClick` | 新增 |

**角色控制**：
- 仅对 `ROLE_SUPER` 和 `ROLE_ADMIN` 角色可见
- 使用 `menuExport.setVisible(false)` 隐藏，通过角色判断动态显示

**实现方式**：
```java
// MainFrameController.initialize() 中
String role = AppStore.getJwt().getRole();
boolean isAdmin = "ROLE_SUPER".equals(role) || "ROLE_ADMIN".equals(role);
menuExport.setVisible(isAdmin);
```

**后端 API**：
- `GET /api/admin/export/posts` - 导出帖子数据（管理员）
- `GET /api/admin/export/users` - 导出用户数据（管理员）
- `GET /api/admin/export/statistics` - 导出统计报表（管理员）

---

### 4.3 设置

| 子菜单 | 功能 | 触发事件 | 说明 |
|--------|------|----------|------|
| 个人资料 | 跳转到个人资料页 | `onProfileClick` | 快捷入口，调用 `changeContent("personal-center", "个人中心")` |
| 修改密码 | 跳转到修改密码页 | `onChangePasswordClick` | 快捷入口，调用 `changeContent("password-change", "修改密码")` |
| ——— | 分隔线 | — | — |
| 退出登录 | 退出当前账号 | `onLogoutClick` | 从原"我的"菜单迁移至此 |

**实现方式**：
- `onProfileClick()` 和 `onChangePasswordClick()` 在 `MainFrameController` 中新增
- `onLogoutClick()` 从原代码迁移，保持逻辑不变

---

### 4.4 帮助

| 子菜单 | 功能 | 触发事件 | 说明 |
|--------|------|----------|------|
| 关于 | 打开关于对话框 | `onAboutClick` | 展示项目信息，详见第 6 节 |
| 反馈与建议 | 打开反馈表单对话框 | `onFeedbackClick` | 提交 Bug/建议，详见第 7 节 |

---

### 4.5 切换账号（必须完整严格保留）

> ⚠️ **此菜单是开发和调试环境的必备功能，必须原样保留，不可修改、不可删除、不可合并。**

| 子菜单 | 功能 | 说明 |
|--------|------|------|
| super | 切换到超级管理员账号 | 动态生成 |
| admin1 | 切换到管理员账号 1 | 动态生成 |
| admin2 | 切换到管理员账号 2 | 动态生成 |
| user1 | 切换到学生账号 1 | 动态生成 |
| user2 | 切换到学生账号 2 | 动态生成 |

**当前实现逻辑**（来自 `main-frame.fxml`）：
- 菜单 ID：`menuSwitchAccount`
- 子菜单通过代码动态生成
- 点击后调用切换账号逻辑（具体方法名待确认）

**要求**：
1. 菜单名称保持"切换账号"不变
2. 子菜单项动态生成的逻辑不变
3. 切换账号的事件处理逻辑不变
4. 菜单在菜单栏中的位置可以调整（建议放在最后）
5. 此菜单对所有角色可见（包括学生用户）

---

## 5. 导出数据功能详细需求

### 5.1 导出帖子数据

**对话框设计**：

```
┌─────────────────────────────────────────────────────────────────────┐
│  导出帖子数据                                                     ✕ │
─────────────────────────────────────────────────────────────────────
│                                                                     │
│  导出范围：  ○ 全部帖子   ○ 指定板块   ○ 指定时间范围   ○ 指定用户 │
│                                                                     │
│  ── 指定板块 ──                                                     │
│  板块：     [下拉选择框]                                           │
│                                                                     │
│  ── 指定时间范围 ──                                                 │
│  开始日期： [日期选择器]        结束日期： [日期选择器]              │
│                                                                     │
│  ── 指定用户 ──                                                     │
│  用户ID：   [文本输入框]                                           │
│                                                                     │
│  ─ 导出字段 ──                                                     │
│  ☑ 帖子ID   ☑ 标题   ☑ 内容   ☑ 作者   ☑ 板块   ☑ 点赞数   ☑ 评论数 │
│   创建时间 ☑ 更新时间 ☑ 状态（置顶/加精/正常）                     │
│                                                                     │
│  ── 导出格式 ──                                                     │
│  ○ Excel (.xlsx)   ○ CSV (.csv)   ○ JSON (.json)                   │
│                                                                     │
│  ── 其他选项 ──                                                     │
│  ☑ 包含已删除的帖子                                                  │
│  ○ 分页导出（每页 1000 条）                                         │
│                                                                     │
│                                    [取消]          [导出]           │
└─────────────────────────────────────────────────────────────────────┘
```

**API 设计**：

```
POST /api/admin/export/posts
请求参数：
{
  "scope": "ALL | BOARD | DATE_RANGE | USER",
  "boardId": 1,          // scope=BOARD 时必填
  "startDate": "2026-01-01",  // scope=DATE_RANGE 时必填
  "endDate": "2026-12-31",
  "userId": 123,         // scope=USER 时必填
  "fields": ["id", "title", "content", "authorId", "boardId", "likeCount", "commentCount", "createTime", "updateTime", "isTop", "isFeatured", "status"],
  "format": "EXCEL | CSV | JSON",
  "includeDeleted": false,
  "pageSize": 1000       // 分页时必填
}
响应：
{
  "code": 0,
  "data": "base64_encoded_file_content",
  "fileName": "posts_export_20260517.xlsx",
  "msg": "导出成功"
}
```

**权限**：`ROLE_SUPER` 或 `ROLE_ADMIN`

---

### 5.2 导出用户数据

**对话框设计**：简化版，主要选择导出格式和用户范围。

**API 设计**：`POST /api/admin/export/users`

**权限**：`ROLE_SUPER` 或 `ROLE_ADMIN`

---

### 5.3 导出统计报表

**对话框设计**：选择统计维度（日/周/月）、时间范围、导出格式。

**API 设计**：`POST /api/admin/export/statistics`

**权限**：`ROLE_SUPER` 或 `ROLE_ADMIN`

---

## 6. "关于"对话框详细需求

### 6.1 显示内容

| 字段 | 内容 | 来源 |
|------|------|------|
| 项目名称 | 学生交流社区（校园论坛系统） | 固定 |
| 版本号 | v2.7 | 从 README.md 或常量获取 |
| 项目简介 | 基于 JavaFX + Spring Boot 的校园论坛系统，支持 AI 智能搜索、写作助手、一键配图等功能 | 固定 |
| GitHub | 可点击的超链接 | 固定（待填入实际 URL） |
| 技术栈 | 前端：Java 21 + JavaFX 21 + Gson<br>后端：Spring Boot 3.2.5 + Spring Data JPA + Spring Security + JWT + MySQL | 固定 |
| AI 功能 | 智能搜索（SSE 流式）、写作助手、一键配图、内容审核、帖子总结 | 固定 |
| 开发团队 | 团队成员列表 | 从底部状态栏获取 |
| 开源协议 | MIT License | 固定 |

### 6.2 对话框布局

```
─────────────────────────────────────────────────────────────────────
│  关于                                                            ✕ │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │                           🏫                                │   │
│  │                                                             │   │
│  │                    学生交流社区                              │   │
│  │                 校园论坛系统 v2.7                            │   │
│  │                                                             │   │
│  │      基于 JavaFX + Spring Boot 的校园论坛系统                  │   │
│  │   支持 AI 智能搜索、写作助手、一键配图、内容审核等功能         │   │
│  │                                                             │   │
│  │      [🔗 GitHub 仓库]                                       │   │
│  │                                                             │   │
│  │      技术栈：                                               │   │
│  │      前端：Java 21 + JavaFX 21 + Gson                       │   │
│  │      后端：Spring Boot 3.2.5 + JPA + MySQL                   │   │
│  │                                                             │   │
│  │      开发团队：                                              │   │
│  │      [团队成员列表]                                          │   │
│  │                                                             │   │
│  │      MIT License © 2026                                     │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│                                                   [确定]           │
─────────────────────────────────────────────────────────────────────
```

---

## 7. "反馈与建议"对话框详细需求

### 7.1 表单字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 反馈类型 | 下拉选择 | 是 | Bug 报告 / 功能建议 / 体验优化 / 其他 |
| 标题 | 文本输入 | 是 | 简短描述（最多 50 字符） |
| 详细描述 | 多行文本 | 是 | 详细描述问题或建议（最多 2000 字符） |
| 联系方式 | 文本输入 | 否 | 邮箱或微信号，方便后续沟通 |

### 7.2 对话框布局

```
┌─────────────────────────────────────────────────────────────────────┐
│  反馈与建议                                                       ✕ │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  反馈类型：  [下拉选择框 ▼]                                        │
│            ○ Bug 报告  ○ 功能建议  ○ 体验优化  ○ 其他               │
│                                                                     │
│  标题：      [文本输入框_________________________________]  (0/50)  │
│                                                                     │
│  详细描述：  [多行文本框]                                           │
│             ____________________________________________________    │
│             ____________________________________________________    │
│             ____________________________________________________    │
│             ____________________________________________________    │
│             ____________________________________________________    │
│                                                      (0/2000)       │
│                                                                     │
│  联系方式：  [文本输入框_________________________________] (可选)   │
│                                                                     │
│                                    [取消]          [提交]           │
└─────────────────────────────────────────────────────────────────────┘
```

### 7.3 提交方式（两种方案）

**方案 A：提交到后端数据库（推荐）**

```
POST /api/bbs/feedback
请求参数：
{
  "type": "BUG | FEATURE | EXPERIENCE | OTHER",
  "title": "帖子搜索功能无法搜索中文",
  "description": "在帖子搜索页面输入中文关键词后，无法返回任何结果...",
  "contact": "user@example.com"
}
响应：
{
  "code": 0,
  "data": { "id": 1 },
  "msg": "反馈提交成功"
}
```

管理员在后台（新增"反馈管理"页面）查看和处理反馈。

**方案 B：调用 GitHub API 创建 Issue**

```
POST https://api.github.com/repos/{owner}/{repo}/issues
Headers:
  Authorization: Bearer {GITHUB_TOKEN}
Body:
{
  "title": "[Bug] 帖子搜索功能无法搜索中文",
  "body": "**类型**: Bug 报告\n\n**描述**:\n在帖子搜索页面输入中文关键词后...",
  "labels": ["bug"]
}
```

**要求**：默认使用方案 A，方案 B 作为可选配置。

---

## 8. 修改文件清单

### 8.1 FXML 文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `base/main-frame.fxml` | 修改 | 重写 `<MenuBar>` 部分 |

### 8.2 Java 控制器文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `controller/base/MainFrameController.java` | 修改 | 新增菜单事件处理方法，添加角色判断逻辑 |

### 8.3 新增文件

| 文件 | 说明 |
|------|------|
| `dialog/about-dialog.fxml` | 关于对话框 |
| `dialog/about-dialog-controller.java` | 关于对话框控制器 |
| `dialog/feedback-dialog.fxml` | 反馈与建议对话框 |
| `dialog/feedback-dialog-controller.java` | 反馈对话框控制器 |
| `dialog/export-posts-dialog.fxml` | 导出帖子数据对话框 |
| `dialog/export-posts-dialog-controller.java` | 导出对话框控制器 |

### 8.4 后端 API（新增）

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/admin/export/posts` | POST | 管理员 | 导出帖子数据 |
| `/api/admin/export/users` | POST | 管理员 | 导出用户数据 |
| `/api/admin/export/statistics` | POST | 管理员 | 导出统计报表 |
| `/api/bbs/feedback` | POST | 认证用户 | 提交反馈 |
| `/api/admin/feedback/list` | GET | 管理员 | 查看反馈列表 |
| `/api/admin/feedback/{id}/handle` | POST | 管理员 | 处理反馈 |

---

## 9. 最终菜单栏结构

```xml
<MenuBar>
    <!-- 首页 -->
    <Menu text="首页" fx:id="menuHome">
        <MenuItem text="帖子广场" onAction="#onPostSquareClick" />
    </Menu>

    <!-- 导出（管理员可见） -->
    <Menu text="导出" fx:id="menuExport" visible="false">
        <MenuItem text="导出帖子数据" onAction="#onExportPostsClick" />
        <MenuItem text="导出用户数据" onAction="#onExportUsersClick" />
        <MenuItem text="导出统计报表" onAction="#onExportStatsClick" />
    </Menu>

    <!-- 设置 -->
    <Menu text="设置" fx:id="menuSettings">
        <MenuItem text="个人资料" onAction="#onProfileClick" />
        <MenuItem text="修改密码" onAction="#onChangePasswordClick" />
        <SeparatorMenuItem />
        <MenuItem text="退出登录" onAction="#onLogoutClick" />
    </Menu>

    <!-- 帮助 -->
    <Menu text="帮助" fx:id="menuHelp">
        <MenuItem text="关于" onAction="#onAboutClick" />
        <MenuItem text="反馈与建议" onAction="#onFeedbackClick" />
    </Menu>

    <!-- 切换账号（必须完整严格保留） -->
    <Menu text="切换账号" fx:id="menuSwitchAccount">
        <!-- 子菜单动态生成，保持原有逻辑不变 -->
    </Menu>
</MenuBar>
```

---

## 10. 角色权限矩阵

| 菜单 | ROLE_SUPER | ROLE_ADMIN | ROLE_STUDENT |
|------|:----------:|:----------:|:------------:|
| 首页 | ✓ | ✓ | ✓ |
| 导出 | ✓ | ✓ |  |
| 设置 | ✓ | ✓ | ✓ |
| 帮助 | ✓ | ✓ | ✓ |
| 切换账号 | ✓ | ✓ | ✓ |

---

## 11. 待确认事项

1. **GitHub 仓库地址**：需要填入实际的 GitHub 仓库 URL（当前仓库为私有，需确认公开后的地址）
2. **团队成员列表**：从底部状态栏获取的团队成员信息格式需要确认
3. **版本号来源**：是从 README.md 读取还是使用硬编码常量
4. **导出功能优先级**：先实现"导出帖子数据"还是三个导出功能一起实现
5. **反馈提交方式**：优先使用方案 A（后端数据库）还是方案 B（GitHub API）
