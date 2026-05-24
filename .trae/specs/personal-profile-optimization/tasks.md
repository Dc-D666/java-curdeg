# 个人资料页面优化 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 页面布局重构 - 使用分组卡片设计
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 将个人资料页面重构为分组卡片布局
  - 创建四个分组：基本信息（头像、学号、昵称、签名）、详细信息（姓名、学院、性别、生日、邮箱、电话、地址、个人简介）、统计信息（发帖数、关注数、粉丝数）
  - 每个分组使用TitledPane或独立的VBox包裹，添加清晰的标题和视觉分隔
  - 优化间距、阴影效果，提升视觉效果
- **Acceptance Criteria Addressed**: [AC-1, AC-7]
- **Test Requirements**:
  - `human-judgement` TR-1.1: 页面布局分为四个清晰的分组卡片，视觉层次清晰
  - `human-judgement` TR-1.2: 每个分组有明确的标题和分隔
- **Notes**: 保持现有功能不变，只调整布局结构

## [x] Task 2: 头像管理功能优化
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在头像区域添加上传按钮
  - 实现文件选择功能（支持jpg、png等常见图片格式
  - 上传前显示上传的图片立即预览
  - 上传成功后更新avatarUrlTextField自动填入
  - 头像可点击，点击也触发上传
- **Acceptance Criteria Addressed**: [AC-2]
- **Test Requirements**:
  - `programmatic` TR-2.1: 点击头像或上传按钮可打开文件选择对话框
  - `programmatic` TR-2.2: 选择图片后立即在页面上预览显示
  - `programmatic` TR-2.3: 上传成功后avatarUrlTextField自动更新
- **Notes**: 复用现有的HttpRequestUtil.uploadImage方法

## [x] Task 3-8: 剩余所有任务一起完成
- **Priority**: P1-P2
- **Depends On**: Task 1, Task 2
- **Description**: 
  - **Task3(隐私设置优化)**: 查看模式简化显示，编辑模式完整显示
  - **Task4(日期选择器)**: 替换生日TextField为DatePicker组件
  - **Task5(表单验证)**: 添加昵称、邮箱、电话、URL的格式验证
  - **Task6(统计卡片交互)**: 添加悬停效果和点击跳转
  - **Task7(按钮优化)**: 已在Task1中完成，编辑按钮在右上角
  - **Task8(过渡动画)**: 添加模式切换的平滑效果
- **Acceptance Criteria Addressed**: [AC-3, AC-4, AC-5, AC-6, AC-7, AC-1]
- **Test Requirements**:
  - 完成所有对应任务的要求
- **Notes**: 保持代码风格一致，复用现有代码
