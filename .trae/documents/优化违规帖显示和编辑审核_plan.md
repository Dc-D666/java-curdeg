
# 优化违规帖显示和编辑审核

## 现状分析
### 前端现状
当前 `PostDetailController.java` 的按钮显示逻辑（`updateButtonVisibility` 方法）是基于用户角色来控制的，没有根据审核状态进行区分。

### 后端现状
当前 `BbsPostService.java` 的 `updatePost` 方法仍然使用敏感词库过滤（第 314-316 行），没有调用 AI 审核。

## 需求说明
1. **违规帖按钮显示优化**：对于审核状态为 `reject`（内容违规）的帖子，只显示「编辑」和「删除」两个按钮，其他全部隐藏
2. **编辑后重新审核**：编辑帖子后，发起新的 AI 审核（不再使用敏感词库过滤）

## 修改计划

### 1. 前端修改
**文件**：`java-fx/src/main/java/com/teach/javafx/controller/PostDetailController.java`

**修改内容**：
- 修改 `updateButtonVisibility` 方法，增加审核状态判断
- 当审核状态为 `reject` 时，只显示编辑和删除按钮，其他按钮（评论、点赞、收藏、举报、关注、置顶、加精）都隐藏

### 2. 后端修改
**文件**：`java-server/src/main/java/cn/edu/sdu/java/server/services/BbsPostService.java`

**修改内容**：
- 修改 `updatePost` 方法
- 注释掉敏感词库过滤的逻辑（参考 `createPost` 方法的做法）
- 编辑后重置审核状态为 `pending`（审核中）
- 调用 `postModerationService.moderatePostAsync(postId)` 发起 AI 审核
- 使用事务同步机制确保保存后再调用审核

## 修改后的行为预期

### 违规帖详情页
- 只有帖子作者和管理员可以看到编辑、删除按钮
- 其他用户看不到任何操作按钮
- 审核状态显示为红色的「内容违规」

### 编辑帖子后
- 帖子状态重置为「审核中」（橙色）
- 对普通用户不可见（`status` 设为 0）
- 后台自动调用 AI 进行审核
- 审核结果出来后自动更新状态并通知用户
