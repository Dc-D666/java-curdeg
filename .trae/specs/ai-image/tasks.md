# 一键配图功能 - The Implementation Plan (Decomposed and Prioritized Task List)

## [x] Task 1: 后端配置与模型扩展
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在application.yml中添加Cogview-3-Flash相关配置
  - 扩展ModerationConfig配置类，添加图片生成API配置
  - 配置API URL、Key、模型名称、超时时间
- **Acceptance Criteria Addressed**: AC-11
- **Test Requirements**:
  - ✅ 配置正确加载，可通过配置类读取所有参数
  - ✅ application.yml中新增配置项完整
- **Notes**: 复用现有的RestTemplate配置

## [x] Task 2: 后端DTO与Service层实现
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建图片生成请求DTO（包含prompt、size等）
  - 创建图片生成响应DTO
  - 创建AiImageService服务类，实现调用Cogview-3-Flash API
  - 实现图片URL获取与上传到本地服务器的逻辑
  - 添加完善的日志记录
- **Acceptance Criteria Addressed**: AC-11
- **Test Requirements**:
  - ✅ 可正确构建API请求并解析响应
  - ✅ 可处理API错误响应并返回友好信息
  - ✅ 代码结构清晰，复用现有上传机制
- **Notes**: 参考AiWriteService的实现模式

## [x] Task 3: 后端Controller层实现
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建图片生成API端点（POST /api/bbs/ai-image/generate）
  - 实现请求验证与权限控制
  - 正确返回图片URL或错误信息
  - 统一使用DataRequest/DataResponse格式
- **Acceptance Criteria Addressed**: AC-6, AC-7, AC-10, AC-11
- **Test Requirements**:
  - ✅ API端点可正确访问并返回响应
  - ✅ 权限控制正常工作（需要登录）
  - ✅ 错误响应格式正确
- **Notes**: 复用现有的Controller基类

## [x] Task 4: 前端FXML布局扩展
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 在post-publish.fxml的图片附件区域添加“一键配图”按钮
  - 创建配图生成对话框的FXML文件（ai-image-dialog.fxml）
  - 对话框包含：尺寸选择ComboBox、提示词TextArea、生成Button、图片预览ImageView、确认/取消/重新生成按钮
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-4
- **Test Requirements**:
  - ✅ 界面布局美观，控件位置合理
  - ✅ 对话框UI元素完整
- **Notes**: 参考现有FXML的布局风格

## [x] Task 5: 前端Controller与对话框实现
- **Priority**: P1
- **Depends On**: Task 4
- **Description**: 
  - 创建AiImageDialogController控制器类
  - 实现对话框初始化逻辑
  - 实现自动从标题/内容提取提示词
  - 实现尺寸选择下拉框功能
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - ✅ 提示词自动提取功能正常
  - ✅ 尺寸下拉框选项正确
  - ✅ 对话框打开/关闭流畅
- **Notes**: 复用现有的JavaFX对话框模式

## [x] Task 6: 前端HttpRequestUtil扩展
- **Priority**: P1
- **Depends On**: Task 3
- **Description**: 
  - 在HttpRequestUtil中添加aiImageGenerate方法
  - 实现调用后端图片生成API
  - 处理响应与错误
- **Acceptance Criteria Addressed**: AC-6, AC-10
- **Test Requirements**:
  - ✅ API调用成功返回图片URL
  - ✅ 错误处理完善
- **Notes**: 复用现有的HTTP请求模式

## [x] Task 7: 前端图片生成与预览功能
- **Priority**: P1
- **Depends On**: Task 5, Task 6
- **Description**: 
  - 实现“生成”按钮点击事件
  - 显示加载状态（参考AI写作助手的进度条）
  - 图片生成成功后在预览区域显示
  - 实现重新生成功能
- **Acceptance Criteria Addressed**: AC-5, AC-6, AC-8
- **Test Requirements**:
  - ✅ 加载状态显示友好
  - ✅ 图片预览显示正常
  - ✅ 重新生成功能正常
- **Notes**: 参考AiWriteService的进度条实现

## [x] Task 8: 前端确认添加与集成
- **Priority**: P0
- **Depends On**: Task 7
- **Description**: 
  - 实现“确认添加”按钮功能
  - 将生成的图片通过现有上传机制保存
  - 添加到PostPublishController的图片列表
  - 更新图片预览区域
  - 实现“取消”按钮功能
  - 在PostPublishController中集成“一键配图”按钮
- **Acceptance Criteria Addressed**: AC-1, AC-7, AC-9
- **Test Requirements**:
  - ✅ 图片成功添加到帖子图片列表
  - ✅ 图片预览区域正确显示新增图片
  - ✅ 整体流程流畅自然
- **Notes**: 确保复用现有的图片上传和预览逻辑

## [x] Task 9: 整体测试与Bug修复
- **Priority**: P0
- **Depends On**: Task 8
- **Description**: 
  - 端到端完整流程测试
  - 编译验证
- **Acceptance Criteria Addressed**: AC-1 至 AC-11
- **Test Requirements**:
  - ✅ java-server 编译成功
  - ✅ java-fx 编译成功
  - ✅ 无严重Bug
- **Notes**: 参考现有的测试方式
