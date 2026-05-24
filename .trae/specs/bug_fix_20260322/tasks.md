# 项目 Bug 修复 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 修复 PostDetailController 事件过滤器
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 问题：当前事件过滤器使用 `==` 比较对象引用，导致回复按钮无法点击
  - 解决：改用 `instanceof` 检查或简化事件过滤逻辑
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 点击评论的回复按钮可以正常弹出回复对话框
  - `human-judgement` TR-1.2: 点击评论区空白处不会导致页面异常
- **Notes**: 不要过度限制事件，确保子组件可以正常工作

## [ ] Task 2: 完善 Post 模型类
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 添加 `authorId` 字段的 getter/setter 方法
  - 保持与 `userId` 字段的兼容性
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `programmatic` TR-2.1: 编译检查通过
  - `programmatic` TR-2.2: 所有字段都有对应的 getter/setter
- **Notes**: 使用 `@SerializedName` 注解保持序列化兼容性

## [ ] Task 3: 增强帖子详情页空值安全检查
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 在 `loadPostDetail()` 方法中增加更多空值检查
  - 特别是 `createTime`、`authorNickname` 等字段
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 帖子详情页可以正常加载，不抛异常
  - `human-judgement` TR-3.2: 即使某些字段为 null，页面也能正常显示
- **Notes**: 使用 Java 8 的 Optional 或三目运算符处理
