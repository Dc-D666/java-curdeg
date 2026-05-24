# 通知页面重新设计文档

## 1. 问题分析

### 1.1 当前页面存在的问题

| 问题 | 描述 | 影响 |
|------|------|------|
| **信息密度过高** | 使用 TableView 平铺展示所有通知，无视觉层次 | 用户难以快速扫描和定位重要信息 |
| **视觉风格单一** | 纯白底表格，类型列大量显示"未知" | 界面单调乏味，缺乏品牌感 |
| **未读标识不明显** | 仅用粗体字区分，选中后变红色 | 不够直观，容易忽略 |
| **无类型图标** | 6种通知类型纯文字展示，无视觉区分 | 需要逐行阅读才能识别类型 |
| **无分组/聚合** | 所有通知平铺展示，无日期分组 | 大量通知时难以浏览 |
| **详情操作繁琐** | 双击打开 Dialog 弹窗查看详情 | 操作路径长，不够便捷 |
| **类型过滤选项文案不一致** | 下拉框使用"回复我的"而实际类型为"评论回复通知" | 用户认知混淆 |
| **内容截断** | 长文本在表格中被截断，无法完整阅读 | 需要额外操作才能看全 |
| **缺少加载状态反馈** | 刷新/加载时无进度提示 | 用户不知道是否在加载 |
| **缺少空状态提示** | 无通知时显示空白页面 | 用户不确定是没数据还是加载失败 |

### 1.2 通知类型分析

后端返回的 6 种通知类型及其特征：

| 类型 | 编码 | 图标 | 关键信息 | 颜色主题 |
|------|------|------|----------|----------|
| 系统通知 | 1 | 🔔 | 标题 + 内容 | 蓝色 |
| 举报处理通知 | 2 | 🛡️ | 处理结果 + 关联内容 | 橙色 |
| 帖子审核通知 | 3 | 📄 | 帖子名 + 审核结果（通过/未通过/审核中） | 绿色/红色/灰色 |
| 评论回复通知 | 4 | 💬 | 评论者 + 帖子名 | 青色 |
| 新增粉丝通知 | 5 | 👤 | 粉丝昵称 | 紫色 |
| 关注用户发帖通知 | 6 | ✏️ | 关注用户 + 帖子名 | 靛蓝 |

### 1.3 现有 API 与模型限制（重要）

#### Notification 模型字段

```java
// com.teach.javafx.models.Notification
private Long id;            // 通知ID
private Long receiverId;    // 接收者ID
private Integer type;       // 类型（1-6）
private String title;       // 标题
private String content;     // 内容
private Integer isRead;     // 是否已读（0=未读, 1=已读）
private String createTime;  // 创建时间（字符串格式，如 "2026-05-17 15:12:00"）
```

**限制：模型不包含头像 URL、发帖人昵称、帖子标题等关联数据，如需此类信息须从 content 文本中解析。**

#### HttpRequestUtil 通知相关 API

| 方法 | 端点 | 参数 | 返回值 | 备注 |
|------|------|------|--------|------|
| `getUnreadNotificationCount()` | GET `/api/bbs/notification/unread-count` | 无 | `Long` | 获取未读数量 |
| `getMyNotificationList(isRead, type)` | GET `/api/bbs/notification/my-list` | `isRead`, `type` | `List<Notification>` | **无分页参数**，一次返回全部 |
| `readNotification(id)` | POST `/api/bbs/notification/{id}/read` | 通知ID | `boolean` | 标记单条已读 |
| `markAllNotificationsAsRead()` | POST `/api/bbs/notification/mark-all-read` | 无 | `boolean` | 全部标记已读 |

**关键约束：**
- 后端 **无分页支持**，列表一次性加载全部，前端须通过虚拟滚动或虚拟列表优化性能
- 后端 **无删除通知接口**，不可设计删除功能
- 后端 **不支持排序参数**，排序须在前端完成
- 所有请求需 JWT Bearer Token 认证

## 2. 设计方案

### 2.1 整体布局

```
──────────────────────────────────────────────────────────────────────────
│  🔔 我的通知                                          (53条未读)       │
├──────────────────────────────────────────────────────────────────────────
│  [全部类型 ▼]  [默认排序 ▼]  [全部已读]  [刷新]                         │
├──────────────────────────────────────────────────────────────────────────
│                                                                          │
│  今天 (5月17日)                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ 🔵 📄 帖子审核通知                          [✓ 通过]      · 15:12  │ │
│  │    您的帖子「寻找饭友，一起探索校园美食」审核通过，快去查看吧！        │ │
│  │                                              [标记未读] [查看原帖] │ │
│  ├────────────────────────────────────────────────────────────────────┤ │
│  │ 🟣 👤 新增粉丝通知                                    · 11:39      │ │
│  │    待测电阻 关注了你                                                 │ │
│  │                                              [标记未读]           │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  昨天 (5月16日)                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │ 🔵 💬 评论回复通知                                    · 23:45      │ │
│  │    不吃香菜 评论了您的帖子「论坛频道用户行为规...                     │ │
│  │                                              [标记未读] [查看原帖] │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  更早                                                                    │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │    🛡️ 举报处理通知                                    · 5月14日    │ │
│  │    您举报的帖子已处理，处理结果：已删除违规内容                       │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ────────────────────────────────────────────────────────────────────   │
│                        共 128 条通知 · 虚拟滚动                          │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────
```

### 2.2 核心设计要素

#### 2.2.1 通知卡片设计

每个通知以卡片形式展示，包含以下元素：

| 元素 | 未读状态 | 已读状态 |
|------|----------|----------|
| 卡片背景 | 白色 `#ffffff` | 浅灰 `#fafafa` |
| 左侧标识 | 3px 蓝色竖条 | 无 |
| 标题文字 | 加粗深色 | 常规灰色 |
| 内容预览 | 正常显示 | 透明度 70% |

**卡片布局：**
- **左侧**：类型图标圆形背景（32x32px），内部显示 Emoji 图标
- **中间上方**：通知类型文字 + 审核结果标签（仅类型3显示）+ 右侧时间
- **中间下方**：内容预览（最多2行，超出用"..."截断）
- **悬停/展开时底部**：操作按钮（标记已读/未读、查看原帖）

#### 2.2.2 审核结果颜色标识

帖子审核通知（type=3）根据 content 内容提取审核结果并展示标签：

| 审核结果 | 标签文本 | 背景色 | 文字色 |
|----------|----------|--------|--------|
| 通过 | `✓ 通过` | `#f6ffed` | `#52c41a` |
| 未通过 | `✗ 未通过` | `#fff2f0` | `#ff4d4f` |
| 审核中 | `⏳ 审核中` | `#fafafa` | `#bfbfbf` |

**提取规则**：优先从 `content` 字段中匹配关键词 "通过"、"未通过"、"审核中"、"拒绝" 等。若 content 中无明确结果标识，则不显示标签。

#### 2.2.3 日期分组

通知按日期分组，每组显示日期标题：

| 规则 | 显示文本 |
|------|----------|
| 今天 | `今天 (5月17日)` |
| 昨天 | `昨天 (5月16日)` |
| 7天内 | `星期X (5月15日)` |
| 更早 | `2026年5月14日` |

同一天内的通知按时间倒序排列。

#### 2.2.4 加载与分页策略

由于后端 API **不支持分页**，一次返回全部通知数据，采用以下策略：

- **前端虚拟滚动**：使用 `ListView` 的虚拟单元格机制（`ListCell`），仅渲染可视区域内的卡片节点，保证大数据量下的性能
- **总数提示**：底部显示 "共 X 条通知"
- **性能预估**：JavaFX `ListView` 虚拟机制可轻松处理 500+ 条数据；若超过 1000 条建议后端增加分页支持
- **加载动画**：数据加载期间显示旋转 `ProgressIndicator` + "加载中..." 文字，加载完成后隐藏

#### 2.2.5 空状态设计

无通知时显示友好的空状态页面：

```
┌──────────────────────────────────────┐
│                                      │
│              🔔                      │
│                                      │
│        暂无通知                       │
│    当您收到新通知时会在这里显示         │
│                                      │
│           [刷新]                      │
│                                      │
└──────────────────────────────────────┘
```

### 2.3 配色方案

| 元素 | 颜色用途 | 色值 | 备注 |
|------|----------|------|------|
| 页面背景 | 整体底色 | `#f5f7fa` | 冷灰，比纯灰更清爽 |
| 卡片背景（未读） | 未读通知卡片 | `#ffffff` | 纯白突出未读 |
| 卡片背景（已读） | 已读通知卡片 | `#fafbfc` | 极浅灰，区分层次 |
| 未读指示条 | 左侧标识 | `#4A90D9` | 醒目蓝 |
| 主文字 | 标题/正文 | `#262626` | 深灰近黑 |
| 次要文字 | 时间/辅助信息 | `#8c8c8c` | 中灰 |
| 分割线 | 卡片分割线 | `#f0f0f0` | 极浅 |
| 审核通过标签 | 通过状态 | 背景 `#f6ffed` / 文字 `#52c41a` | 浅绿 |
| 审核未通过标签 | 未通过状态 | 背景 `#fff2f0` / 文字 `#ff4d4f` | 浅红 |
| 审核中标签 | 审核中状态 | 背景 `#fafafa` / 文字 `#bfbfbf` | 浅灰 |
| 类型图标底色-系统 | 蓝色圆形 | `#e6f7ff` | 浅蓝 |
| 类型图标底色-举报 | 橙色圆形 | `#fff7e6` | 浅橙 |
| 类型图标底色-审核 | 绿色圆形 | `#f6ffed` | 浅绿 |
| 类型图标底色-评论 | 青色圆形 | `#e6fffb` | 浅青 |
| 类型图标底色-粉丝 | 紫色圆形 | `#f9f0ff` | 浅紫 |
| 类型图标底色-发帖 | 靛蓝圆形 | `#f0f5ff` | 浅靛 |
| 按钮悬停 | 交互反馈 | `#e6f7ff` | 浅蓝 |
| 加载动画色 | 进度指示 | `#1890ff` | 品牌蓝 |
| 空状态图标色 | 空状态 | `#d9d9d9` | 浅灰 |

**配色原则：**
- 使用 Ant Design 色系，保持视觉一致性
- 避免在 JavaFX 中使用纯 Emoji 作为类型图标（Windows 平台部分 Emoji 显示为黑白方块），改用 **Unicode 图标字符 + 彩色圆形背景** 的方案
- 所有颜色值使用标准 CSS 颜色格式，适配 JavaFX CSS

### 2.4 交互设计

| 操作 | 触发方式 | 行为 |
|------|----------|------|
| 单击未读卡片 | 鼠标左键单击 | 调用 API 标记已读，卡片变为已读样式，展开显示操作按钮 |
| 单击已读卡片 | 鼠标左键单击 | 切换展开/收起状态，显示/隐藏操作按钮 |
| 双击卡片 | 鼠标左键双击 | 展开详情面板（如果关联帖子则弹出"查看原帖"确认） |
| 右键卡片 | 鼠标右键 | 弹出上下文菜单：标记已读/标记未读/查看原帖 |
| 点击"标记未读" | 按钮点击 | 调用 API 将 `isRead` 重置为 0（注意：需后端支持或仅前端状态切换） |
| 点击"查看原帖" | 按钮点击 | 通过 `MainFrameController.openPostDetail(postId)` 跳转 |
| 点击"全部已读" | 按钮点击 | 调用 `markAllNotificationsAsRead` API，刷新列表 |
| 点击"刷新" | 按钮点击 | 重新加载通知列表和未读数，带加载动画 |
| 切换类型筛选 | 下拉框选择 | 前端过滤当前列表（不调用 API，因为 API 无缓存机制，每次调用会重新请求） |
| 悬停卡片 | 鼠标悬停 | 显示操作按钮区域（标记已读/未读、查看原帖） |
| 滚动列表 | 鼠标滚轮 | 虚拟滚动，仅渲染可视区域 |

**注意 - "标记未读"功能说明：**
后端仅提供标记已读接口（`readNotification`），无标记未读接口。前端可实现两种方式：
1. **保守方案**：移除"标记未读"按钮，仅支持"标记已读"
2. **激进方案**：前端仅更新本地状态（不调用 API），刷新后恢复为已读

**推荐采用保守方案**，不提供"标记未读"功能。

## 3. 技术实现

### 3.1 FXML 结构变更

从 TableView 改为 ListView + 自定义 Cell + 日期分组 Header：

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.ProgressIndicator?>

<BorderPane xmlns="http://javafx.com/javafx/21" xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.teach.javafx.controller.MyNotificationController"
            styleClass="notification-page">
   <top>
      <VBox spacing="12" styleClass="notification-toolbar">
         <padding>
            <Insets bottom="12" left="16" right="16" top="12"/>
         </padding>
         <!-- 标题行 -->
         <HBox alignment="CENTER_LEFT" spacing="8">
            <Label text="🔔 我的通知" styleClass="page-title"/>
            <Label fx:id="unreadCountLabel" styleClass="unread-badge" text="(0 条未读)"/>
            <Region HBox.hgrow="ALWAYS"/>
            <Label fx:id="totalLabel" styleClass="total-label" text=""/>
         </HBox>
         <!-- 操作行 -->
         <HBox alignment="CENTER_LEFT" spacing="10">
            <ComboBox fx:id="typeFilterComboBox" promptText="全部类型"
                      styleClass="filter-combo" prefWidth="150"/>
            <Button fx:id="markAllReadButton" text="全部已读" styleClass="action-button"/>
            <Button fx:id="refreshButton" text="刷新" styleClass="action-button"/>
         </HBox>
      </VBox>
   </top>
   <center>
      <StackPane>
         <!-- 通知列表 -->
         <ListView fx:id="notificationListView" styleClass="notification-list"/>
         <!-- 加载遮罩 -->
         <VBox fx:id="loadingPane" alignment="CENTER" spacing="10"
               visible="false" managed="false" styleClass="loading-pane">
            <ProgressIndicator styleClass="loading-spinner"/>
            <Label text="加载中..." styleClass="loading-text"/>
         </VBox>
         <!-- 空状态 -->
         <VBox fx:id="emptyPane" alignment="CENTER" spacing="12"
               visible="false" managed="false" styleClass="empty-pane">
            <Label text="🔔" styleClass="empty-icon"/>
            <Label text="暂无通知" styleClass="empty-title"/>
            <Label text="当您收到新通知时会在这里显示" styleClass="empty-subtitle"/>
            <Button fx:id="emptyRefreshButton" text="刷新" styleClass="empty-button"/>
         </VBox>
      </StackPane>
   </center>
</BorderPane>
```

**FXML 设计说明：**
- 使用 `StackPane` 叠放 ListView、加载遮罩和空状态面板，通过 `visible` 和 `managed` 属性控制显示
- 移除排序下拉框，因为后端不支持排序参数，排序在前端按时间倒序处理即可
- `typeFilterComboBox` 改为前端过滤，避免重复请求 API

### 3.2 通知卡片 Node 结构

```
StackPane (根容器, 带 CSS class "notification-card")
├── Region (未读指示条, 3px 蓝色左边框, 未读时可见)
└── HBox (内容区域, spacing=12)
    ├── StackPane (类型图标, 32x32)
    │   ├── Circle (彩色圆形背景)
    │   └── Label (Emoji 图标文字, 居中)
    ├── VBox (文字内容区, VBox.vgrow=ALWAYS)
    │   ├── HBox (标题行)
    │   │   ├── Label (类型名称, 加粗)
    │   │   ├── Region (Hgrow=ALWAYS, 弹性间距)
    │   │   ├── Label (审核结果标签, 仅 type=3 显示)
    │   │   └── Label (相对时间, 灰色小字)
    │   ├── Label (内容预览, 最多2行, 自动换行)
    │   └── HBox (操作按钮行, 悬停/展开时显示)
    │       ├── Button (标记已读)
    │       └── Button (查看原帖, 有关联帖子时显示)
    └── Region (右侧已读小圆点, 已读时隐藏)
```

### 3.3 JavaFX 21 适配要点

| 特性 | JavaFX 21 支持 | 使用方式 |
|------|----------------|----------|
| 虚拟滚动 | `ListView` 内置 | 使用 `setCellFactory` 创建自定义 `ListCell` |
| CSS 样式 | 完全支持 | 使用 `-fx-*` 前缀的 CSS 属性 |
| 自定义控件 | 完全支持 | 组合 `VBox`/`HBox`/`Label` 等基础控件 |
| 动画 | 支持 `Timeline`/`Transition` | 用于展开/收起卡片动画 |
| 平台 Emoji | **部分支持** | Windows 上部分 Emoji 显示为黑白方块，建议用 Unicode 符号 + 彩色圆形背景替代 |

**Emoji 兼容方案**：

不使用原生 Emoji，改用彩色圆形 + Unicode 字符：

| 通知类型 | 圆形背景色 | 内部字符 | 替代方案 |
|----------|------------|----------|----------|
| 系统通知 | `#e6f7ff` + `#1890ff` 文字 | 🔔 | `🔔` 或 `⚙` |
| 举报处理 | `#fff7e6` + `#fa8c16` 文字 | 🛡️ | `⚠` |
| 帖子审核 | `#f6ffed` + `#52c41a` 文字 | 📄 | `📋` |
| 评论回复 | `#e6fffb` + `#13c2c2` 文字 | 💬 | `💬` |
| 新增粉丝 | `#f9f0ff` + `#722ed1` 文字 | 👤 | `★` |
| 关注发帖 | `#f0f5ff` + `#2f54eb` 文字 | ✏️ | `✎` |

### 3.4 新增/修改的文件

| 文件 | 操作 | 说明 |
|------|------|------|
| `my-notification.fxml` | 重写 | TableView 改为 ListView + 加载/空状态遮罩 |
| `MyNotificationController.java` | 重写 | 自定义 ListCell 工厂、前端过滤/排序、日期分组 |
| `notification-page.css` | 新建 | 通知页面全局样式（卡片、按钮、空状态等） |

### 3.5 Controller 核心方法设计

```java
public class MyNotificationController extends ToolController {
    // ---- 数据 ----
    private ObservableList<Notification> allNotifications;        // 全量数据
    private ObservableList<Notification> filteredNotifications;   // 筛选后数据
    private Integer currentTypeFilter = null;
    private Notification expandedNotification = null;             // 当前展开的卡片

    // ---- 生命周期 ----
    @FXML public void initialize();      // 初始化 UI 组件和事件绑定

    // ---- 数据加载 ----
    void loadNotificationList();         // 从 API 加载全量通知列表
    void loadUnreadCount();              // 从 API 加载未读数量
    void applyFilters();                 // 根据类型筛选条件重新过滤列表
    void sortNotifications();            // 前端按 createTime 倒序排序

    // ---- 状态切换 ----
    void showLoading();                  // 显示加载遮罩
    void hideLoading();                  // 隐藏加载遮罩
    void showEmptyState();               // 显示空状态
    void hideEmptyState();               // 隐藏空状态
    void showNotificationList();         // 显示通知列表

    // ---- 用户操作 ----
    void markAsRead(Notification n);     // 标记单条已读（调用 API）
    void markAllAsRead();                // 全部标记已读（调用 API）
    void openNotificationDetail(Notification n);  // 打开详情 Dialog
    void openPostDetail(Notification n); // 跳转原帖

    // ---- 辅助方法 ----
    String getTypeText(Integer type);    // 类型编码 -> 文字
    String getTypeIcon(Integer type);    // 类型编码 -> Emoji 图标
    String getTypeIconBgColor(Integer type);  // 类型编码 -> 图标背景色
    String getTypeIconFgColor(Integer type);  // 类型编码 -> 图标文字色
    String formatRelativeTime(String createTime); // 绝对时间 -> 相对时间
    String extractReviewResult(String content);   // 提取审核结果文本
    Long extractPostId(Notification n);  // 从通知中提取关联帖子ID
    String groupDateLabel(String createTime);    // 时间 -> 日期分组标题
}
```

### 3.6 日期分组实现方案

JavaFX `ListView` 不直接支持分组，推荐两种方案：

**方案 A：扁平化列表 + 分组 Header 行（推荐）**

将所有数据（分组标题 + 通知项）放入同一个 `ObservableList<Object>`，使用标记类区分：

```java
class DateHeader {
    String dateLabel;  // "今天 (5月17日)"
}

class NotificationItem {
    Notification notification;
}

ObservableList<Object> displayList;  // 交替存放 DateHeader 和 NotificationItem
```

在 `setCellFactory` 中根据 item 类型渲染不同的内容：

```java
listView.setCellFactory(lv -> new ListCell<Object>() {
    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
        } else if (item instanceof DateHeader) {
            setGraphic(createHeaderNode((DateHeader) item));
        } else if (item instanceof NotificationItem) {
            setGraphic(createCardNode(((NotificationItem) item).notification));
        }
    }
});
```

**方案 B：使用第三方控件**

如 `ControlsFX` 的分组控件，但增加依赖复杂度，不推荐。

**推荐方案 A**，无需额外依赖，实现简洁。

### 3.7 前端筛选与排序

由于后端 API 每次调用都会重新请求服务器，**筛选和排序建议在前端完成**：

```java
// 筛选逻辑
private void applyFilters() {
    if (allNotifications == null) return;

    filteredNotifications.clear();

    List<Notification> filtered = allNotifications.stream()
        .filter(n -> currentTypeFilter == null || n.getType().equals(currentTypeFilter))
        .collect(Collectors.toList());

    // 排序（按 createTime 倒序）
    filtered.sort((a, b) -> {
        String ta = a.getCreateTime() != null ? a.getCreateTime() : "";
        String tb = b.getCreateTime() != null ? b.getCreateTime() : "";
        return tb.compareTo(ta); // 倒序
    });

    // 构建分组列表
    buildDisplayList(filtered);
    updateTotalLabel(filtered.size());
}
```

### 3.8 关联帖子ID提取

沿用现有 `extractPostId` 方法的正则提取逻辑，从 `title` + `content` 中提取帖子 ID：

```java
private static final Pattern POST_ID_PATTERN = Pattern.compile(
    "(?:帖子ID|帖子 ID|postId|post_id|帖子)[:：]?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
private static final Pattern CONTENT_ID_PATTERN = Pattern.compile(
    "(?:内容ID|内容 ID|ID)[:：]?\\s*(\\d+)");
```

当通知类型为 3（帖子审核）、4（评论回复）、6（关注发帖）时，优先匹配 `POST_ID_PATTERN`，其次匹配 `CONTENT_ID_PATTERN`。

## 4. 实施步骤

### 第一阶段：基础重构

1. 创建 `notification-page.css` 样式文件，定义所有 CSS class
2. 重写 `my-notification.fxml`，使用 ListView + StackPane 遮罩结构
3. 在 `MyNotificationController.java` 中实现：
   - 自定义 `ListCell` 工厂（含 DateHeader 和 NotificationItem 两种渲染模式）
   - 数据加载、加载/空状态切换
   - 前端筛选和排序

### 第二阶段：交互完善

4. 实现日期分组逻辑（`buildDisplayList` 方法）
5. 实现相对时间格式化（`formatRelativeTime` 方法）
6. 实现审核结果提取和标签展示
7. 实现右键上下文菜单
8. 实现卡片展开/收起动画

### 第三阶段：测试与优化

9. 测试大数据量（500+ 条）下的滚动性能
10. 测试类型筛选功能
11. 测试全部已读功能
12. 测试空状态和加载状态
13. 测试关联帖子跳转
14. 兼容性测试（Windows 平台 Emoji 显示）

## 5. 风险评估

| 风险项 | 影响 | 概率 | 缓解措施 |
|--------|------|------|----------|
| 数据量过大导致卡顿 | 高 | 低 | ListView 虚拟滚动 + 必要时限制显示条数 |
| Emoji 在 Windows 显示异常 | 中 | 中 | 使用 Unicode 符号 + 彩色圆形背景替代原生 Emoji |
| 后端未来增加分页 | 低 | 中 | 代码预留分页扩展点，筛选逻辑可改为后端请求 |
| 标记未读无 API 支持 | 低 | - | 采用保守方案，不提供标记未读功能 |

## 6. 后续扩展建议

- **后端增加分页接口**：支持 `page` 和 `pageSize` 参数，提升大数据量场景性能
- **后端增加删除接口**：允许用户清理不需要的通知
- **后端增加标记未读接口**：允许用户将已读通知重新标记为未读
- **通知点击直接展开**：考虑增加点击即展开详情的内联模式，减少 Dialog 弹窗
- **通知声音/系统托盘提醒**：新通知到达时弹出系统提醒
