# 一键配图功能 - Verification Checklist

## 后端验证
- [ ] application.yml中已添加Cogview-3-Flash配置项
- [ ] ModerationConfig配置类已扩展图片生成API配置
- [ ] AiImageService服务类已创建
- [ ] 图片生成请求/响应DTO已创建
- [ ] 可正确调用智谱AI API并返回图片URL
- [ ] 图片URL可正确保存到本地服务器（复用现有上传机制）
- [ ] 后端Controller端点已创建（POST /api/bbs/ai-image/generate）
- [ ] 权限控制已实现（需要登录）
- [ ] 完善的错误处理与日志记录

## 前端验证
- [ ] post-publish.fxml中已添加"一键配图"按钮
- [ ] ai-image-dialog.fxml对话框布局已创建
- [ ] 对话框包含尺寸选择下拉框
- [ ] 对话框包含提示词编辑区域
- [ ] 对话框包含生成按钮与预览区域
- [ ] 对话框包含确认添加/取消/重新生成按钮
- [ ] AiImageDialogController已创建
- [ ] 提示词可自动从帖子标题和内容提取
- [ ] HttpRequestUtil中已添加aiImageGenerate方法
- [ ] 点击"生成"按钮可正确调用API
- [ ] 加载状态显示正常
- [ ] 生成的图片可在预览区域显示
- [ ] "确认添加"可将图片加入帖子图片列表
- [ ] "重新生成"功能正常工作
- [ ] "取消"功能正常工作
- [ ] 图片添加后可在主页面预览区域正确显示
- [ ] 错误场景有友好的提示信息

## 集成与流程验证
- [ ] 端到端完整流程测试通过
- [ ] 界面响应流畅无卡顿
- [ ] 复用了现有的图片上传和预览机制
- [ ] 代码风格与现有项目一致
- [ ] 文档与注释完整
