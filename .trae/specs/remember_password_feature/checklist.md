# 记住密码功能 - 验证清单

## UI 验证
- [x] login-view.fxml 已添加"记住密码"复选框
- [x] 复选框位置在账号密码框底下
- [x] 复选框 fx:id 配置正确

## 控制器验证
- [x] LoginController 已注入 rememberPasswordCheckBox
- [x] 已定义 Preferences 相关常量
- [x] loadSavedCredentials 方法已实现
- [x] saveCredentials 方法已实现
- [x] loadSavedCredentials 在 initialize 中被调用
- [x] saveCredentials 在登录成功后被调用

## 功能验证
- [ ] 登录页面显示"记住密码"复选框
- [ ] 勾选"记住密码"后登录成功，用户名和密码被保存
- [ ] 下次打开登录页面，用户名和密码自动填充
- [ ] 下次打开登录页面，"记住密码"复选框自动勾选
- [ ] 不勾选"记住密码"登录，已保存的密码被清除
- [ ] 前端能够编译通过
