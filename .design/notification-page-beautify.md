# 通知页面美化设计文档

## 1. 当前问题诊断

### 1.1 视觉问题

| # | 问题 | 严重度 | 描述 |
|---|------|--------|------|
| 1 | 卡片间距不均匀 | 高 | `background-insets: 10 20 10 20` 导致卡片实际可见区域与 ListView 单元格大小不匹配，卡片之间间距不一致 |
| 2 | 未读标识与卡片脱节 | 高 | 蓝色边框 `border-width: 3 0 0 3` 与卡片圆角不协调，左上角直角 `border-radius: 0 8 8 8` 破坏了卡片整体圆角美感 |
| 3 | 类型图标太小且不统一 | 中 | 36x36 的圆形图标在卡片中视觉权重不足，Emoji 字符（⚙⚠📋💬★✎）在不同系统渲染差异大 |
| 4 | 工具栏视觉层次不清 | 中 | 标题行和操作行挤在一起，缺乏视觉分隔；"全部已读"蓝色按钮过于突出，喧宾夺主 |
| 5 | 日期分组样式简陋 | 中 | 仅用文字+横线，缺乏视觉吸引力 |
| 6 | 卡片内容排版拥挤 | 中 | 标题、内容、操作按钮垂直排列间距仅 6px，阅读体验差 |
| 7 | 已读通知视觉区分不够 | 低 | 仅通过文字颜色变灰区分，不够明显 |
| 8 | 底部信息栏样式突兀 | 低 | 白色背景+上边框，与页面整体风格不协调 |
| 9 | 滚动条未自定义 | 低 | 使用系统默认滚动条，与页面风格不匹配 |
| 10 | 卡片阴影效果过弱 | 低 | `dropshadow(gaussian, rgba(15,23,42,0.04), 8, 0, 0, 2)` 几乎不可见 |

### 1.2 布局问题

| # | 问题 | 描述 |
|---|------|------|
| 1 | 卡片内边距不足 | `padding: 14 16` 导致内容贴边 |
| 2 | 图标与文字间距过大 | `HBox spacing: 12` 加上图标自身 padding，实际间距超过 20px |
| 3 | 操作按钮行始终占位 | 即使隐藏也影响布局 |
| 4 | 内容预览截断不合理 | 80 字符硬截断，不考虑中英文混排 |

### 1.3 配色不一致问题

当前 `notification-page.css` 使用 Ant Design 色板，与 `personal-center.css` 的 Tailwind 色板不一致：

| 元素 | 当前色值（Ant Design） | 应改为（Tailwind，与 personal-center 一致） |
|------|----------------------|------------------------------------------|
| 未读边框 | `#4A90D9` | `#2563eb` |
| 系统通知图标背景 | `#e6f7ff` | `#eff6ff` |
| 系统通知图标文字 | `#1890ff` | `#2563eb` |
| 举报处理图标背景 | `#fff7e6` | `#fff7ed` |
| 举报处理图标文字 | `#fa8c16` | `#d97706` |
| 帖子审核图标背景 | `#f6ffed` | `#f0fdf4` |
| 帖子审核图标文字 | `#52c41a` | `#16a34a` |
| 评论回复图标背景 | `#e6fffb` | `#ecfeff` |
| 评论回复图标文字 | `#13c2c2` | `#0891b2` |
| 新增粉丝图标背景 | `#f9f0ff` | `#faf5ff` |
| 新增粉丝图标文字 | `#722ed1` | `#9333ea` |
| 关注发帖图标背景 | `#f0f5ff` | `#eef2ff` |
| 关注发帖图标文字 | `#2f54eb` | `#4f46e5` |
| 操作按钮文字（标记已读） | `#1890ff` | `#2563eb` |
| 操作按钮边框（标记已读） | `#91d5ff` | `#93c5fd` |
| 操作按钮悬停背景（标记已读） | `#e6f7ff` | `#eff6ff` |
| 页面背景 | `#f5f7fa` | `#eef2f7` |
| 副文字颜色 | `#8c8c8c` / `#595959` | `#6b7280` / `#94a3b8` |

## 2. 美化方案

### 2.1 设计理念

**关键词**：轻盈、呼吸感、层次分明

- 减少视觉噪音，增加留白
- 用色彩和图标建立信息层次
- 未读/已读状态对比更鲜明
- 与项目整体风格（personal-center.css）保持一致

### 2.2 整体布局调整

```
─────────────────────────────────────────────────────────────────────┐
│                                                                     │
│   我的通知                                              3 条未读    │
│                                                                     │
│   ┌──────────┐ ┌──────────┐ ┌──────────┐          ┌──────┐ ┌────┐ │
│   │  全部 53  │ │ 未读 3   │ │ 已读 50  │          │全部已读│ │刷新│ │
│   └──────────┘ └──────────┘ └──────────┘          └──────┘ └────┘ │
│   ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  │
│                                                                     │
│   ● 今天                                                           │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │ ● ⬜  帖子审核通知                              ✓ 通过      │   │
│   │       您的帖子「寻找饭友，一起探索校园美食」审核通过         │   │
│   │       15:12                           [标记已读] [查看原帖]  │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │ ● 🟣  新增粉丝通知                                          │   │
│   │       待测电阻 关注了你                                     │   │
│   │       11:39                                    [标记已读]   │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   ● 昨天                                                           │
│                                                                     │
│   ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐   │
│   │   💬  评论回复通知                                          │   │
│   │       不吃香菜 评论了您的帖子「论坛频道用户行为...」         │   │
│   │       23:45                                   [查看原帖]   │   │
│   └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘   │
│                                                                     │
│                                              共 53 条 · 3 条未读    │
└─────────────────────────────────────────────────────────────────────┘
```

> 布局说明：`●` 代表未读圆点指示器（仅未读卡片显示），`⬜` 代表类型图标（圆角矩形），已读卡片背景色为 `#fafbfc` 且无圆点。

### 2.3 核心改动点

#### 改动 1：Tab 式状态切换替代下拉框

将类型过滤下拉框改为 **状态 Tab 按钮**（全部/未读/已读），更直观：

```
┌──────────┐ ┌──────────┐ ┌──────────┐
│  全部 53  │ │ 未读 3   │ │ 已读 50  │
└──────────┘ └──────────┘ └──────────┘
```

- 选中的 Tab 使用蓝色背景 + 白色文字（与项目 `nav-button:selected` 风格一致）
- 未选中的 Tab 使用浅灰背景 + 灰色文字
- 每个 Tab 右侧显示对应数量

**⚠️ 功能变更说明**：当前 `typeFilterComboBox` 提供的是**类型过滤**（系统通知/举报处理/帖子审核等 6 种），而 Tab 按钮提供的是**状态过滤**（全部/未读/已读）。这是从「按类型筛选」到「按已读状态筛选」的功能维度切换。

**决策理由**：
1. 状态过滤（全部/未读/已读）是通知页面的核心需求，使用频率远高于类型过滤
2. 仅 3 个选项，Tab 按钮比下拉框更直观、操作更快捷
3. 日期分组已经提供了类型图标的视觉区分，用户可通过滚动快速定位
4. 如后续需要类型过滤，可在 Tab 行下方追加类型 Chip 过滤行（当前不实现，避免过度设计）

#### 改动 2：未读标识改为左侧圆点

去掉蓝色边框，改为卡片左侧的 **小圆点指示器**：

- 未读：卡片左侧显示 8px 蓝色圆点
- 已读：左侧无圆点，卡片背景色变为 `#fafbfc`

这种方式更简洁，不会破坏卡片的圆角美感。

**实现方式**：在卡片 HBox 最左侧添加一个 `Circle` 节点（半径 4px，fill=`#2563eb`），已读时设置 `setVisible(false)` 并调整左内边距。**不要使用 CSS 伪元素**（JavaFX 不支持 `::before`/`::after`）。

**布局结构**：
```
HBox (card)
├── Circle (unread-dot, 8px, 仅未读可见)
├── Region (dot-spacer, 宽 8px, 仅未读时占位)
├── StackPane (type-icon, 40x40 圆角矩形)
├── VBox (text-content)
│   ├── HBox (title-row)
│   ├── Label (content-preview)
│   └── HBox (action-row)
```

#### 改动 3：类型图标改为彩色圆角矩形

将 36x36 圆形图标改为 **40x40 圆角矩形**（radius=10），内部使用 Unicode 符号：

| 类型 | 图标 | 背景色 | 文字色 |
|------|------|--------|--------|
| 系统通知 | ⚙ | `#eff6ff` | `#2563eb` |
| 举报处理 | ⚠ | `#fff7ed` | `#d97706` |
| 帖子审核 | ✓ | `#f0fdf4` | `#16a34a` |
| 评论回复 | 💬 | `#ecfeff` | `#0891b2` |
| 新增粉丝 | ★ | `#faf5ff` | `#9333ea` |
| 关注发帖 | ✎ | `#eef2ff` | `#4f46e5` |

**实现方式**：将当前 `Circle` + `Label` 的组合改为 `StackPane` + `Label`，通过 CSS 的 `-fx-background-radius: 10` 实现圆角矩形。去掉 Java 代码中的 `new Circle(18)` 节点，改用 StackPane 的背景色。

#### 改动 4：卡片样式优化

- **修复 `background-insets` 问题**：去掉 `background-insets: 10 20 10 20`，改为通过 ListView 的 cell padding 控制间距
- 增大内边距：`padding: 16 20`
- 增强阴影：`dropshadow(gaussian, rgba(15,23,42,0.06), 12, 0, 0, 4)`
- 已读卡片：背景色 `#fafbfc`，边框 `#f0f0f0`
- 未读卡片：背景色 `#ffffff`，无边框
- 卡片间距：统一 8px（通过 ListView cell 的 `-fx-padding: 4 20` 实现，上下 4px 合计 8px）

**`background-insets` 修复方案**：

当前问题：`background-insets: 10 20 10 20` 让背景色从边框内缩，导致卡片可见区域与单元格大小不一致。

修复：去掉 `background-insets`，改为在 ListView 的 list-cell 上设置 padding：
```css
.notification-list > .virtual-flow > .clipped-container > .sheet > .list-cell {
    -fx-padding: 4 20;
    -fx-background-color: transparent;
}
```
卡片自身不再使用 `background-insets`，直接用 `padding` 控制内部间距。

#### 改动 5：日期分组样式优化

- 日期标题前加彩色圆点 `●`（使用 Label 文本 `● `，颜色 `#2563eb`，字号 12px）
- 去掉横线分隔，改用间距分组（日期标题上方增加 8px 间距）
- 字体大小 12px，颜色 `#94a3b8`

**实现方式**：修改 `createDateHeader()` 方法，在 Label 文本前添加 `● ` 前缀，去掉 Region 横线节点，调整 VBox 间距。

#### 改动 6：工具栏优化

- 标题行：标题 20px 加粗 + 右侧未读数胶囊
- 操作行：Tab 按钮 + 右侧操作按钮（全部已读、刷新）
- 两行之间用细线分隔

#### 改动 7：滚动条自定义

与 `personal-center.css` 保持一致：
- 宽度 8px
- 滑块圆角 `#c1c9d4`
- 轨道透明

```css
.notification-list .scroll-bar:vertical {
    -fx-background-color: transparent;
    -fx-pref-width: 8;
}
.notification-list .scroll-bar:vertical .thumb {
    -fx-background-color: #c1c9d4;
    -fx-background-radius: 4;
}
.notification-list .scroll-bar:vertical .track {
    -fx-background-color: transparent;
}
```

#### 改动 8：底部信息栏优化

- 去掉白色背景和上边框
- 改为透明背景，居中灰色小字

#### 改动 9：内容预览截断优化

- 将 80 字符硬截断改为基于行数的截断
- 使用 Label 的 `-fx-max-height` 配合 `-fx-wrap-text: true` 限制为最多 2 行
- JavaFX Label 没有原生行数限制属性，通过设置 `-fx-pref-height` / `-fx-max-height` 近似实现
- 保留尾部 `...` 省略号：在 Java 代码中仍做截断，但放宽到 120 字符，并确保不在中文汉字中间截断（使用 `BreakIterator` 获取安全截断点）

#### 改动 10：空状态图标优化

- 将 Emoji `🔔` 替换为纯文本 Unicode 字符 `○` 或使用 Label + CSS 样式绘制一个铃铛轮廓
- 避免跨平台 Emoji 渲染差异

#### 改动 11：悬停效果优化

- 悬停卡片：背景变浅蓝 + 左侧出现蓝色竖条（2px 宽，通过 `-fx-border-color` 的左侧边框实现）
- 已读卡片悬停：`-fx-border-color: transparent transparent transparent #93c5fd`
- 未读卡片悬停：`-fx-border-color: transparent transparent transparent #2563eb`

**注意**：JavaFX 不支持 CSS `transition` 属性，边框变化是即时切换，无过渡动画。如需过渡效果，需在 Java 代码中使用 `Timeline` 手动实现，但考虑到边框变化的视觉幅度很小，即时切换的体验可接受，**不建议为此增加代码复杂度**。

### 2.4 配色方案（与项目统一）

| 元素 | 颜色 | 色值 | 来源 |
|------|------|------|------|
| 页面背景 | 浅灰蓝 | `#eef2f7` | personal-center.css `.personal-center-root` |
| 卡片背景（未读） | 白色 | `#ffffff` | — |
| 卡片背景（已读） | 极浅灰 | `#fafbfc` | personal-center.css `.password-note-box` |
| 卡片边框（已读） | 浅灰 | `#f0f0f0` | — |
| 未读圆点 | 蓝色 | `#2563eb` | personal-center.css 主色 |
| 主文字 | 深灰 | `#1f2937` | personal-center.css `.personal-sidebar-title` |
| 副文字 | 中灰 | `#6b7280` | personal-center.css `.profile-subtitle` |
| 时间文字 | 浅灰 | `#94a3b8` | — |
| 已读内容文字 | 浅灰 | `#9ca3af` | — |
| 选中 Tab 背景 | 蓝色 | `#2563eb` | personal-center.css `.nav-button:selected` |
| 选中 Tab 文字 | 白色 | `#ffffff` | personal-center.css `.nav-button:selected` |
| 未选中 Tab 背景 | 浅灰 | `#f1f5f9` | — |
| 未选中 Tab 文字 | 灰色 | `#64748b` | personal-center.css `.stats-label` |
| 未读徽章背景 | 浅红 | `#fef2f2` | 保留（红色徽章是通用惯例） |
| 未读徽章文字 | 红色 | `#dc2626` | 保留（红色徽章是通用惯例） |
| 悬停竖条（未读） | 蓝色 | `#2563eb` | 主色 |
| 悬停竖条（已读） | 浅蓝 | `#93c5fd` | personal-center.css `.stats-card:hover` |

### 2.5 交互优化

| 操作 | 当前行为 | 优化后行为 | 实现方式 |
|------|----------|-----------|---------|
| 悬停卡片 | 背景变浅蓝 + 阴影增强 | 背景变浅蓝 + 左侧出现蓝色竖条（2px） | CSS `:hover` 伪类 |
| 单击未读卡片 | 标记已读 | 标记已读 + 圆点消失 | Java 代码：`markAsRead()` 后 `dot.setVisible(false)` |
| 操作按钮 | 悬停时显示 | 始终显示（未读时），已读时悬停显示 | 与当前逻辑一致，无需改动 |
| Tab 切换 | 无 | 即时切换列表内容 | Java 代码：切换 `filterMode` 后调用 `rebuildDisplayList()` |

**关于动画的决策**：

原方案中提到的"圆点消失动画"和"Tab 切换下划线动画"在 JavaFX 中**无法通过 CSS 实现**（JavaFX CSS 不支持 `@keyframes`、`transition`、`animation` 等属性）。如需动画效果，必须使用 Java 代码的 `Timeline` / `FadeTransition` / `TranslateTransition`。

**决策**：
- ❌ 圆点消失动画：**不实现**。圆点消失是一个瞬时状态变化，即时隐藏的体验足够好，不值得为此引入 `FadeTransition` 的代码复杂度。
- ❌ Tab 下划线动画：**不实现**。Tab 切换使用背景色变化即可（与 `nav-button:selected` 风格一致），下划线动画需要额外的 `TranslateTransition` + 指示器节点，收益不大。
- ✅ 悬停效果：通过 CSS `:hover` 即时切换，无需动画。

## 3. 技术实现

### 3.1 JavaFX CSS 限制与应对

JavaFX CSS 与 Web CSS 有显著差异，以下是本方案涉及的关键限制：

| 限制 | 影响 | 应对方案 |
|------|------|---------|
| 不支持 `::before` / `::after` 伪元素 | 无法用 CSS 生成未读圆点 | 在 Java 代码中创建 `Circle` 节点 |
| 不支持 `transition` / `animation` | 无法实现平滑过渡动画 | 使用 CSS `:hover` 即时切换，或 Java `Timeline` |
| 不支持 `@keyframes` | 同上 | 同上 |
| 不支持 `box-shadow` | 阴影写法不同 | 使用 `-fx-effect: dropshadow()` |
| 不支持 `border-left` 简写 | 左侧竖条需用四值语法 | `-fx-border-color: transparent transparent transparent #2563eb` + `-fx-border-width: 0 0 0 2` |
| 不支持 `rgba()` 在 `-fx-text-fill` 中 | 文字颜色不能用 rgba | 使用不透明十六进制色值 |
| `-fx-background-radius` 与 `-fx-border-radius` 独立 | 背景和边框圆角需分别设置 | 两者都设置为相同值 |
| `-fx-background-insets` 影响背景绘制区域 | 误用会导致间距问题 | 去掉 `background-insets`，改用 `padding` |
| `ListView` 的 `list-cell` 默认有样式 | 需要重置 cell 默认样式 | 显式设置 `-fx-background-color: transparent; -fx-padding: 0` |
| 不支持 `calc()` | 无法动态计算尺寸 | 使用固定值或 Java 代码绑定 |
| 不支持 CSS 变量（`--var`） | 无法定义设计令牌 | 在每个选择器中硬编码色值（或通过 Java 代码设置） |

### 3.2 FXML 变更

```xml
<BorderPane>
  <top>
    <VBox>
      <!-- 标题行 -->
      <HBox>
        <Label text="我的通知" styleClass="page-title" />
        <Region HBox.hgrow="ALWAYS" />
        <Label fx:id="unreadBadge" styleClass="unread-badge" />
      </HBox>
      <!-- Tab + 操作行 -->
      <HBox>
        <HBox styleClass="tab-group">
          <Button fx:id="tabAll" text="全部 53" styleClass="tab-btn, tab-btn-active" />
          <Button fx:id="tabUnread" text="未读 3" styleClass="tab-btn" />
          <Button fx:id="tabRead" text="已读 50" styleClass="tab-btn" />
        </HBox>
        <Region HBox.hgrow="ALWAYS" />
        <Button fx:id="markAllReadButton" text="全部已读" styleClass="toolbar-btn" />
        <Button fx:id="refreshButton" text="刷新" styleClass="toolbar-btn" />
      </HBox>
    </VBox>
  </top>
  <center>
    <StackPane>
      <ListView fx:id="notificationListView" />
      <VBox fx:id="loadingPane" />
      <VBox fx:id="emptyPane" />
    </StackPane>
  </center>
  <bottom>
    <HBox styleClass="bottom-bar">
      <Label fx:id="bottomInfoLabel" />
    </HBox>
  </bottom>
</BorderPane>
```

**FXML 变更要点**：
1. 删除 `ComboBox fx:id="typeFilterComboBox"` 节点
2. 新增 `HBox styleClass="tab-group"` 包含 3 个 `Button`（tabAll, tabUnread, tabRead）
3. Tab 按钮使用 `styleClass="tab-btn, tab-btn-active"` 语法（多个 styleClass 用逗号分隔）
4. "全部已读"按钮从 `action-button-primary` 改为 `toolbar-btn`（降低视觉权重）
5. "刷新"按钮从 `action-button` 改为 `toolbar-btn`

### 3.3 控制器变更

1. **删除** `ComboBox<String> typeFilterComboBox` 字段及 `setupTypeFilter()` 方法
2. **新增** 3 个 `Button` 字段：`tabAll`, `tabUnread`, `tabRead`
3. **新增** `filterMode` 枚举：`ALL`, `UNREAD`, `READ`
4. **新增** `setupTabButtons()` 方法：
   - 为 3 个 Tab 按钮设置点击事件
   - 点击时切换 `filterMode`，更新按钮 `styleClass`（添加/移除 `tab-btn-active`），调用 `rebuildDisplayList()`
5. **修改** `rebuildDisplayList()`：根据 `filterMode` 过滤（而非 `currentTypeFilter`）
6. **修改** `createNotificationCard()`：
   - 在卡片 HBox 最左侧添加 `Circle` 节点（未读圆点，半径 4px）
   - 将 `createTypeIcon()` 中的 `Circle` 节点替换为 `StackPane`（圆角矩形背景）
   - 去掉 `notification-card-unread` 样式类中的边框逻辑
7. **修改** `createDateHeader()`：
   - Label 文本前添加 `● ` 前缀
   - 去掉 Region 横线节点
   - 调整间距
8. **修改** `getContentPreview()`：
   - 放宽截断长度到 120 字符
   - 使用 `BreakIterator` 确保不在中文汉字中间截断
9. **修改** `markAsRead()`：
   - 标记已读后，通过引用更新对应卡片的圆点可见性和卡片样式类

**Tab 按钮切换逻辑伪代码**：
```java
private FilterMode filterMode = FilterMode.ALL;

private void setupTabButtons() {
    tabAll.setOnAction(e -> switchTab(FilterMode.ALL));
    tabUnread.setOnAction(e -> switchTab(FilterMode.UNREAD));
    tabRead.setOnAction(e -> switchTab(FilterMode.READ));
}

private void switchTab(FilterMode mode) {
    filterMode = mode;
    tabAll.getStyleClass().remove("tab-btn-active");
    tabUnread.getStyleClass().remove("tab-btn-active");
    tabRead.getStyleClass().remove("tab-btn-active");
    switch (mode) {
        case ALL:    tabAll.getStyleClass().add("tab-btn-active"); break;
        case UNREAD: tabUnread.getStyleClass().add("tab-btn-active"); break;
        case READ:   tabRead.getStyleClass().add("tab-btn-active"); break;
    }
    rebuildDisplayList();
}
```

**过滤逻辑伪代码**：
```java
private void rebuildDisplayList() {
    List<Notification> filtered = new ArrayList<>();
    for (Notification n : allNotifications) {
        boolean match = true;
        if (filterMode == FilterMode.UNREAD) match = (n.getIsRead() == null || n.getIsRead() != 1);
        if (filterMode == FilterMode.READ) match = (n.getIsRead() != null && n.getIsRead() == 1);
        if (match) filtered.add(n);
    }
    // ... 后续日期分组逻辑不变
}
```

**未读圆点实现伪代码**：
```java
// 在 createNotificationCard() 中
Circle unreadDot = new Circle(4);
unreadDot.setFill(Color.web("#2563eb"));
unreadDot.setVisible(!isRead);

Region dotSpacer = new Region();
dotSpacer.setPrefWidth(8);
dotSpacer.setVisible(!isRead);

// 卡片布局
HBox contentBox = new HBox(0);
contentBox.setAlignment(Pos.CENTER_LEFT);
contentBox.getChildren().addAll(unreadDot, dotSpacer, iconPane, textContent);
```

### 3.4 CSS 变更

#### 完整 CSS 重写方案

```css
/* ===== 页面基础 ===== */
.root {
    -fx-font-family: "Microsoft YaHei", "Segoe UI", sans-serif;
}

.notification-page {
    -fx-background-color: #eef2f7;
}

/* ===== 工具栏 ===== */
.notification-toolbar {
    -fx-background-color: #ffffff;
    -fx-border-color: transparent transparent #e5e7eb transparent;
    -fx-border-width: 0 0 1 0;
    -fx-padding: 14 20 12 20;
    -fx-spacing: 10;
}

.page-title {
    -fx-font-size: 20px;
    -fx-font-weight: bold;
    -fx-text-fill: #1f2937;
}

.unread-badge {
    -fx-font-size: 12px;
    -fx-font-weight: bold;
    -fx-text-fill: #dc2626;
    -fx-padding: 4 12;
    -fx-background-color: #fef2f2;
    -fx-background-radius: 999;
}

/* ===== Tab 按钮 ===== */
.tab-group {
    -fx-spacing: 4;
    -fx-alignment: CENTER_LEFT;
}

.tab-btn {
    -fx-background-color: #f1f5f9;
    -fx-text-fill: #64748b;
    -fx-font-size: 13px;
    -fx-font-weight: bold;
    -fx-padding: 6 14;
    -fx-background-radius: 6;
    -fx-border-color: transparent;
    -fx-border-radius: 6;
    -fx-cursor: hand;
}

.tab-btn:hover {
    -fx-background-color: #e2e8f0;
    -fx-text-fill: #475569;
}

.tab-btn-active {
    -fx-background-color: #2563eb;
    -fx-text-fill: white;
}

.tab-btn-active:hover {
    -fx-background-color: #1d4ed8;
    -fx-text-fill: white;
}

/* ===== 工具栏按钮 ===== */
.toolbar-btn {
    -fx-background-color: #f8fafc;
    -fx-text-fill: #4b5563;
    -fx-font-size: 13px;
    -fx-font-weight: bold;
    -fx-padding: 6 14;
    -fx-background-radius: 6;
    -fx-border-color: #e5e7eb;
    -fx-border-radius: 6;
    -fx-cursor: hand;
}

.toolbar-btn:hover {
    -fx-background-color: #eff6ff;
    -fx-text-fill: #2563eb;
    -fx-border-color: #bfdbfe;
}

.toolbar-btn:pressed {
    -fx-background-color: #dbeafe;
}

/* ===== 通知列表 ===== */
.notification-list {
    -fx-background-color: #eef2f7;
    -fx-padding: 0;
    -fx-border-color: transparent;
}

.notification-list > .virtual-flow > .clipped-container > .sheet > .list-cell {
    -fx-padding: 4 20;
    -fx-background-color: transparent;
    -fx-border-color: transparent;
}

/* ===== 日期标题 ===== */
.date-header {
    -fx-font-size: 12px;
    -fx-font-weight: bold;
    -fx-text-fill: #94a3b8;
    -fx-padding: 12 4 6 4;
}

/* ===== 通知卡片 ===== */
.notification-card {
    -fx-background-color: #ffffff;
    -fx-background-radius: 8;
    -fx-border-color: transparent;
    -fx-border-radius: 8;
    -fx-border-width: 0;
    -fx-padding: 16 20;
    -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.06), 12, 0, 0, 4);
    -fx-cursor: hand;
    -fx-spacing: 0;
}

.notification-card:hover {
    -fx-background-color: #f8fafc;
    -fx-border-color: transparent transparent transparent #2563eb;
    -fx-border-width: 0 0 0 2;
    -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.10), 14, 0, 0, 6);
}

.notification-card:pressed {
    -fx-background-color: #f0f5ff;
}

.notification-card-read {
    -fx-background-color: #fafbfc;
    -fx-border-color: #f0f0f0;
    -fx-border-width: 1;
    -fx-effect: dropshadow(gaussian, rgba(15, 23, 42, 0.03), 8, 0, 0, 2);
}

.notification-card-read:hover {
    -fx-background-color: #f1f5f9;
    -fx-border-color: transparent transparent transparent #93c5fd;
    -fx-border-width: 0 0 0 2;
}

/* ===== 类型图标（圆角矩形） ===== */
.type-icon {
    -fx-min-width: 40;
    -fx-min-height: 40;
    -fx-max-width: 40;
    -fx-max-height: 40;
    -fx-background-radius: 10;
    -fx-alignment: center;
}

.type-icon-system {
    -fx-background-color: #eff6ff;
}

.type-icon-report {
    -fx-background-color: #fff7ed;
}

.type-icon-review {
    -fx-background-color: #f0fdf4;
}

.type-icon-comment {
    -fx-background-color: #ecfeff;
}

.type-icon-follower {
    -fx-background-color: #faf5ff;
}

.type-icon-follow-post {
    -fx-background-color: #eef2ff;
}

.type-icon-label {
    -fx-font-size: 18px;
    -fx-font-weight: bold;
}

.type-icon-label-system {
    -fx-text-fill: #2563eb;
}

.type-icon-label-report {
    -fx-text-fill: #d97706;
}

.type-icon-label-review {
    -fx-text-fill: #16a34a;
}

.type-icon-label-comment {
    -fx-text-fill: #0891b2;
}

.type-icon-label-follower {
    -fx-text-fill: #9333ea;
}

.type-icon-label-follow-post {
    -fx-text-fill: #4f46e5;
}

/* ===== 卡片内容 ===== */
.card-type-label {
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-text-fill: #1f2937;
}

.card-type-label-read {
    -fx-text-fill: #9ca3af;
}

.card-time-label {
    -fx-font-size: 12px;
    -fx-text-fill: #94a3b8;
}

.card-content-label {
    -fx-font-size: 13px;
    -fx-text-fill: #4b5563;
    -fx-wrap-text: true;
    -fx-line-spacing: 4;
}

.card-content-label-read {
    -fx-text-fill: #9ca3af;
}

/* ===== 审核结果标签 ===== */
.review-tag {
    -fx-font-size: 11px;
    -fx-font-weight: bold;
    -fx-padding: 2 8;
    -fx-background-radius: 999;
}

.review-tag-pass {
    -fx-background-color: #f0fdf4;
    -fx-text-fill: #16a34a;
    -fx-border-color: #bbf7d0;
    -fx-border-radius: 999;
    -fx-border-width: 1;
}

.review-tag-fail {
    -fx-background-color: #fef2f2;
    -fx-text-fill: #dc2626;
    -fx-border-color: #fecaca;
    -fx-border-radius: 999;
    -fx-border-width: 1;
}

.review-tag-pending {
    -fx-background-color: #f8fafc;
    -fx-text-fill: #64748b;
    -fx-border-color: #e2e8f0;
    -fx-border-radius: 999;
    -fx-border-width: 1;
}

/* ===== 操作按钮 ===== */
.card-action-button {
    -fx-font-size: 11px;
    -fx-padding: 3 10;
    -fx-background-radius: 999;
    -fx-border-radius: 999;
    -fx-border-width: 1;
    -fx-cursor: hand;
}

.card-action-read {
    -fx-background-color: transparent;
    -fx-text-fill: #2563eb;
    -fx-border-color: #93c5fd;
}

.card-action-read:hover {
    -fx-background-color: #eff6ff;
}

.card-action-post {
    -fx-background-color: transparent;
    -fx-text-fill: #16a34a;
    -fx-border-color: #86efac;
}

.card-action-post:hover {
    -fx-background-color: #f0fdf4;
}

/* ===== 加载 / 空状态 ===== */
.loading-pane,
.empty-pane {
    -fx-alignment: center;
    -fx-spacing: 12;
}

.loading-spinner {
    -fx-pref-width: 32;
    -fx-pref-height: 32;
}

.loading-text {
    -fx-font-size: 13px;
    -fx-text-fill: #6b7280;
}

.empty-icon {
    -fx-font-size: 48px;
    -fx-text-fill: #d1d5db;
}

.empty-title {
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-text-fill: #6b7280;
}

.empty-subtitle {
    -fx-font-size: 13px;
    -fx-text-fill: #9ca3af;
}

.empty-button {
    -fx-background-color: #2563eb;
    -fx-text-fill: white;
    -fx-font-size: 13px;
    -fx-font-weight: bold;
    -fx-padding: 8 24;
    -fx-background-radius: 6;
    -fx-cursor: hand;
}

.empty-button:hover {
    -fx-background-color: #1d4ed8;
}

/* ===== 底部信息 ===== */
.bottom-bar {
    -fx-alignment: center;
    -fx-padding: 8 0;
    -fx-background-color: transparent;
}

.bottom-info-text {
    -fx-font-size: 12px;
    -fx-text-fill: #94a3b8;
}

/* ===== 滚动条 ===== */
.notification-list .scroll-bar:vertical {
    -fx-background-color: transparent;
    -fx-pref-width: 8;
}

.notification-list .scroll-bar:vertical .thumb {
    -fx-background-color: #c1c9d4;
    -fx-background-radius: 4;
}

.notification-list .scroll-bar:vertical .track {
    -fx-background-color: transparent;
}

.notification-list .scroll-bar:horizontal {
    -fx-background-color: transparent;
    -fx-pref-height: 8;
}

.notification-list .scroll-bar:horizontal .thumb {
    -fx-background-color: #c1c9d4;
    -fx-background-radius: 4;
}

.notification-list .scroll-bar:horizontal .track {
    -fx-background-color: transparent;
}
```

### 3.5 修改文件清单

| 文件 | 操作 | 变更要点 |
|------|------|---------|
| `notification-page.css` | 重写 | 全量替换为 Tailwind 色板；去掉 `background-insets`；新增 Tab/圆点/圆角矩形/滚动条样式；统一文字色值 |
| `my-notification.fxml` | 修改 | 删除 ComboBox，新增 Tab 按钮组；调整 styleClass 名称 |
| `MyNotificationController.java` | 修改 | Tab 按钮逻辑 + 圆点指示器 + 圆角矩形图标 + 日期标题优化 + 内容截断优化 |

### 3.6 实现优先级

| 优先级 | 改动项 | 预估工作量 |
|--------|--------|-----------|
| P0 | 修复 `background-insets` 间距问题 | 小 |
| P0 | 统一配色方案（Ant Design → Tailwind） | 中 |
| P0 | Tab 按钮替代 ComboBox | 中 |
| P1 | 未读圆点替代蓝色边框 | 中 |
| P1 | 类型图标改为圆角矩形 | 小 |
| P1 | 卡片样式优化（padding、shadow、hover 竖条） | 小 |
| P2 | 日期分组样式优化 | 小 |
| P2 | 滚动条自定义 | 小 |
| P2 | 底部信息栏优化 | 小 |
| P2 | 内容预览截断优化 | 小 |
| P3 | 空状态图标优化 | 小 |

## 4. 审稿记录

### 4.1 发现的问题与修正

| # | 问题 | 严重度 | 修正措施 |
|---|------|--------|---------|
| 1 | Tab 按钮将类型过滤替换为状态过滤，属于功能变更，原文档未说明 | 高 | 新增「功能变更说明」和「决策理由」，明确这是有意的维度切换 |
| 2 | 原文档提到"圆点消失动画"和"Tab 切换下划线动画"，但 JavaFX CSS 不支持动画 | 高 | 新增 JavaFX CSS 限制表，明确不实现动画，说明理由 |
| 3 | `background-insets` 问题在诊断中提出但解决方案中未明确修复 | 高 | 在改动 4 中新增 `background-insets` 修复方案，给出具体 CSS 代码 |
| 4 | 配色方案未标注色值来源，难以验证一致性 | 中 | 配色表新增「来源」列，标注每个色值对应的 personal-center.css 选择器 |
| 5 | 未读圆点未说明实现方式（JavaFX 不支持伪元素） | 中 | 在改动 2 中新增「实现方式」和「布局结构」说明 |
| 6 | 内容预览截断问题在诊断中提出但无解决方案 | 中 | 新增改动 9，提出基于行数限制 + BreakIterator 安全截断的方案 |
| 7 | 空状态 Emoji 跨平台渲染差异未处理 | 低 | 新增改动 10，建议替换 Emoji |
| 8 | 悬停效果未说明 JavaFX 中 border 变化无过渡 | 低 | 在改动 11 中明确说明，并给出决策 |
| 9 | CSS 变更部分仅列了要点，无完整代码 | 中 | 新增 3.4 节完整 CSS 重写方案 |
| 10 | 控制器变更缺少关键逻辑的伪代码 | 中 | 新增 Tab 切换、过滤、圆点实现的伪代码 |
| 11 | 实现无优先级划分 | 低 | 新增 3.6 节实现优先级表 |
