# Person 表隐私设置 - The Implementation Plan

## [ ] Task 1: 创建数据库更新 SQL 脚本
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 为 Person 表添加 8 个隐私设置字段
  - 字段命名：name_privacy, dept_privacy, gender_privacy, birthday_privacy, email_privacy, phone_privacy, address_privacy, introduce_privacy
  - 默认值设为 "PUBLIC"
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` SQL 脚本可以成功执行并添加字段
- **Notes**: 隐私设置枚举：PUBLIC（全员可见）, FOLLOWING（相互关注可见）, PRIVATE（仅自己可见）

## [ ] Task 2: 更新 Person 实体类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 Person.java 中添加 8 个隐私设置字段
  - 添加对应的 getter/setter
  - 设置默认值为 "PUBLIC"
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` 实体类字段添加成功

## [ ] Task 3: 更新 User 实体类（Transient 字段）
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在 User.java 中添加隐私设置的 Transient 字段
  - 添加对应的 getter/setter
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` User 类包含隐私设置字段

## [ ] Task 4: 更新 BbsUserService.getCurrentUser() 方法
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 在获取当前用户时，同时获取隐私设置并填充到 User 的 Transient 字段
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` getCurrentUser 返回包含隐私设置

## [ ] Task 5: 更新 BbsUserService.updateCurrentUser() 方法
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 支持更新隐私设置
  - 验证隐私设置值的合法性
  - 保存到 Person 表
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` 可以成功更新隐私设置

## [ ] Task 6: 创建判断相互关注的功能
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsUserService 或相关 Service 中添加方法
  - 判断两个用户是否相互关注
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` 可以正确判断相互关注关系

## [ ] Task 7: 创建查看其他用户个人信息的 API
- **Priority**: P1
- **Depends On**: Task 6
- **Description**: 
  - 创建新的 API 端点：GET /api/bbs/user/{userId}
  - 根据隐私设置过滤返回的字段
  - 检查访问权限
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `programmatic` 根据隐私设置正确过滤字段

## [ ] Task 8: 更新前端 HttpRequestUtil
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 更新 updateUserProfile 方法，支持传递隐私设置
  - 添加获取其他用户信息的方法
- **Acceptance Criteria Addressed**: AC-2, AC-3
- **Test Requirements**:
  - `programmatic` HTTP 请求工具支持隐私设置参数
