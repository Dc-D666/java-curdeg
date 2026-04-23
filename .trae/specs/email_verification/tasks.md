# 邮箱验证功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 前期准备工作
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 确认后端项目情况（如果有）
  - 准备配置文件模板
  - 准备邮件模板内容
  - 准备测试邮箱账号
- **Acceptance Criteria Addressed**: [AC-8]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 配置文件模板已准备，包含QQ邮箱SMTP配置项
  - `human-judgement` TR-1.2: 邮件内容文案已准备
  - `human-judgement` TR-1.3: 测试邮箱账号已准备
- **Notes**: 这是开发者需要手动完成的前期工作

## [ ] Task 2: 前端UI修改 - FXML布局
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改 `register-view.fxml`，在邮箱输入框下方添加"发送验证码"按钮
  - 添加验证码输入框（初始隐藏）
  - 调整布局确保界面美观
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `human-judgement` TR-2.1: FXML文件中包含"发送验证码"按钮
  - `human-judgement` TR-2.2: FXML文件中包含验证码输入框
  - `human-judgement` TR-2.3: 验证码输入框初始状态为隐藏
- **Notes**: 参考现有UI风格保持一致性

## [ ] Task 3: 前端UI修改 - Controller逻辑
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 修改 `RegisterController.java`
  - 实现发送验证码按钮的点击事件
  - 实现60秒倒计时功能
  - 点击发送后显示验证码输入框
  - 修改注册逻辑，增加验证码参数
- **Acceptance Criteria Addressed**: [AC-1, AC-5]
- **Test Requirements**:
  - `programmatic` TR-3.1: 点击发送按钮后，验证码输入框显示
  - `programmatic` TR-3.2: 发送按钮有60秒倒计时，期间不可点击
  - `programmatic` TR-3.3: 倒计时结束后按钮恢复可点击状态
  - `human-judgement` TR-3.4: 注册时包含验证码参数
- **Notes**: 注意保持现有验证逻辑的顺序

## [ ] Task 4: 前端API调用 - HttpRequestUtil扩展
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 在 `HttpRequestUtil.java` 中新增发送验证码的方法
  - 修改注册方法，增加验证码参数
  - 处理发送验证码的响应
- **Acceptance Criteria Addressed**: [AC-2, AC-5]
- **Test Requirements**:
  - `programmatic` TR-4.1: 新增 `sendVerificationCode(email) 方法
  - `programmatic` TR-4.2: 修改 `registerUser()` 方法，增加 verificationCode 参数
  - `human-judgement` TR-4.3: 正确处理API响应和错误消息
- **Notes**: 接口路径需与后端协商一致

## [ ] Task 5: 后端邮件服务集成
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 集成QQ邮箱SMTP服务（使用授权码 wftomqvhpfggchga）
  - 实现邮件发送功能
  - 配置邮件模板
  - 处理邮件内容包含6位验证码和有效期说明
- **Acceptance Criteria Addressed**: [AC-2, AC-3, AC-8]
- **Test Requirements**:
  - `programmatic` TR-5.1: QQ邮箱SMTP配置正确
  - `programmatic` TR-5.2: 可以成功发送邮件
  - `human-judgement` TR-5.3: 邮件内容符合规范
  - `human-judgement` TR-5.4: 授权码通过配置文件管理，不硬编码
- **Notes**: 如果后端不在本代码库中，需要用户自行完成

## [ ] Task 6: 后端验证码管理
- **Priority**: P0
- **Depends On**: Task 5
- **Description**: 
  - 实现6位随机验证码生成
  - 实现验证码存储（使用Redis或内存缓存）
  - 实现验证码过期机制（5分钟）
  - 实现验证码验证逻辑
  - 实现验证后验证码失效逻辑
- **Acceptance Criteria Addressed**: [AC-2, AC-4, AC-7]
- **Test Requirements**:
  - `programmatic` TR-6.1: 验证码为6位纯数字
  - `programmatic` TR-6.2: 验证码5分钟后过期
  - `programmatic` TR-6.3: 验证通过后验证码立即失效
  - `programmatic` TR-6.4: 验证错误有次数限制
- **Notes**: 考虑使用Redis实现过期时间机制

## [ ] Task 7: 后端发送频率限制
- **Priority**: P1
- **Depends On**: Task 6
- **Description**: 
  - 实现同一邮箱发送频率限制
  - 实现同一IP发送频率限制
  - 实现验证错误次数限制
  - 超过限制后给出明确提示
- **Acceptance Criteria Addressed**: [AC-6]
- **Test Requirements**:
  - `programmatic` TR-7.1: 同一邮箱每分钟不超过限制次数
  - `programmatic` TR-7.2: 同一IP每分钟不超过限制次数
  - `programmatic` TR-7.3: 超过限制后返回正确的错误码
- **Notes**: 建议限制：同一邮箱每分钟1次，每小时5次；同一IP每小时10次

## [ ] Task 8: 后端API接口实现
- **Priority**: P0
- **Depends On**: Task 6, Task 7
- **Description**: 
  - 新增发送验证码接口 `POST /api/auth/sendVerificationCode`
  - 修改注册接口 `POST /api/auth/registerUser`（或新增带验证码的新接口）
  - 定义清晰的错误码和响应格式
- **Acceptance Criteria Addressed**: [AC-2, AC-4, AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `programmatic` TR-8.1: 发送验证码接口正常工作
  - `programmatic` TR-8.2: 注册接口验证验证码
  - `programmatic` TR-8.3: 各种错误情况有明确的错误码和消息
- **Notes**: 与前端约定好接口格式

## [ ] Task 9: 前端错误提示优化
- **Priority**: P1
- **Depends On**: Task 3, Task 4
- **Description**: 
  - 完善各种错误情况的提示信息
  - 确保用户友好的提示内容
  - 验证码输入框的状态反馈
- **Acceptance Criteria Addressed**: [AC-5, AC-6, AC-7]
- **Test Requirements**:
  - `human-judgement` TR-9.1: 错误提示清晰明确
  - `human-judgement` TR-9.2: 用户知道下一步操作
- **Notes**: 参考现有代码中的 MessageDialog

## [ ] Task 10: 整体功能测试
- **Priority**: P0
- **Depends On**: Task 8, Task 9
- **Description**: 
  - 完整流程测试：发送验证码 → 输入验证码 → 注册成功
  - 各种异常情况测试
  - 邮件发送测试
  - 边界条件测试
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7, AC-8]
- **Test Requirements**:
  - `programmatic` TR-10.1: 完整流程测试通过
  - `programmatic` TR-10.2: 各种异常情况处理正确
  - `human-judgement` TR-10.3: 边界条件测试通过
- **Notes**: 需要手动测试和验证
