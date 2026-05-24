# Person 表个人资料字段实现 - 验证清单

## 后端验证
- [ ] User.java 模型已添加 Person 相关的 Transient 字段
- [ ] BbsUserService.getCurrentUser() 方法正确加载 Person 数据
- [ ] BbsUserService.updateCurrentUser() 方法支持更新 Person 字段
- [ ] 邮箱格式验证逻辑已实现
- [ ] GET /api/bbs/user/me API 返回完整的 Person 字段数据
- [ ] PUT /api/bbs/user/me API 可以正确保存 Person 字段

## 前端验证
- [ ] User.java 模型已添加 Person 相关字段
- [ ] HttpRequestUtil.updateUserProfile() 方法支持传递 Person 字段
- [ ] personal-profile.fxml 包含所有 Person 字段的 UI 控件
- [ ] PersonalProfileController 能够正确加载 Person 字段
- [ ] PersonalProfileController 能够正确编辑和保存 Person 字段
- [ ] 性别字段使用下拉选择
- [ ] 生日字段使用日期控件（或正确的格式）
- [ ] 页面编辑模式下所有字段可以编辑
- [ ] 页面保存功能正常工作
- [ ] 所有字段在页面上正确展示

## 整体功能验证
- [ ] 用户可以在个人资料页面查看完整的个人信息
- [ ] 用户可以编辑并保存个人信息
- [ ] 数据修改后能够正确持久化到数据库
- [ ] 刷新页面后数据保持不变
