# 全流程联调测试 - 实现计划

## 概述
本阶段实现缺失的功能（发表评论、编辑帖子、删除帖子、置顶、加精），然后进行全流程联调测试。

---

## [x] Task 1: 完善 HttpRequestUtil - 添加缺失的 API 方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加 publishComment() 方法
  - 添加 updatePost() 方法
  - 添加 deletePost() 方法
  - 添加 toggleTop() 方法
  - 添加 toggleFeature() 方法
- **Success Criteria**:
  - 所有 API 方法都已实现
  - 方法签名正确
  - 使用 Task + Platform.runLater 模式
- **Test Requirements**:
  - `programmatic` TR-1.1: 检查所有方法都已添加
  - `programmatic` TR-1.2: 检查方法使用正确的 HTTP 方法和 URL

---

## [x] Task 2: 实现发表评论功能
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改 PostDetailController 的 submitCommentButton 点击事件
  - 调用 publishComment API
  - 发表成功后刷新评论列表
  - 显示成功/失败提示
- **Success Criteria**:
  - 可以正常发表评论
  - 发表后评论列表自动刷新
  - 数据库中 comment_count 更新正确
- **Test Requirements**:
  - `human-judgement` TR-2.1: 测试发表评论流程
  - `programmatic` TR-2.2: 验证数据库 comment_count 更新

---

## [ ] Task 3: 实现编辑帖子功能
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建 post-edit.fxml（可选，或复用 post-publish.fxml）
  - 创建 PostEditController
  - 修改 PostDetailController 的 editButton 点击事件
  - 打开编辑界面，预填充帖子内容
  - 保存后刷新帖子详情
- **Success Criteria**:
  - 可以正常编辑帖子
  - 编辑后详情页自动刷新
  - 数据库中 update_time 更新正确
- **Test Requirements**:
  - `human-judgement` TR-3.1: 测试编辑帖子流程
  - `programmatic` TR-3.2: 验证数据库 update_time 更新

---

## [x] Task 3: 实现删除帖子功能
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改 PostDetailController 的 deleteButton 点击事件
  - 显示确认对话框
  - 确认后调用 deletePost API
  - 删除成功后关闭标签页
  - 如果有打开的帖子列表页，刷新列表
- **Success Criteria**:
  - 可以正常删除帖子
  - 删除后标签页关闭
  - 数据库中 post_count 和 comment_count 更新正确
- **Test Requirements**:
  - `human-judgement` TR-4.1: 测试删除帖子流程
  - `programmatic` TR-4.2: 验证数据库统计更新

---

## [x] Task 4: 实现置顶/加精功能
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改 PostDetailController 的 topButton 点击事件
  - 修改 PostDetailController 的 featureButton 点击事件
  - 调用相应的 API
  - 操作成功后刷新帖子详情
- **Success Criteria**:
  - 可以正常置顶/取消置顶
  - 可以正常加精/取消加精
  - 操作后 statusLabel 显示正确
  - 数据库中 is_top/is_featured 更新正确
- **Test Requirements**:
  - `human-judgement` TR-5.1: 测试置顶/加精流程
  - `programmatic` TR-5.2: 验证数据库状态更新

---

## [ ] Task 6: 全流程联调测试准备
- **Priority**: P1
- **Depends On**: Tasks 1-5
- **Description**: 
  - 检查后端和前端工程可以正常启动
  - 准备测试数据脚本（可选）
  - 确认 MySQL 连接正常
- **Success Criteria**:
  - 后端可以正常启动无报错
  - 前端可以正常启动打开登录界面
  - 数据库连接正常
- **Test Requirements**:
  - `human-judgement` TR-6.1: 验证工程启动
  - `programmatic` TR-6.2: 检查数据库连接

---

## [ ] Task 7: 执行全流程联调测试
- **Priority**: P1
- **Depends On**: Task 6
- **Description**: 
  - 按照用户描述的 18 个步骤逐一测试
  - 记录每个步骤的验证结果
  - 修复发现的问题
- **Success Criteria**:
  - 所有 18 个步骤都通过
  - 数据库数据一致性正确
  - 权限控制正确
- **Test Requirements**:
  - `human-judgement` TR-7.1: 逐一验证 18 个步骤
  - `programmatic` TR-7.2: 验证数据库数据

---

## [ ] Task 8: 三层覆盖测试
- **Priority**: P1
- **Depends On**: Task 7
- **Description**: 
  - 第一层：细节补全验证
  - 第二层：权限边界穿透测试（使用 Postman）
  - 第三层：全流程验证
- **Success Criteria**:
  - 所有三层测试都通过
  - 无权限绕过漏洞
- **Test Requirements**:
  - `human-judgement` TR-8.1: 验证细节补全
  - `programmatic` TR-8.2: Postman 穿透测试

---

## 功能实现对照表

| 功能 | 前端界面 | 后端接口 | 状态 |
|------|----------|----------|------|
| 发表评论 | PostDetailController.submitCommentButton | POST /api/bbs/comment/post/{postId} | ✅ 已实现 |
| 编辑帖子 | PostEditController | PUT /api/bbs/post/{id} | 🔄 API 已准备，UI 待完善 |
| 删除帖子 | PostDetailController.deleteButton | DELETE /api/bbs/post/{id} | ✅ 已实现 |
| 置顶帖子 | PostDetailController.topButton | POST /api/bbs/post/{id}/top | ✅ 已实现 |
| 加精帖子 | PostDetailController.featureButton | POST /api/bbs/post/{id}/feature | ✅ 已实现 |

---

## 测试步骤清单

### 1. 注册学生 1
### 2. 学生 1 登录
### 3. 打开帖子列表
### 4. 发布新帖子
### 5. 打开帖子详情
### 6. 发表评论
### 7. 编辑帖子
### 8. 删除帖子
### 9. 学生 2 登录
### 10. 学生 2 发布帖子
### 11. 学生 2 看学生 1 的帖子
### 12. 管理员登录
### 13. 管理员看学生 2 的帖子
### 14. 管理员置顶帖子
### 15. 管理员加精帖子
### 16. 管理员删除帖子
### 17. 禁言学生 1 登录
### 18. 禁言学生 1 尝试发帖
