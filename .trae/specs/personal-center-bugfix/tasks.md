# 个人中心Bug修复 - 实现计划

## [x] Task 1: 添加 ScrollPane 修复滚动问题
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 personal-profile.fxml，在最外层添加 ScrollPane
  - 将现有的 VBox 内容放置在 ScrollPane 中
  - 配置 ScrollPane 的 fitToWidth 属性为 true
- **Acceptance Criteria Addressed**: FR-1, AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 个人资料页面可以正常向下滚动
  - `programmatic` TR-1.2: 所有原有功能保持正常工作
- **Notes**: 保持所有原有 fx:id 和控制器绑定不变

## [x] Task 2: 优化统计卡片样式
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 personal-center.css 中的 stats-card 样式
  - 使用更深的背景色，增加文字对比度
  - 优化文字颜色，确保清晰可见
- **Acceptance Criteria Addressed**: FR-2, AC-2
- **Test Requirements**:
  - `human-judgement` TR-2.1: 统计卡片文字清晰可见，对比度足够
