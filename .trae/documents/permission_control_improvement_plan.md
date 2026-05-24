# 权限控制完善 - 实现计划

## 概述
本阶段完善前后端的完整权限控制闭环，包括：
- 后端接口级权限（@PreAuthorize）
- 后端数据级权限（防止参数篡改）
- 前端界面级权限（菜单）
- 前端按钮级权限（编辑/删除/置顶/加精）

---

## [x] Task 1: 完善后端 BbsPostController 接口级权限
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 为所有需要权限的 BbsPostController 接口添加 @PreAuthorize 注解
  - 列表和详情接口公开访问
  - 发布/修改/删除需要登录并校验禁言
  - 置顶/加精需要管理员权限
- **Success Criteria**:
  - 所有接口都有正确的 @PreAuthorize 注解
  - 无权限访问时返回 403 Forbidden
- **Test Requirements**:
  - `programmatic` TR-1.1: 检查所有接口都有正确的权限注解
  - `human-judgement` TR-1.2: 代码结构清晰，权限逻辑合理
- **Notes**: 需要复用 SecurityUtils 等框架能力

---

## [x] Task 2: 完善后端 BbsCommentController 接口级权限
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 为所有需要权限的 BbsCommentController 接口添加 @PreAuthorize 注解
  - 获取评论列表公开访问
  - 发布/修改/删除需要登录并校验禁言
- **Success Criteria**:
  - 所有接口都有正确的 @PreAuthorize 注解
- **Test Requirements**:
  - `programmatic` TR-2.1: 检查所有接口都有正确的权限注解

---

## [x] Task 3: 完善后端 BbsUserController 接口级权限
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 确保 BbsUserController 接口权限正确
  - getCurrentUser 和 updateCurrentUser 需要登录
  - getUserList 需要管理员权限
- **Success Criteria**:
  - 所有接口权限正确
- **Test Requirements**:
  - `programmatic` TR-3.1: 检查接口权限注解

---

## [x] Task 4: 完善后端数据级权限 - 发布操作
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 在 BbsPostService.createPost 中强制设置 author_id 为当前登录用户
  - 在 BbsCommentService.createComment 中强制设置 author_id 为当前登录用户
  - 忽略前端传来的 author_id
  - 添加禁言用户校验
- **Success Criteria**:
  - 无论前端传什么 author_id，后端都强制使用当前登录用户 ID
  - 禁言用户无法发布
- **Test Requirements**:
  - `programmatic` TR-4.1: 检查强制设置 author_id 的逻辑
  - `programmatic` TR-4.2: 检查禁言校验逻辑

---

## [x] Task 5: 完善后端数据级权限 - 修改/删除操作
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 在 BbsPostService.updatePost/deletePost 中：
    - 学生只能操作自己的帖子
    - 管理员可以操作所有帖子
  - 在 BbsCommentService.updateComment/deleteComment 中：
    - 学生只能操作自己的评论
    - 管理员可以操作所有评论
  - 添加参数校验（ID > 0）
- **Success Criteria**:
  - 学生无法操作他人的帖子/评论
  - 管理员可以操作所有
  - 参数校验正确
- **Test Requirements**:
  - `programmatic` TR-5.1: 检查作者身份校验逻辑
  - `programmatic` TR-5.2: 检查角色判断逻辑

---

## [x] Task 6: 前端 - 完善 User 模型
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 确保 User.java 模型包含所有必要字段（id, authority, isBanned 等）
- **Success Criteria**:
  - User 模型字段完整
- **Test Requirements**:
  - `programmatic` TR-6.1: 检查 User 模型字段

---

## [x] Task 7: 前端 - 完善 HttpRequestUtil.getCurrentUser()
- **Priority**: P1
- **Depends On**: Task 6
- **Description**: 
  - 确保可以获取当前登录用户完整信息
- **Success Criteria**:
  - getCurrentUser() 返回完整 User 对象
- **Test Requirements**:
  - `programmatic` TR-7.1: 验证 getCurrentUser() 方法

---

## [x] Task 8: 前端 - PostListController 按钮级权限
- **Priority**: P1
- **Depends On**: Task 7
- **Description**: 
  - 在 PostListController.initialize() 中获取当前用户
  - 根据用户登录状态和 isBanned 控制"发布帖子"按钮的可见性和可用性
- **Success Criteria**:
  - 未登录或禁言用户看不到/用不了发布按钮
  - 正常登录用户可以发布
- **Test Requirements**:
  - `human-judgement` TR-8.1: 测试发布按钮显示逻辑

---

## [x] Task 9: 前端 - PostDetailController 按钮级权限（含置顶/加精按钮）
- **Priority**: P1
- **Depends On**: Task 7
- **Description**: 
  - 在 PostDetailController 中添加 currentUser 字段
  - 在 setPostId() 中获取当前用户
  - 在 loadPostDetail() 成功回调中：
    - 根据用户身份控制编辑/删除按钮显示
    - 根据角色控制置顶/加精按钮显示
  - 根据用户状态控制评论按钮显示
- **Success Criteria**:
  - 作者可以看到编辑/删除按钮
  - 管理员可以看到所有按钮
  - 普通用户看不到编辑/删除/置顶/加精按钮
  - 禁言用户看不到评论按钮
- **Test Requirements**:
  - `human-judgement` TR-9.1: 测试详情页所有按钮显示逻辑

---

## [x] Task 10: IDE 诊断检查
- **Priority**: P1
- **Depends On**: All previous tasks
- **Description**: 
  - 运行 GetDiagnostics 检查所有修改的文件
  - 确保无编译错误
- **Success Criteria**:
  - 无任何诊断错误或警告
- **Test Requirements**:
  - `programmatic` TR-10.1: 运行诊断检查并通过

---

## 权限对照表

### 后端接口权限表
| 接口 | 允许角色 | 数据级权限 |
|------|----------|------------|
| GET /api/bbs/post/list | 公开 | 无 |
| GET /api/bbs/post/{id} | 公开 | 无 |
| POST /api/bbs/post | 所有登录用户（非禁言） | 强制 author_id = 当前用户 |
| PUT /api/bbs/post/{id} | 所有登录用户（非禁言）+ ADMIN/SUPER | 学生只能改自己的，管理员可以改所有 |
| DELETE /api/bbs/post/{id} | 所有登录用户（非禁言）+ ADMIN/SUPER | 学生只能删自己的，管理员可以删所有 |
| POST /api/bbs/post/{id}/top | ADMIN/SUPER | 无 |
| POST /api/bbs/post/{id}/feature | ADMIN/SUPER | 无 |
| GET /api/bbs/comment/post/{postId} | 公开 | 无 |
| POST /api/bbs/comment/post/{postId} | 所有登录用户（非禁言） | 强制 author_id = 当前用户 |
| PUT /api/bbs/comment/{id} | 所有登录用户（非禁言）+ ADMIN/SUPER | 学生只能改自己的，管理员可以改所有 |
| DELETE /api/bbs/comment/{id} | 所有登录用户（非禁言）+ ADMIN/SUPER | 学生只能删自己的，管理员可以删所有 |
| GET /api/bbs/user/me | 所有登录用户 | 只能获取自己的 |
| PUT /api/bbs/user/me | 所有登录用户 | 只能修改自己的 |
| GET /api/bbs/user/list | ADMIN/SUPER | 无 |
| POST /api/bbs/board | SUPER | 无 |
| PUT /api/bbs/board/{id} | SUPER | 无 |
| DELETE /api/bbs/board/{id} | SUPER | 无 |

### 前端按钮权限表
| 按钮 | 所在页面 | 显示条件 |
|------|----------|----------|
| 管理后台菜单 | 主框架 | ADMIN 或 SUPER |
| 发布帖子按钮 | 帖子列表页 | 已登录且 isBanned=0 |
| 编辑帖子按钮 | 帖子详情页 | 已登录且 isBanned=0 且（是作者或 ADMIN/SUPER） |
| 删除帖子按钮 | 帖子详情页 | 同上 |
| 置顶帖子按钮 | 帖子详情页 | ADMIN 或 SUPER |
| 加精帖子按钮 | 帖子详情页 | ADMIN 或 SUPER |
| 发表评论按钮 | 帖子详情页 | 已登录且 isBanned=0 |
