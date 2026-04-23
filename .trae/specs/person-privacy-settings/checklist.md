# Person 表隐私设置 - Verification Checklist

## 数据库验证
- [ ] SQL 脚本可以成功执行
- [ ] Person 表成功添加 8 个隐私设置字段
- [ ] 隐私设置字段有默认值 "PUBLIC"

## 后端验证
- [ ] Person.java 实体类添加了 8 个隐私设置字段
- [ ] User.java 实体类添加了隐私设置的 Transient 字段
- [ ] getCurrentUser() 方法返回包含隐私设置
- [ ] updateCurrentUser() 方法支持更新隐私设置
- [ ] 隐私设置更新时有合法值验证
- [ ] 可以正确判断两个用户是否相互关注
- [ ] 查看其他用户信息的 API 根据隐私设置过滤字段

## 功能验证
- [ ] 用户可以获取自己的隐私设置
- [ ] 用户可以更新自己的隐私设置
- [ ] 查看其他用户信息时，PRIVATE 字段不显示
- [ ] 查看其他用户信息时，FOLLOWING 字段仅在相互关注时显示
- [ ] 查看其他用户信息时，PUBLIC 字段正常显示
- [ ] 隐私设置的默认值为 "PUBLIC"
