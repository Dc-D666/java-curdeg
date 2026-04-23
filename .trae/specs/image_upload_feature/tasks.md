# 图片上传功能 - 实现计划

## [x] Task 1: 创建数据库SQL脚本 - 为评论表添加图片字段
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建SQL脚本，为 `bbs_comment` 表添加 `image_urls` 字段
  - 字段类型：TEXT，可为空
  - 位置：在 `content` 字段之后
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `programmatic` TR-1.1: SQL脚本执行成功
  - `programmatic` TR-1.2: 表结构正确包含新字段
- **Notes**: 先给用户SQL脚本，用户确认后再执行其他任务

## [x] Task 2: 后端 - 更新评论实体类和Repository
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 更新 `BbsComment` 实体类，添加 `imageUrls` 字段
  - 使用 `@Column` 注解映射到数据库
  - 添加 getter 和 setter 方法
- **Acceptance Criteria Addressed**: [AC-3]
- **Test Requirements**:
  - `programmatic` TR-2.1: 实体类编译通过
  - `programmatic` TR-2.2: JPA映射正确
- **Notes**: 保持现有字段不变

## [x] Task 3: 后端 - 创建图片上传服务和控制器
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 `BbsFileService` 服务类
  - 实现图片保存功能，使用配置的 `attach.folder`
  - 按日期分目录存储（如：/teach-2025/20260410/）
  - 生成唯一文件名（UUID + 原始扩展名）
  - 创建 `BbsFileController` 控制器
  - 提供 `POST /api/bbs/file/upload-image` 接口
  - 验证图片格式（JPG, JPEG, PNG, GIF）
  - 限制文件大小（最大10MB）
  - 返回可访问的URL
- **Acceptance Criteria Addressed**: [AC-1]
- **Test Requirements**:
  - `programmatic` TR-3.1: 图片上传API可以正常访问
  - `programmatic` TR-3.2: 图片保存到正确位置
  - `programmatic` TR-3.3: 返回正确的URL格式
  - `programmatic` TR-3.4: 非图片格式被拒绝
  - `programmatic` TR-3.5: 超过10MB的文件被拒绝
- **Notes**: 参考 BaseService 中的文件存储实现

## [x] Task 4: 后端 - 更新评论服务支持图片
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 更新 `BbsCommentService.createComment()` 方法
  - 保存评论时同时保存图片URL
  - 更新评论查询方法，返回图片URL
- **Acceptance Criteria Addressed**: [AC-4]
- **Test Requirements**:
  - `programmatic` TR-4.1: 评论图片URL正确保存
  - `programmatic` TR-4.2: 评论查询返回图片URL
- **Notes**: 保持现有评论功能不变

## [x] Task 5: 前端 - 添加图片上传HTTP工具方法
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 `HttpRequestUtil` 中添加 `uploadImage()` 方法
  - 支持 MultipartFile 上传
  - 处理上传响应，返回图片URL
- **Acceptance Criteria Addressed**: [AC-1, AC-2]
- **Test Requirements**:
  - `programmatic` TR-5.1: HTTP方法可以正常调用
  - `programmatic` TR-5.2: 正确解析返回的URL
- **Notes**: 参考现有HTTP请求方法的实现

## [x] Task 6: 前端 - 发布帖子页面添加图片上传功能
- **Priority**: P0
- **Depends On**: Task 5
- **Description**: 
  - 修改 `post-publish.fxml`，添加图片上传按钮
  - 添加图片预览区域
  - 修改 `PostPublishController`
  - 实现图片选择功能
  - 实现图片上传功能
  - 上传成功后自动填充图片URL字段
  - 保持现有的URL输入框功能
- **Acceptance Criteria Addressed**: [AC-2, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-6.1: 可以选择本地图片
  - `human-judgement` TR-6.2: 图片可以预览
  - `human-judgement` TR-6.3: 图片上传成功
  - `human-judgement` TR-6.4: URL字段自动填充
  - `human-judgement` TR-6.5: 现有URL输入功能继续可用
- **Notes**: 不修改现有功能，只新增上传功能

## [ ] Task 7: 前端 - 帖子详情页评论添加图片上传功能
- **Priority**: P1
- **Depends On**: Task 5
- **Description**: 
  - 修改 `PostDetailController` 中的评论发布区域
  - 添加图片上传按钮
  - 添加图片预览
  - 实现评论图片上传
  - 发布评论时包含图片URL
- **Acceptance Criteria Addressed**: [AC-4, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-7.1: 评论可以上传图片
  - `human-judgement` TR-7.2: 评论发布成功包含图片
- **Notes**: 保持现有评论功能不变

## [ ] Task 8: 前端 - 帖子详情页显示评论图片
- **Priority**: P1
- **Depends On**: Task 4, Task 7
- **Description**: 
  - 修改 `PostDetailController` 中的评论显示
  - 解析评论的图片URL
  - 在评论内容下方显示图片
- **Acceptance Criteria Addressed**: [AC-5]
- **Test Requirements**:
  - `human-judgement` TR-8.1: 评论图片正确显示
  - `human-judgement` TR-8.2: 多张图片正确显示
- **Notes**: 参考帖子图片的显示方式

## [ ] Task 9: 集成测试和验证
- **Priority**: P0
- **Depends On**: [Task 3, Task 4, Task 6, Task 7, Task 8]
- **Description**: 
  - 执行所有测试用例
  - 验证所有现有功能不受影响
  - 验证新增功能正常工作
  - 修复发现的问题
- **Acceptance Criteria Addressed**: [AC-1, AC-2, AC-3, AC-4, AC-5, AC-6]
- **Test Requirements**:
  - `human-judgement` TR-9.1: 所有功能按验收标准正常工作
  - `human-judgement` TR-9.2: 没有引入新的bug
  - `human-judgement` TR-9.3: 用户体验良好
- **Notes**: 进行全面的回归测试
