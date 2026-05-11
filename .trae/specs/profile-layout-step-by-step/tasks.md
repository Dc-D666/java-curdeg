# 个人资料页面布局逐步优化 - 实现计划

## [ ] 第一步：增加统计卡片宽度
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 只修改统计卡片的prefWidth，从默认改为更大的值
  - 让三个统计卡片更宽一些
  - 不添加任何ColumnConstraints等复杂配置
  - 测试页面能正常打开
- **Acceptance Criteria Addressed**: FR-1, AC-1
- **Test Requirements**:
  - `human-judgement` TR-1.1: 页面能正常打开
  - `human-judgement` TR-1.2: 统计卡片更宽了
- **Notes**: 简单修改，不增加复杂度

## [ ] 第二步：增加基本信息区域表单字段宽度
- **Priority**: P0
- **Depends On**: 第一步
- **Description**: 
  - 将基本信息区域的TextField和TextArea的prefWidth从300改为400
  - 保持其他不变
  - 测试页面能正常打开
- **Acceptance Criteria Addressed**: FR-2, AC-2
- **Test Requirements**:
  - `human-judgement` TR-2.1: 页面能正常打开
  - `human-judgement` TR-2.2: 基本信息字段更宽了
- **Notes**: 只改prefWidth值

## [ ] 第三步：增加详细资料区域表单字段宽度
- **Priority**: P0
- **Depends On**: 第二步
- **Description**: 
  - 将详细资料区域的TextField、TextArea的prefWidth从280改为380
  - 将ComboBox的prefWidth从140改为180
  - 保持其他不变
  - 测试页面能正常打开
- **Acceptance Criteria Addressed**: FR-3, AC-3
- **Test Requirements**:
  - `human-judgement` TR-3.1: 页面能正常打开
  - `human-judgement` TR-3.2: 详细资料字段更宽了
- **Notes**: 只改prefWidth值
