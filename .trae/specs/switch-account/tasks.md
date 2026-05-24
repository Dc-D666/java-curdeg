# 切换账号功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 修改FXML布局 - 新增"切换账号"菜单
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在main-frame.fxml中，在menuAdmin后新增menuSwitchAccount菜单
  - 菜单文本为"切换账号"
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 检查FXML中新增了menuSwitchAccount元素
  - `human-judgement` TR-1.2: 检查菜单在正确位置

## [x] Task 2: 在MainFrameController中初始化"切换账号"子菜单
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加@FXML字段menuSwitchAccount
  - 在initialize()方法中，初始化五个子菜单项
  - 子菜单项ID和文本为：super、admin1、admin2、user1、user2
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-2.1: 检查menuSwitchAccount字段存在
  - `programmatic` TR-2.2: 检查五个子菜单项正确初始化

## [x] Task 3-4: 实现账号切换完整逻辑
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 添加switchToAccount()方法，用于切换到指定用户
  - 在初始化时获取当前用户名，禁用对应的菜单项
  - 为每个子菜单项添加事件处理
- **Acceptance Criteria Addressed**: AC-3, AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-3.1: 检查当前用户菜单项被禁用
  - `programmatic` TR-3.2: 检查点击非当前用户菜单项可用

## [ ] Task 4: 实现账号切换逻辑
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - switchToAccount(String username)方法实现登出和登录逻辑
  - 调用HttpRequestUtil.login()使用密码"123456"
  - 成功后加载主界面
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-4.1: 检查使用正确的密码登录
  - `programmatic` TR-4.2: 检查成功后显示主界面
