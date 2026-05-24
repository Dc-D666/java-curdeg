# 认证功能改进 - 实施计划

## 概述
修复注册乐观锁异常，开发忘记密码功能，美化登录/注册/忘记密码三个认证页面。

---

## 任务列表

### [x] Task 1: 修复注册账号乐观锁异常
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 `AuthService.registerUser()` 方法
  - 将 `userRepository.saveAndFlush(u)` 替换为使用 EntityManager 的 `persist` 操作
  - 确保新 User 实体的 `@Version` 字段由 JPA 正确初始化为 0
  - 添加 `@Transactional`，使用 `entityManager.persist(u)` + `entityManager.flush()` 替代 `saveAndFlush`
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 注册新用户不再触发乐观锁异常 ✅
  - `programmatic` TR-1.2: 注册后自动登录成功 ✅
- **Notes**: 根因是 `User` 实体使用 `@Id private Integer personId`（非自增），Spring Data 的 `save` 方法在 ID 已设置时调用 `merge` 而非 `persist`

### [x] Task 2: 后端实现忘记密码 API
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 `AuthController.java` 新增 `POST /api/auth/resetPassword` 接口
  - 在 `AuthService.java` 新增 `resetPassword` 方法
  - 在 `UserRepository.java` 新增 `findByPersonEmail` 查询方法
  - 发送验证码复用已有 `POST /api/auth/sendEmailCode` 接口，type 传 "RESET_PASSWORD"
- **Acceptance Criteria Addressed**: AC-2, AC-3
- **Test Requirements**:
  - `programmatic` TR-2.1: API 接口能正确处理合法请求 ✅
  - `programmatic` TR-2.2: 错误码/验证失败返回正确错误信息 ✅
  - `programmatic` TR-2.3: 未注册邮箱返回"该邮箱未注册" ✅

### [x] Task 3: 前端实现忘记密码页面
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建 `forgot-password.fxml`：邮箱、验证码+发送按钮、新密码、确认密码、重置按钮、返回链接
  - 创建 `ForgotPasswordController.java`：发送验证码、重置密码、输入校验
  - 在 `HttpRequestUtil.java` 新增 `sendEmailCode(email, type)` 和 `resetPassword(email, code, newPassword)` 方法
  - 在 `LoginController.java` 新增 `onForgotPasswordClick()` 方法跳转
- **Acceptance Criteria Addressed**: AC-2, AC-3
- **Test Requirements**:
  - `programmatic` TR-3.1: 页面各控件正常工作 ✅
  - `programmatic` TR-3.2: 发送验证码有倒计时 ✅
  - `programmatic` TR-3.3: 重置成功跳转登录页 ✅
  - `programmatic` TR-3.4: 返回按钮回到登录页 ✅

### [x] Task 4: 美化登录页面 UI
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 重新设计 `login-view.fxml`：卡片式布局、白色卡片 400px maxWidth、#1890ff 主色、圆角阴影
  - 底部链接行："忘记密码?"和"注册新账号"
  - 保持所有 fx:id 不变
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `human-judgment` TR-4.1: 页面美观、现代化 ✅
  - `programmatic` TR-4.2: 所有原有功能正常（登录、记住密码） ✅
  - `programmatic` TR-4.3: "忘记密码"跳转正常 ✅
  - `programmatic` TR-4.4: "注册"跳转正常 ✅

### [x] Task 5: 美化注册页面 UI
- **Priority**: P1
- **Depends On**: Task 4（复用相同设计语言）
- **Description**: 
  - 重新设计 `register-view.fxml`：与登录页完全相同的设计语言
  - 保持所有 fx:id 和字段不变
  - 添加 ScrollPane 支持小屏幕
- **Acceptance Criteria Addressed**: AC-4, AC-6
- **Test Requirements**:
  - `human-judgment` TR-5.1: 页面与登录页风格统一 ✅
  - `programmatic` TR-5.2: 所有原有功能正常（注册、发送验证码、返回） ✅
  - `programmatic` TR-5.3: 所有验证逻辑正常工作 ✅

### [x] Task 6: 美化忘记密码页面 UI
- **Priority**: P1
- **Depends On**: Task 3, Task 4（复用相同设计语言）
- **Description**: 
  - 在 Task 3 创建的 `forgot-password.fxml` 基础上已使用统一设计语言
  - 与登录页完全相同的设计语言，无需额外修改
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `human-judgment` TR-6.1: 页面与登录页风格统一 ✅
  - `programmatic` TR-6.2: 所有功能正常 ✅

---

## 总体实施顺序（已完成）
1. ✅ Task 1（修复乐观锁）与 Task 2（后端忘记密码 API）并行
2. ✅ Task 3（前端忘记密码页）与 Task 4（美化登录页）并行
3. ✅ Task 5（美化注册页）
4. ✅ Task 6（忘记密码页已同步美化）
