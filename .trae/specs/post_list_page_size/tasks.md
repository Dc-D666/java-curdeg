# 帖子列表每页条数选择功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 修改 FXML 布局 - 新增每页条数选择控件
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 post-list.fxml 的底部翻页区域新增 Label 和 ComboBox
  - Label 文本为"每页显示："
  - ComboBox 位于翻页按钮旁边
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-1.1: 检查 FXML 中是否新增了 Label 和 ComboBox 控件
  - `human-judgement` TR-1.2: 检查控件布局是否合理，与现有控件风格一致
- **Notes**: 在 pageInfoLabel 和 prevButton 之间插入新控件

## [x] Task 2: 修改 PostListController - 初始化和绑定下拉框
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 添加 @FXML 字段 pageSizeComboBox
  - 修改 currentPageSize 初始值为 20
  - 在 initialize() 中初始化下拉框选项（10, 20, 50）
  - 添加选择变化监听器
- **Acceptance Criteria Addressed**: AC-2, AC-3
- **Test Requirements**:
  - `programmatic` TR-2.1: 检查 currentPageSize 初始值为 20
  - `programmatic` TR-2.2: 检查下拉框初始化时包含 10, 20, 50 三个选项
  - `programmatic` TR-2.3: 检查默认选中 20

## [x] Task 3: 修改 PostListController - 实现切换逻辑
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在下拉框选择变化监听器中，更新 currentPageSize
  - 重置 currentPageNum 为 1
  - 调用 loadPostList() 刷新列表
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `programmatic` TR-3.1: 选择新值后 currentPageSize 更新为选中值
  - `programmatic` TR-3.2: 选择新值后 currentPageNum 重置为 1
  - `programmatic` TR-3.3: 选择新值后调用 loadPostList()

## [x] Task 4: 修改后端 BbsPostService - 调整默认 pageSize
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 BbsPostService.getPostList() 方法中的默认 pageSize 从 10 改为 20
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-4.1: 检查后端默认 pageSize 为 20
- **Notes**: 保持最大限制 50 不变
