# 用户注册功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 修改后端注册API以支持社区用户字段
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 AuthService.registerUser 方法
  - 新增参数 studentId 和 nickname（使用username和perName作为值）
  - 确保User对象的studentId和nickname字段被正确设置
  - 默认注册用户角色为ROLE_STUDENT
- **Acceptance Criteria Addressed**: [AC-8, AC-9]
- **Test Requirements**:
  - `programmatic` TR-1.1: 验证User对象的studentId和nickname字段被正确设置
  - `programmatic` TR-1.2: 验证默认角色为STUDENT
- **Notes**: 保持现有API兼容性

## [x] Task 2: 创建注册界面FXML
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建新的 register-view.fxml 文件
  - 包含学号、昵称、邮箱、密码、重复密码输入框
  - 包含"注册"和"返回"按钮
  - 使用类似登录界面的布局风格
- **Acceptance Criteria Addressed**: [AC-1, AC-2]
- **Test Requirements**:
  - `human-judgement` TR-2.1: 检查所有表单字段和按钮都存在
  - `human-judgement` TR-2.2: 检查界面布局美观合理

## [x] Task 3: 创建RegisterController控制器
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建 RegisterController 类
  - 添加所有表单字段的@FXML注入
  - 添加返回登录界面的方法
- **Acceptance Criteria Addressed**: [AC-10]
- **Test Requirements**:
  - `programmatic` TR-3.1: 验证所有字段都有@FXML注入
  - `programmatic` TR-3.2: 验证返回功能正常工作

## [x] Task 4-6: 实现注册功能完整实现
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 实现学号校验：12位，不能为空
  - 实现昵称校验：1-16位，不能为空
  - 实现邮箱校验：不能为空，符合邮箱格式
  - 实现密码校验：不能为空，至少6位
  - 实现重复密码校验：与密码一致
  - 使用MessageDialog显示清晰的错误提示
- **Acceptance Criteria Addressed**: [AC-3, AC-4, AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `programmatic` TR-4.1: 验证每个字段的校验逻辑
  - `programmatic` TR-4.2: 验证错误提示正确显示

## [ ] Task 5: 在HttpRequestUtil中添加注册方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加 registerUser 方法
  - 封装DataRequest调用后端API
  - 处理返回结果
- **Acceptance Criteria Addressed**: [AC-8, AC-9]
- **Test Requirements**:
  - `programmatic` TR-5.1: 验证API调用封装正确

## [ ] Task 6: 实现注册功能逻辑
- **Priority**: P0
- **Depends On**: Task 4, Task 5
- **Description**: 
  - 实现点击注册按钮的事件处理
  - 首先进行前端校验
  - 校验通过后调用后端API
  - 注册成功后自动登录并跳转主界面
  - 注册失败时显示错误信息
- **Acceptance Criteria Addressed**: [AC-8, AC-9]
- **Test Requirements**:
  - `programmatic` TR-6.1: 验证完整的注册流程
  - `programmatic` TR-6.2: 验证注册成功后的自动登录跳转

## [x] Task 7: 修改LoginController以支持注册跳转
- **Priority**: P0
- **Depends On**: Task 2, Task 3
- **Description**: 
  - 修改 onRegisterButtonClick 方法
  - 从显示"开发中"改为跳转到注册界面
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `programmatic` TR-7.1: 验证点击注册按钮正确跳转
