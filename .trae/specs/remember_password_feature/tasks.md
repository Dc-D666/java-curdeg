# 记住密码功能 - 实施计划

## 概述
在登录页面添加"记住密码"复选框，实现用户名密码的本地保存和自动填充功能。

---

## 任务列表

### [x] Task 1: 更新 login-view.fxml 添加"记住密码"复选框
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在密码框下方、登录按钮上方添加 CheckBox
  - fx:id 设置为 rememberPasswordCheckBox
  - 文本显示为"记住密码"
  - 位置在账号密码框底下，按钮上方
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: CheckBox 元素添加成功
  - `programmatic` TR-1.2: fx:id 配置正确
  - `human-judgement` TR-1.3: 位置在账号密码框底下
- **Notes**: 文件路径：java-fx/src/main/resources/com/teach/javafx/base/login-view.fxml

### [x] Task 2: 更新 LoginController 添加字段和注入
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加 @FXML 注入 rememberPasswordCheckBox
  - 添加 Preferences 相关的常量：
    - PREFS_NODE: "com.teach.javafx.login"
    - KEY_USERNAME: "username"
    - KEY_PASSWORD: "password"
    - KEY_REMEMBER: "rememberPassword"
  - 在类开头添加这些常量
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-2.1: CheckBox 注入成功
  - `programmatic` TR-2.2: 常量定义正确
- **Notes**: 文件路径：java-fx/src/main/java/com/teach/javafx/controller/base/LoginController.java

### [x] Task 3: 实现 loadSavedCredentials 方法
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在 initialize 方法开头调用 loadSavedCredentials()
  - 实现 loadSavedCredentials 方法：
    - 使用 Preferences.userRoot().node(PREFS_NODE) 获取 Preferences
    - 读取 rememberPassword 的值
    - 如果为 true，读取 username 和 password 并填充到输入框
    - 设置 rememberPasswordCheckBox 的选中状态
- **Acceptance Criteria Addressed**: AC-3, AC-4
- **Test Requirements**:
  - `programmatic` TR-3.1: 方法实现正确
  - `programmatic` TR-3.2: 在 initialize 中被调用
  - `programmatic` TR-3.3: 能正确读取并填充
- **Notes**: 文件路径：java-fx/src/main/java/com/teach/javafx/controller/base/LoginController.java

### [x] Task 4: 实现 saveCredentials 方法
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 在登录成功后（msg == null 时）调用 saveCredentials()
  - 实现 saveCredentials 方法：
    - 获取 rememberPasswordCheckBox 的选中状态
    - 如果选中：
      - 保存 username, password, rememberPassword 为 true
    - 如果不选中：
      - 清除 password（设为空字符串）
      - 保存 rememberPassword 为 false
- **Acceptance Criteria Addressed**: AC-2, AC-5
- **Test Requirements**:
  - `programmatic` TR-4.1: 方法实现正确
  - `programmatic` TR-4.2: 在登录成功后被调用
  - `programmatic` TR-4.3: 保存/清除逻辑正确
- **Notes**: 文件路径：java-fx/src/main/java/com/teach/javafx/controller/base/LoginController.java

---

## 总体实施顺序
1. Task 1 (更新 FXML)
2. Task 2 (添加字段和常量)
3. Task 3 (实现加载功能)
4. Task 4 (实现保存功能)
