# 校园论坛系统 - 登录与主框架优化实现计划

## [x] 现状评估（前置检查）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 检查现有代码状态，确认哪些已经完成
  - 登录界面标题已正确设置为 "学生交流社区 - 登录"
  - 主框架菜单栏已包含所需菜单（首页、板块、我的、管理后台）
  - MainFrameController 已实现基本权限控制逻辑
  - 主窗口标题已设置为 "学生交流社区"
- **Success Criteria**:
  - 确认现有功能状态
- **Notes**: 现有代码基础良好，大部分需求已实现

---

## [x] Task 1: 完善 HttpRequestUtil - 添加获取当前用户信息接口
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 HttpRequestUtil 中新增 getCurrentUser() 方法
  - 调用后端 GET /api/bbs/user/me 接口
  - 使用 Task 异步处理
  - 返回用户信息（包含 authority 字段）
- **Success Criteria**:
  - 能成功获取当前登录用户信息
- **Test Requirements**:
  - `programmatic` TR-1.1: 方法能正常调用并返回用户数据
  - `human-judgement` TR-1.2: 代码风格与现有 HttpRequestUtil 保持一致
- **Notes**: 参考 getBoardList() 和 getPostList() 的实现模式

---

## [x] Task 2: 创建 User 模型类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 models 包下创建 User.java
  - 字段与后端 User 实体保持一致
  - 包含：studentId, nickname, avatarUrl, signature, postCount, commentCount, violationCount, isBanned, authority 等
- **Success Criteria**:
  - User 模型类创建完成
- **Test Requirements**:
  - `programmatic` TR-2.1: 所有字段正确定义
  - `human-judgement` TR-2.2: 代码风格与现有 Post.java、Board.java 保持一致
- **Notes**: 参考现有模型类的代码风格

---

## [x] Task 3: 优化 MainFrameController 权限控制
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 完善 initialize() 方法中的权限控制逻辑
  - 调用 getCurrentUser() 获取完整用户信息
  - 确保使用 Task 异步处理
  - 使用 Platform.runLater 更新 UI
  - 根据用户角色精确控制菜单可见性：
    - ROLE_STUDENT: 隐藏管理后台菜单
    - ROLE_ADMIN, ROLE_SUPER: 显示管理后台菜单
- **Success Criteria**:
  - 不同角色登录看到不同的菜单
- **Test Requirements**:
  - `programmatic` TR-3.1: 学生账号登录看不到 "管理后台" 菜单
  - `programmatic` TR-3.2: 管理员账号登录能看到 "管理后台" 菜单
  - `programmatic` TR-3.3: 使用 Task 异步处理，不阻塞 UI
  - `human-judgement` TR-3.4: 代码风格与现有 MainFrameController 保持一致
- **Notes**: 现有代码已有基础，只需完善和验证

---

## [x] Task 4: 验证登录流程
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 确保后端服务正常运行
  - 使用测试账号登录验证：
    - 学生账号（zhangsan / Test123456）
    - 管理员账号（admin / 123456）
  - 验证能正常进入主框架
  - 验证菜单权限控制正确
- **Success Criteria**:
  - 所有测试账号能正常登录并显示正确菜单
- **Test Requirements**:
  - `human-judgement` TR-4.1: 学生登录看不到管理后台菜单
  - `human-judgement` TR-4.2: 管理员登录能看到管理后台菜单
  - `human-judgement` TR-4.3: 界面响应流畅，无卡顿
- **Notes**: 确保后端服务先启动

---

## [ ] Task 5: 清理登录界面按钮（可选优化）
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 优化登录界面的按钮
  - 可以保留快速登录按钮，或简化为一个通用登录按钮
  - 确保不破坏现有登录逻辑
- **Success Criteria**:
  - 登录界面简洁易用
- **Test Requirements**:
  - `human-judgement` TR-5.1: 界面布局合理
  - `programmatic` TR-5.2: 登录功能正常
- **Notes**: 仅做小优化，不重写

---

## 约束规则回顾

### ✅ 必须遵守的规则
1. **复用框架能力**
   - ✅ 复用主框架标签页管理逻辑（已实现）
   - ✅ 复用 HttpRequestUtil（扩展而非重写）
   - ✅ 复用登录界面布局和逻辑（仅小优化）

2. **异步请求处理**
   - ✅ 所有后端请求使用 Task
   - ✅ UI 更新使用 Platform.runLater
   - ✅ 参考现有示例代码模式

3. **界面布局**
   - ✅ 使用简单布局（VBox, HBox, BorderPane）
   - ✅ 美化放后续阶段
