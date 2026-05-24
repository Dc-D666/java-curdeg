# Person 表个人资料字段实现 - 任务分解

## [ ] Task 1: 后端 - User 模型添加 Person 字段（Transient）
- **Priority**: P0
- **Depends On**: None
- **Description**: 在后端 User 模型中添加 Person 表的字段作为 Transient 字段，用于 API 返回
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic`: GET /api/bbs/user/me 返回数据中包含 Person 字段
- **Notes**: 添加字段：personName, personDept, personGender, personBirthday, personEmail, personPhone, personAddress, personIntroduce

## [ ] Task 2: 后端 - 修改 getCurrentUser 方法获取并设置 Person 数据
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 修改 BbsUserService.getCurrentUser 方法，从 PersonRepository 获取关联的 Person 数据并填充到 User 的 Transient 字段
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic`: GET /api/bbs/user/me 正确返回 Person 相关数据

## [ ] Task 3: 后端 - 修改 updateCurrentUser 方法支持更新 Person 字段
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 修改 BbsUserService.updateCurrentUser 方法，支持更新 Person 表的字段（name, dept, gender, birthday, email, phone, address, introduce）
- **Acceptance Criteria Addressed**: AC-2, AC-3
- **Test Requirements**:
  - `programmatic`: PUT /api/bbs/user/me 可以更新 Person 字段
  - `programmatic`: 邮箱格式验证正常工作

## [ ] Task 4: 前端 - User 模型添加 Person 字段
- **Priority**: P0
- **Depends On**: None
- **Description**: 在前端 User.java 模型中添加 Person 相关字段
- **Acceptance Criteria Addressed**: FR-3
- **Test Requirements**:
  - `programmatic`: User 模型包含所有新增字段

## [ ] Task 5: 前端 - 修改 HttpRequestUtil 支持传递 Person 字段
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 修改 updateUserProfile 方法，支持传递 Person 字段参数
- **Acceptance Criteria Addressed**: FR-5
- **Test Requirements**:
  - `programmatic`: 新的 updateUserProfile 方法支持所有参数

## [ ] Task 6: 前端 - 修改 personal-profile.fxml 添加 Person 字段界面
- **Priority**: P0
- **Depends On**: None
- **Description**: 在 FXML 文件中添加所有 Person 字段的展示和编辑组件
- **Acceptance Criteria Addressed**: FR-4
- **Test Requirements**:
  - `human-judgment`: 界面包含所有 Person 字段的控件

## [ ] Task 7: 前端 - 修改 PersonalProfileController 处理 Person 字段
- **Priority**: P0
- **Depends On**: Task 5, Task 6
- **Description**: 在控制器中添加 Person 字段的加载、编辑、保存逻辑
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `programmatic`: 字段可以正确加载和保存
