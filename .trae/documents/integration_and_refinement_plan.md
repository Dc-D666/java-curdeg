# 校园论坛系统 - 前后端联调细节补全与优化实现计划

## [x] 现状评估（前置检查）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 后端已有 authorAvatarUrl 字段和填充逻辑
  - 后端已有权限控制基础（@PreAuthorize 注解）
  - 前端已有 Post/Comment 模型，包含 authorAvatarUrl
  - 需要补全默认头像、自动刷新、按钮显示控制
- **Success Criteria**:
  - 确认现有功能状态
- **Notes**: 后端基础较好，主要需要前端优化

---

## [ ] Task 1: 后端补全 - 默认头像处理
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsPostService.fillPostAuthorInfo() 中添加默认头像逻辑
  - 在 BbsCommentService.fillCommentAuthorInfo() 中添加默认头像逻辑
  - 使用网络占位符头像：https://via.placeholder.com/50x50?text=User
- **Success Criteria**:
  - 头像为空时返回默认头像 URL
- **Test Requirements**:
  - `programmatic` TR-1.1: avatarUrl 为空时设置默认值
  - `programmatic` TR-1.2: 用 Postman 测试接口返回正确
- **Notes**: 修改 BbsPostService 和 BbsCommentService

---

## [ ] Task 2: 前端补全 - 帖子列表作者头像显示
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改 post-list.fxml："作者"列改为 ImageView + Label 的 HBox 组合
  - 修改 PostListController：实现带头像的单元格渲染
  - 处理头像加载失败的情况
- **Success Criteria**:
  - 帖子列表显示作者头像和昵称
- **Test Requirements**:
  - `human-judgement` TR-2.1: 头像和昵称正常显示
  - `human-judgement` TR-2.2: 头像为空时显示默认头像
- **Notes**: 使用 TableCell 自定义渲染

---

## [ ] Task 3: 前端补全 - 帖子详情作者头像显示
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 修改 post-detail.fxml：顶部作者信息改为 ImageView + Label 组合
  - 修改 PostDetailController：实现作者头像显示
  - 修改评论渲染逻辑：每条评论显示作者头像
- **Success Criteria**:
  - 详情页和评论都显示作者头像
- **Test Requirements**:
  - `human-judgement` TR-3.1: 帖子作者头像正常显示
  - `human-judgement` TR-3.2: 评论作者头像正常显示
- **Notes**: 保持代码风格一致

---

## [ ] Task 4: 前端补全 - 发布后自动刷新列表
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 修改 MainFrameController：
    - 新增 postListController 全局变量
    - 修改 changeContent/changeContentWithScene：打开 post-list 时保存 Controller
    - 关闭 post-list 时清空引用
  - 修改 PostPublishController：
    - 新增 mainFrameController 变量和 setMainFrameController() 方法
    - 发布成功后调用 postListController.loadPostList()（通过 Platform.runLater）
  - 修改 MainFrameController 打开发布页的入口：
    - 打开后调用 setMainFrameController(this)
- **Success Criteria**:
  - 发布帖子后列表自动刷新
- **Test Requirements**:
  - `programmatic` TR-4.1: 使用 Task 异步处理
  - `programmatic` TR-4.2: 使用 Platform.runLater 更新 UI
  - `human-judgement` TR-4.3: 发布成功后列表自动刷新
- **Notes**: 注意线程安全

---

## [ ] Task 5: 前端补全 - 详情页编辑/删除按钮隐藏
- **Priority**: P1
- **Depends On**: Task 4
- **Description**: 
  - 修改 PostDetailController.loadPostDetail()：
    - 成功回调中设置编辑/删除按钮 visible = false
    - （可选）设置置顶/加精按钮 visible = false
- **Success Criteria**:
  - 详情页编辑/删除按钮隐藏
- **Test Requirements**:
  - `human-judgement` TR-5.1: 编辑/删除按钮不显示
- **Notes**: 权限逻辑后续补充

---

## [ ] Task 6: IDE 诊断检查
- **Priority**: P0
- **Depends On**: Task 5
- **Description**: 
  - 使用 IDE 诊断工具检查代码
  - 修复任何诊断错误或警告
- **Success Criteria**:
  - 无诊断错误
- **Test Requirements**:
  - `programmatic` TR-6.1: GetDiagnostics 返回空列表
- **Notes**: 确保代码质量

---

## 约束规则回顾

### ✅ 必须遵守的规则
1. **复用框架能力**
   - 复用后端 SecurityUtils、JwtUtils、BCryptPasswordEncoder
   - 复用前端 ApiService（HttpRequestUtil）、Task、Platform.runLater、Alert
   - 复用主框架标签页管理

2. **异步请求处理**
   - 所有接口调用必须用 Task
   - UI 更新必须用 Platform.runLater

3. **权限控制三层闭环**
   - 前端界面级（菜单）
   - 前端按钮级（编辑/删除/置顶/加精）
   - 后端接口级（@PreAuthorize）
   - 后端数据级（操作前再次校验身份）

4. **每完成一个功能立即测试**
   - 细节补全后立即验证显示/刷新
   - 权限完善后立即做穿透测试
   - 全流程联调后立即做完整走查
