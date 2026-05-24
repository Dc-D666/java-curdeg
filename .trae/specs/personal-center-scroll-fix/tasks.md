# 个人中心滚动和统计卡片优化 - 实现计划

## [x] Task 1: 修复 ScrollPane 滚动问题
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修复 personal-profile.fxml 中的 ScrollPane 布局
  - 确保 ScrollPane 正确配置，内容可以正常滚动
  - 调整 padding 位置，确保布局正确
- **Acceptance Criteria Addressed**: FR-1, AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 个人资料页面可以正常向下滚动
  - `programmatic` TR-1.2: 所有原有功能保持正常工作

## [x] Task 2: 将统计卡片改为莫兰迪配色
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 personal-center.css 中的 stats-card 样式
  - 使用莫兰迪配色（如莫兰迪蓝、莫兰迪绿等）
  - 确保文字为白色，清晰可见
- **Acceptance Criteria Addressed**: FR-2, AC-2
- **Test Requirements**:
  - `human-judgement` TR-2.1: 统计卡片使用莫兰迪配色，文字白色清晰可见
