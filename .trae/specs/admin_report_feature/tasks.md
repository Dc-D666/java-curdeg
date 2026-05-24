# 管理员举报处理功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [ ] Task 1: 在 MainFrameController 中添加"举报处理"菜单项
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 MainFrameController.java 的 initialize() 方法中，添加"举报处理"菜单项
  - 菜单项名称为"admin-report"，标题为"举报处理"
  - 添加在"数据统计"菜单项之后
- **Acceptance Criteria Addressed**: [AC-1, AC-5]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 管理后台菜单中显示"举报处理"选项
  - `human-judgement` TR-1.2: 仅管理员和超级管理员可见此菜单项
  - `human-judgement` TR-1.3: 点击菜单项可以打开对应页面
- **Notes**: 参考第188-190行的菜单项添加方式

## [ ] Task 2: 在 HttpRequestUtil 中添加管理员举报相关请求方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加 getAdminReportList(int pageNum, int pageSize, Integer status) 方法
  - 添加 handleReport(Long id, int handleType, String handleRemark) 方法
  - 参考 getMyReportList 方法的实现
- **Acceptance Criteria Addressed**: [AC-2, AC-3, AC-4]
- **Test Requirements**:
  - `human-judgement` TR-2.1: getAdminReportList 方法能正确请求后端接口
  - `human-judgement` TR-2.2: handleReport 方法能正确提交处理请求
- **Notes**: 后端接口为 /api/bbs/report/admin-list 和 /api/bbs/report/{id}/handle

## [ ] Task 3: 创建 AdminReportController 控制器
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建 AdminReportController.java，继承 ToolController
  - 实现举报列表展示功能（分页）
  - 实现状态筛选下拉框（全部/待处理/已处理）
  - 实现处理功能（删除内容/驳回举报按钮）
  - 参考 MyReportController 的实现
- **Acceptance Criteria Addressed**: [AC-2, AC-3, AC-4]
- **Test Requirements**:
  - `human-judgement` TR-3.1: 举报列表正确显示所有字段
  - `human-judgement` TR-3.2: 状态筛选功能正常
  - `human-judgement` TR-3.3: 分页功能正常
  - `human-judgement` TR-3.4: 处理按钮只对未处理举报可见
  - `human-judgement` TR-3.5: 点击处理按钮弹出对话框
- **Notes**: 列表字段包括：举报类型、原因、状态、举报人、举报时间、处理人、处理时间、处理备注、操作按钮

## [ ] Task 4: 创建 admin-report.fxml 布局文件
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 创建 admin-report.fxml 布局文件
  - 包含状态筛选下拉框
  - 包含举报列表表格
  - 包含分页按钮
  - 参考 my-report.fxml 的布局
- **Acceptance Criteria Addressed**: [AC-2, AC-3]
- **Test Requirements**:
  - `human-judgement` TR-4.1: 页面布局完整，包含所有必要组件
  - `human-judgement` TR-4.2: 样式与现有页面保持一致
- **Notes**: 表格列需要包含"操作"列用于放置处理按钮

## [ ] Task 5: 完善 Report 模型类（如需）
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 检查 Report.java 是否包含所有需要的字段
  - 确保 reporterNickname 和 handlerNickname 字段存在
  - 如有缺失，添加相应字段和 getter/setter
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `human-judgement` TR-5.1: Report 模型包含所有需要的字段
- **Notes**: 检查 Report.java 第17-18行
