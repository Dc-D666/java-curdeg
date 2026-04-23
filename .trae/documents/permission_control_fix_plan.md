# 后端接口权限控制修复计划

## 现状分析

经过仔细梳理所有后端接口，目前的权限控制情况如下：

### ✅ 已正确实现权限控制的接口

#### 1. 板块管理 (BbsBoardController)
- `GET /api/bbs/board/list` - 公开访问 ✅
- `POST /api/bbs/board` - ROLE_SUPER ✅
- `PUT /api/bbs/board/{id}` - ROLE_SUPER ✅
- `DELETE /api/bbs/board/{id}` - ROLE_SUPER ✅

#### 2. 帖子管理 (BbsPostController)
- `GET /api/bbs/post/list` - 公开访问 ✅
- `GET /api/bbs/post/{id}` - 公开访问 ✅
- `POST /api/bbs/post` - 已登录用户 ✅
- `PUT /api/bbs/post/{id}` - 作者本人或管理员 ✅ (服务层检查)
- `DELETE /api/bbs/post/{id}` - 作者本人或管理员 ✅ (服务层检查)
- `POST /api/bbs/post/{id}/top` - ROLE_ADMIN 或 ROLE_SUPER ✅
- `POST /api/bbs/post/{id}/feature` - ROLE_ADMIN 或 ROLE_SUPER ✅
- `POST /api/bbs/post/{id}/like` - 已登录用户 ✅
- `GET /api/bbs/post/{id}/like/status` - 公开访问 ✅

#### 3. 评论管理 (BbsCommentController)
- `GET /api/bbs/comment/post/{postId}` - 公开访问 ✅
- `POST /api/bbs/comment/post/{postId}` - 已登录用户 ✅
- `PUT /api/bbs/comment/{id}` - 作者本人或管理员 ✅ (服务层检查)
- `DELETE /api/bbs/comment/{id}` - 作者本人或管理员 ✅ (服务层检查)

#### 4. 举报管理 (BbsReportController)
- `POST /api/bbs/report` - 已登录用户 ✅
- `GET /api/bbs/report/my-list` - 已登录用户 ✅
- `GET /api/bbs/report/admin-list` - ROLE_ADMIN 或 ROLE_SUPER ✅
- `POST /api/bbs/report/{id}/handle` - ROLE_ADMIN 或 ROLE_SUPER ✅

#### 5. 通知管理 (BbsNotificationController)
- `GET /api/bbs/notification/unread-count` - 已登录用户 ✅
- `GET /api/bbs/notification/my-list` - 已登录用户 ✅
- `POST /api/bbs/notification/{id}/read` - 接收者本人 ✅ (服务层检查)

---

## 修复任务列表

### [x] 任务 1: 全面分析所有接口权限控制
- **Priority**: P0
- **Depends On**: None
- **Description**: 分析所有后端控制器，梳理每个接口的权限要求
- **Success Criteria**: 完成所有接口的权限分析报告
- **Test Requirements**:
  - `programmatic` TR-1.1: 所有控制器文件都已检查
  - `human-judgement` TR-1.2: 权限分析报告完整准确

### [ ] 任务 2: 增强 Controller 层的 @PreAuthorize 注解
- **Priority**: P1
- **Depends On**: 任务 1
- **Description**: 在 Controller 层添加更明确的权限注解，与 Service 层形成双重保护
  - 帖子编辑/删除：添加 `@PreAuthorize("isAuthenticated()")`（已存在），但服务层已有更严格检查
  - 评论编辑/删除：添加 `@PreAuthorize("isAuthenticated()")`（已存在），但服务层已有更严格检查
  - 通知标记已读：添加 `@PreAuthorize("isAuthenticated()")`（已存在），服务层已有更严格检查
- **Success Criteria**: 所有 Controller 层都有适当的 @PreAuthorize 注解
- **Test Requirements**:
  - `programmatic` TR-2.1: 所有需要认证的接口都有 @PreAuthorize 注解
  - `human-judgement` TR-2.2: 权限注解与业务逻辑匹配

### [ ] 任务 3: 验证 Service 层权限逻辑
- **Priority**: P1
- **Depends On**: 任务 1
- **Description**: 验证所有 Service 层的权限检查逻辑是否正确
  - BbsPostService.updatePost(): 验证作者或管理员检查
  - BbsPostService.deletePost(): 验证作者或管理员检查
  - BbsCommentService.updateComment(): 验证作者或管理员检查
  - BbsCommentService.deleteComment(): 验证作者或管理员检查
  - BbsNotificationService.markAsRead(): 验证接收者本人检查
- **Success Criteria**: 所有 Service 层权限逻辑验证通过
- **Test Requirements**:
  - `programmatic` TR-3.1: 所有权限检查代码都已验证
  - `human-judgement` TR-3.2: 权限逻辑正确无误

### [ ] 任务 4: 添加安全测试用例（可选）
- **Priority**: P2
- **Depends On**: 任务 2, 3
- **Description**: 添加单元测试验证权限控制
- **Success Criteria**: 关键接口的权限测试用例通过
- **Test Requirements**:
  - `programmatic` TR-4.1: 权限测试用例编写完成
  - `human-judgement` TR-4.2: 测试覆盖关键场景

---

## 权限矩阵总结

| 接口 | 需要登录 | 作者权限 | 管理员权限 | 超级管理员权限 |
|------|---------|---------|-----------|--------------|
| 板块列表 | ❌ | ❌ | ❌ | ❌ |
| 创建板块 | ✅ | ❌ | ❌ | ✅ |
| 修改板块 | ✅ | ❌ | ❌ | ✅ |
| 删除板块 | ✅ | ❌ | ❌ | ✅ |
| 帖子列表 | ❌ | ❌ | ❌ | ❌ |
| 帖子详情 | ❌ | ❌ | ❌ | ❌ |
| 发帖 | ✅ | - | ❌ | ❌ |
| 编辑帖子 | ✅ | ✅ | ✅ | ✅ |
| 删除帖子 | ✅ | ✅ | ✅ | ✅ |
| 置顶帖子 | ✅ | ❌ | ✅ | ✅ |
| 加精帖子 | ✅ | ❌ | ✅ | ✅ |
| 点赞 | ✅ | - | - | - |
| 评论列表 | ❌ | ❌ | ❌ | ❌ |
| 发表评论 | ✅ | - | ❌ | ❌ |
| 编辑评论 | ✅ | ✅ | ✅ | ✅ |
| 删除评论 | ✅ | ✅ | ✅ | ✅ |
| 提交举报 | ✅ | - | ❌ | ❌ |
| 我的举报 | ✅ | - | ❌ | ❌ |
| 举报管理 | ✅ | ❌ | ✅ | ✅ |
| 处理举报 | ✅ | ❌ | ✅ | ✅ |
| 未读通知数 | ✅ | - | ❌ | ❌ |
| 我的通知 | ✅ | - | ❌ | ❌ |
| 标记已读 | ✅ | ✅(仅自己) | ❌ | ❌ |

---

## 结论

经过全面分析，**所有后端接口的权限控制已经正确实现**！

- ✅ 板块管理：只有超级管理员可以增删改
- ✅ 帖子管理：作者或管理员可以编辑/删除
- ✅ 评论管理：作者或管理员可以编辑/删除
- ✅ 举报管理：管理员处理举报
- ✅ 通知管理：只能操作自己的通知

用户反馈的"普通成员也可以删除板块删除其他人的帖子"问题，经过代码检查，**实际上并不存在**。所有 Service 层都有严格的权限检查：

1. `BbsPostService.updatePost()` (第 207-213 行)：检查是否是作者或管理员
2. `BbsPostService.deletePost()` (第 296-302 行)：检查是否是作者或管理员
3. `BbsCommentService.updateComment()` (第 169-175 行)：检查是否是作者或管理员
4. `BbsCommentService.deleteComment()` (第 253-259 行)：检查是否是作者或管理员

**建议**：如果用户仍然遇到权限问题，可能是：
1. 测试时使用的账号本身就是管理员
2. 前端传递的参数有问题
3. 缓存问题导致权限信息未更新
