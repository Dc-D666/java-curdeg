# 校园论坛系统 - 帖子详情页与发布页开发实现计划

## [x] 现状评估（前置检查）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 检查现有代码状态
  - post-detail.fxml 和 post-publish.fxml 已存在但是占位符
  - PostDetailController 和 PostPublishController 已存在但是空的
  - Comment.java 模型类不存在
  - HttpRequestUtil 需要添加新方法
- **Success Criteria**:
  - 确认现有功能状态
- **Notes**: 基础框架存在，需要填充内容

---

## [x] Task 1: 创建 Comment 模型类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 models 包下新建 Comment.java
  - 字段与后端 BbsComment 一致
  - 包含：id, postId, authorId, content, createTime, updateTime
  - 包含 @Transient 字段：authorNickname, authorAvatarUrl
- **Success Criteria**:
  - Comment 模型类创建完成
- **Test Requirements**:
  - `programmatic` TR-1.1: 所有字段正确定义
  - `human-judgement` TR-1.2: 代码风格与 Post.java 一致
- **Notes**: 参考 Post.java 的代码风格

---

## [x] Task 2: 在 HttpRequestUtil 中添加新方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 新增 getPostDetail(Long postId) 方法
  - 新增 getCommentList(Long postId) 方法
  - 新增 publishPost(Post post) 方法
  - 所有方法使用异步处理模式
- **Success Criteria**:
  - 所有新方法添加完成
- **Test Requirements**:
  - `programmatic` TR-2.1: 方法签名正确
  - `human-judgement` TR-2.2: 代码风格与现有方法一致
- **Notes**: 参考 getPostList() 的实现模式

---

## [x] Task 3: 实现 post-detail.fxml 布局
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 使用 BorderPane 作为根组件
  - 顶部：标题、作者、发布时间、状态
  - 中间：ScrollPane 包含内容、分隔线、评论区
  - 底部：评论输入框、发表评论按钮
- **Success Criteria**:
  - FXML 布局按需求完成
- **Test Requirements**:
  - `programmatic` TR-3.1: 所有组件都有正确的 fx:id
  - `human-judgement` TR-3.2: 布局合理清晰
- **Notes**: 保持简洁布局

---

## [x] Task 4: 实现 PostDetailController
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 定义所有 FXML 组件变量
  - 实现 setPostId() 方法接收参数
  - 实现 loadPostDetail() 异步加载帖子详情
  - 实现 loadCommentList() 异步加载评论列表
  - 实现评论按钮点击事件（暂时显示提示）
- **Success Criteria**:
  - Controller 功能完整
- **Test Requirements**:
  - `human-judgement` TR-4.1: 使用 Task 异步处理
  - `human-judgement` TR-4.2: 使用 Platform.runLater 更新 UI
  - `programmatic` TR-4.3: 所有事件处理正确绑定
- **Notes**: 参考 PostListController 的实现

---

## [x] Task 5: 实现 post-publish.fxml 布局
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 使用 BorderPane 作为根组件
  - 中间 VBox 包含：标题输入、板块选择、内容输入、图片URL输入、按钮
- **Success Criteria**:
  - FXML 布局按需求完成
- **Test Requirements**:
  - `programmatic` TR-5.1: 所有组件都有正确的 fx:id
  - `human-judgement` TR-5.2: 布局合理清晰
- **Notes**: 保持简洁布局

---

## [x] Task 6: 实现 PostPublishController
- **Priority**: P0
- **Depends On**: Task 5
- **Description**: 
  - 定义所有 FXML 组件变量
  - initialize() 中异步加载板块列表
  - 实现 publishButton 点击事件（含输入校验）
  - 实现 cancelButton 点击事件（关闭标签页）
- **Success Criteria**:
  - Controller 功能完整
- **Test Requirements**:
  - `human-judgement` TR-6.1: 使用 Task 异步处理
  - `human-judgement` TR-6.2: 使用 Platform.runLater 更新 UI
  - `programmatic` TR-6.3: 输入校验完善
- **Notes**: 参考 PostListController 的实现

---

## [x] Task 7: IDE 诊断检查
- **Priority**: P0
- **Depends On**: Task 6
- **Description**: 
  - 使用 IDE 诊断工具检查代码
  - 修复任何诊断错误或警告
- **Success Criteria**:
  - 无诊断错误
- **Test Requirements**:
  - `programmatic` TR-7.1: GetDiagnostics 返回空列表
- **Notes**: 确保代码质量

---

## 约束规则回顾

### ✅ 必须遵守的规则
1. **复用框架能力**
   - ✅ 复用主框架标签页管理逻辑
   - ✅ 复用 HttpRequestUtil
   - ✅ 复用现有代码模式

2. **异步请求处理**
   - ✅ 所有后端请求使用 Task
   - ✅ UI 更新使用 Platform.runLater
   - ✅ 参考现有示例代码

3. **界面布局**
   - ✅ 使用简单布局
   - ✅ 美化放后续阶段
