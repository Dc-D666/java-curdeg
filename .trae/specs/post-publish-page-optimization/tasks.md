# 发布帖子页面优化 - 实施计划 (Decomposed and Prioritized Task List)

## [ ] Task 1: 重新设计 post-publish.fxml 布局
- **Priority**: high
- **Depends On**: None
- **Description**:
  - 将页面划分为清晰的区域：基本信息区、内容编辑区、AI写作助手区（可折叠）、媒体附件区、操作按钮区
  - 使用 TitledPane 或分组标题组织相关控件
  - 优化控件间距和对齐方式，使其更加美观
  - 调整 AI 功能位置，放置在更合理的位置（如侧边或可折叠区域）
- **Acceptance Criteria Addressed**: AC-1, AC-2
- **Test Requirements**:
  - human-judgement: 新的布局应该清晰、有条理
  - human-judgement: AI 区域应该可以折叠/展开
- **Notes**: 参考 post-detail.fxml 的设计风格，保持一致性

## [ ] Task 2: 添加字数统计功能
- **Priority**: high
- **Depends On**: Task 1
- **Description**:
  - 在标题输入框下方添加字数统计标签，格式如 `当前字数: 0/256`
  - 在内容输入框下方添加字数统计标签，格式如 `当前字数: 0/20000`
  - 接近或达到限制时，统计标签变为红色警告色
  - 字数统计实时更新
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - programmatic: 标题输入时实时更新字数统计
  - programmatic: 内容输入时实时更新字数统计
  - human-judgement: 接近限制时有视觉警告
- **Notes**: 限制值与当前验证逻辑保持一致

## [ ] Task 3: 优化 AI 写作助手的集成和交互
- **Priority**: high
- **Depends On**: Task 1
- **Description**:
  - 将 AI 功能区域使用 TitledPane 包装，可折叠/展开
  - 改进 AI 生成后的内容对比功能，在对话框中显示原始内容和 AI 生成内容的对比
  - 提供更友好的接受/拒绝界面
  - 优化加载状态的显示
- **Acceptance Criteria Addressed**: AC-2, AC-7
- **Test Requirements**:
  - human-judgement: AI 区域可折叠
  - human-judgement: AI 生成结果有对比查看
  - human-judgement: 交互流畅且友好
- **Notes**: 保持与现有 AI 功能的兼容性

## [ ] Task 4: 优化图片上传功能
- **Priority**: high
- **Depends On**: Task 1
- **Description**:
  - 修改图片预览区域，支持多图网格显示（2列或3列布局）
  - 为每个图片添加删除按钮
  - 改进图片 URL 管理方式，使用更直观的界面
  - 添加图片上传进度提示
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - human-judgement: 多张图片可以预览和删除
  - human-judgement: 上传过程有进度提示
  - human-judgement: 图片展示美观
- **Notes**: 保持与现有的图片上传 API 兼容

## [ ] Task 5: 添加草稿保存功能（本地存储）
- **Priority**: medium
- **Depends On**: Task 2, Task 4
- **Description**:
  - 添加手动保存草稿按钮
  - 实现自动定期保存（每60秒）
  - 草稿保存到本地文件或本地存储
  - 页面加载时检查是否有草稿，提示用户恢复
  - 发布后清除草稿
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - human-judgement: 保存和恢复功能正常工作
  - human-judgement: 自动保存提示不干扰用户
- **Notes**: 优先实现本地存储方案，避免后端依赖

## [ ] Task 6: 添加内容预览功能
- **Priority**: medium
- **Depends On**: Task 5
- **Description**:
  - 添加"预览"按钮
  - 点击预览时，弹出对话框或新标签页展示帖子的实际效果
  - 预览内容包括标题、板块、内容和图片
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - human-judgement: 预览功能正常工作
  - human-judgement: 预览效果与实际帖子展示一致
- **Notes**: 可以复用 post-detail.fxml 的部分布局或单独创建预览对话框

## [ ] Task 7: 改进表单验证和交互反馈
- **Priority**: medium
- **Depends On**: Task 2
- **Description**:
  - 添加实时表单验证，在输入时即时提示（而不是只在发布时）
  - 改进错误提示的展示方式
  - 发布过程显示进度指示
  - 发布成功后提供预览帖子的选项
- **Acceptance Criteria Addressed**: AC-4, AC-6
- **Test Requirements**:
  - human-judgement: 验证反馈及时且友好
  - human-judgement: 发布过程有适当反馈

## [ ] Task 8: 优化响应式布局
- **Priority**: low
- **Depends On**: Task 1
- **Description**:
  - 确保布局在不同尺寸的窗口上都能良好显示
  - 合理设置最小和最大尺寸
  - 优化空间利用率
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - human-judgement: 在不同窗口大小下布局正常
- **Notes**: 这是最后进行的优化任务

## [ ] Task 9: 集成测试和修复
- **Priority**: high
- **Depends On**: Task 3, Task 4, Task 5, Task 6, Task 7, Task 8
- **Description**:
  - 进行完整的端到端功能测试
  - 修复发现的 Bug
  - 代码审查和优化
  - 确保所有功能正常工作
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7
- **Test Requirements**:
  - human-judgement: 所有功能正常工作
  - human-judgement: 用户体验良好
- **Notes**: 可以分批进行测试，在每个主要任务完成后进行相应的验证
