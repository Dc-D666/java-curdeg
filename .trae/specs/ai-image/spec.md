# 一键配图功能 - Product Requirement Document

## Overview
- **Summary**: 在校园论坛的发布帖子页面新增"一键配图"功能，调用智谱AI的Cogview-3-Flash模型，根据帖子标题和内容自动生成匹配的图片，并支持图片尺寸选择，生成后可一键添加到帖子图片中。
- **Purpose**: 解决用户发布帖子时配图困难、需要手动寻找图片的问题，提升帖子的视觉吸引力和发布效率。
- **Target Users**: 校园论坛的所有注册用户（学生、管理员等）。

## Goals
- 在发布帖子页面的图片附件区域新增"一键配图"按钮
- 支持根据帖子标题和内容生成AI配图
- 提供图片尺寸选择（1280x1280、1568x1056、1056x1568等推荐尺寸）
- 生成图片后显示预览
- 支持确认添加到帖子图片列表
- 复用现有的图片上传存储机制

## Non-Goals (Out of Scope)
- 不实现多张图片批量生成（单次1张）
- 不实现图片编辑功能
- 不实现图片风格选择（MVP版本只使用默认风格）
- 不实现历史生成图片记录
- 不实现图片质量调整

## Background & Context
- 项目已有完整的AI内容审核、AI搜索、AI写作助手功能，可复用AI API调用逻辑
- 已有完善的图片上传和预览功能
- 前端使用JavaFX + FXML，已有PostPublishController
- 后端使用Spring Boot + Spring Data JPA
- 发布帖子页面已有完整的UI结构（图片附件区域）
- 智谱AI Cogview-3-Flash API文档：https://docs.bigmodel.cn/api-reference/%E6%A8%A1%E5%9E%8B-api/%E5%9B%BE%E5%83%8F%E7%94%9F%E6%88%90

## Functional Requirements
- **FR-1**: 在发布帖子页面的图片附件区域新增"一键配图"按钮
- **FR-2**: 点击按钮弹出配图生成对话框
- **FR-3**: 对话框包含：图片尺寸选择下拉框、生成按钮、预览区域、确认/取消按钮
- **FR-4**: 图片尺寸提供预定义选项（1280x1280、1568x1056、1056x1568、1472x1088、1088x1472、1728x960、960x1728）
- **FR-5**: 自动从当前编辑的帖子标题和内容提取提示词
- **FR-6**: 用户可查看和编辑发送给AI的提示词
- **FR-7**: 点击"生成"按钮时调用Cogview-3-Flash API
- **FR-8**: 显示加载状态（进度提示）
- **FR-9**: 生成成功后在预览区域显示图片
- **FR-10**: 用户可选择"确认添加"将图片加入帖子图片列表
- **FR-11**: 用户可选择"取消"或"重新生成"
- **FR-12**: 生成的图片需通过现有上传接口保存到服务器
- **FR-13**: 提供完善的错误处理

## Non-Functional Requirements
- **NFR-1**: 图片生成请求响应时间应控制在30秒以内
- **NFR-2**: 生成的图片应与帖子内容相关，画面美观
- **NFR-3**: 前端界面响应流畅，无卡顿
- **NFR-4**: AI API调用失败时应有友好的错误提示
- **NFR-5**: 图片加载预览应快速流畅

## Constraints
- **Technical**: 
  - 复用现有的RestTemplate和AI配置框架
  - 使用JavaFX进行前端扩展
  - 后端使用Spring Boot
  - AI模型：Cogview-3-Flash（智谱AI）
  - API Key：68c1b48b1b0448fdb7e11e25e8ff107d.JyhwtU9wuCB1aAE5
  - 图片尺寸限制：长宽在1024px-2048px之间，且为32的整数倍，最大像素数不超过2^22
  - 复用现有的图片上传存储机制
- **Business**: 
  - 控制API调用成本
  - 不影响现有发布帖子功能的稳定性
- **Dependencies**: 
  - 智谱AI Cogview-3-Flash API服务
  - 现有的PostPublishController和图片上传功能
  - 现有的ModerationConfig配置（可扩展）

## Assumptions
- 智谱AI Cogview-3-Flash API可正常访问
- API Key有效且有足够配额
- 用户对AI生成图片有合理的质量预期
- 现有图片上传功能可正确处理AI生成的图片URL

## Acceptance Criteria

### AC-1: 一键配图按钮显示
- **Given**: 用户进入发布帖子页面
- **When**: 页面加载完成
- **Then**: 用户可以在图片附件区域看到"一键配图"按钮
- **Verification**: `human-judgment`

### AC-2: 配图对话框显示
- **Given**: 用户点击"一键配图"按钮
- **When**: 按钮被点击
- **Then**: 弹出配图生成对话框，包含尺寸选择、提示词编辑、生成按钮
- **Verification**: `human-judgment`

### AC-3: 提示词自动提取
- **Given**: 配图对话框打开
- **When**: 对话框初始化
- **Then**: 提示词输入框自动填充当前帖子的标题+内容摘要
- **Verification**: `programmatic`

### AC-4: 图片尺寸选择
- **Given**: 用户在配图对话框中
- **When**: 用户打开尺寸下拉框
- **Then**: 可以看到预定义的尺寸选项（1280x1280、1568x1056等）
- **Verification**: `programmatic`

### AC-5: 生成按钮点击
- **Given**: 用户已选择尺寸并确认提示词
- **When**: 用户点击"生成"按钮
- **Then**: 
  - 显示加载状态提示
  - "生成"按钮变为禁用状态
  - 防止重复点击
- **Verification**: `human-judgment`

### AC-6: 图片生成成功
- **Given**: AI API调用成功
- **When**: 图片生成完成
- **Then**: 
  - 在预览区域显示生成的图片
  - 显示"确认添加"和"重新生成"按钮
- **Verification**: `human-judgment`

### AC-7: 确认添加功能
- **Given**: 图片已生成并显示在预览区域
- **When**: 用户点击"确认添加"按钮
- **Then**: 
  - 图片通过现有上传接口保存到服务器
  - 添加到帖子图片列表
  - 在图片预览区域显示
  - 关闭配图对话框
- **Verification**: `programmatic`

### AC-8: 重新生成功能
- **Given**: 图片已生成并显示在预览区域
- **When**: 用户点击"重新生成"按钮
- **Then**: 
  - 清空预览区域
  - 重新调用AI生成新图片
- **Verification**: `programmatic`

### AC-9: 取消功能
- **Given**: 配图对话框打开
- **When**: 用户点击"取消"按钮
- **Then**: 关闭对话框，不做任何修改
- **Verification**: `programmatic`

### AC-10: 错误处理
- **Given**: AI API调用失败、网络异常或返回错误
- **When**: 用户点击"生成"按钮
- **Then**: 
  - 显示友好的错误提示信息
  - "生成"按钮恢复可用状态
  - 不影响现有图片列表
- **Verification**: `programmatic`

### AC-11: 后端API设计
- **Given**: 后端收到图片生成请求
- **When**: 调用智谱AI API
- **Then**: 
  - 正确构建请求格式（包含prompt、model、size等）
  - 使用提供的API Key进行认证
  - 正确解析API返回的图片URL
  - 将图片URL通过现有上传机制保存
- **Verification**: `programmatic`

## Open Questions
- [ ] 是否需要限制用户的AI生成图片使用频率？（建议MVP版本暂时不限制，观察使用情况）
- [ ] 是否需要支持更多图片尺寸自定义？（建议MVP版本只提供预定义选项）
- [ ] 是否需要在编辑帖子页面也使用一键配图？（建议后续版本添加）
- [ ] 是否需要为生成的图片添加水印？（建议MVP版本不实现）
