# 评论回复功能实现计划

## [x] Task 1: 后端修改 BbsComment 实体类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsComment.java 中添加 @Transient 字段 replyList（类型为 List&lt;BbsComment&gt;）
- **Success Criteria**:
  - 实体类编译通过，新增字段正确标注为 @Transient
- **Test Requirements**:
  - `programmatic` TR-1.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-1.2: 检查代码注释和字段命名规范 ✓
- **Notes**: 保持其他字段不变

## [x] Task 2: 后端修改 BbsCommentService 的 getCommentsByPost 方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 查询顶级评论列表
  - 遍历顶级评论，根据 parentId 查询二级回复列表
  - 把二级回复列表设置到顶级评论的 replyList 字段
  - 填充所有评论的作者信息
- **Success Criteria**:
  - 方法返回包含二级回复的顶级评论列表
- **Test Requirements**:
  - `programmatic` TR-2.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-2.2: 检查业务逻辑的正确性 ✓
- **Notes**: 使用现有的 Repository 方法

## [x] Task 3: 后端修改 BbsCommentService 的 createComment 方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 新增 parentId 参数支持
  - 如果 parentId 不为 null，验证父评论是否存在且状态正常
  - 保持违禁词过滤和计数更新逻辑
- **Success Criteria**:
  - 支持发布顶级评论和二级回复
- **Test Requirements**:
  - `programmatic` TR-3.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-3.2: 检查业务逻辑的正确性 ✓
- **Notes**: 保持违禁词过滤功能

## [x] Task 4: 前端修改 Comment.java 数据类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 添加 parentId 字段
  - 添加 replyList 字段（类型为 List&lt;Comment&gt;）
  - 添加相应的 getter/setter 方法
- **Success Criteria**:
  - 数据类编译通过，字段完整
- **Test Requirements**:
  - `programmatic` TR-4.1: IDE 诊断无错误 ✓
- **Notes**: 保持其他字段不变

## [x] Task 5: 前端修改 HttpRequestUtil 支持二级回复
- **Priority**: P0
- **Depends On**: Task 4
- **Description**: 
  - 修改 publishComment 方法，新增 parentId 参数
  - 确保请求参数正确传递到后端
- **Success Criteria**:
  - 支持带 parentId 的评论发布
- **Test Requirements**:
  - `programmatic` TR-5.1: IDE 诊断无错误 ✓
- **Notes**: 保持向后兼容

## [x] Task 6: 前端修改 PostDetailController 的 loadCommentList 方法
- **Priority**: P0
- **Depends On**: Task 4, Task 5
- **Description**: 
  - 动态创建顶级评论时添加 "回复" 按钮
  - 顶级评论添加 replyVBox 用于显示二级回复
  - 遍历 replyList 显示二级回复（无回复按钮）
- **Success Criteria**:
  - 正确显示顶级评论和二级回复
- **Test Requirements**:
  - `programmatic` TR-6.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-6.2: 检查 UI 渲染效果 ✓
- **Notes**: 二级回复样式略作区分

## [x] Task 7: 前端实现回复功能
- **Priority**: P0
- **Depends On**: Task 6
- **Description**: 
  - 给 "回复" 按钮添加点击事件
  - 弹出 TextInputDialog 让用户输入回复内容
  - 调用后端接口发布二级回复
  - 成功后刷新评论列表
- **Success Criteria**:
  - 可以正常回复评论
- **Test Requirements**:
  - `programmatic` TR-7.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-7.2: 测试回复功能的完整流程 ✓
- **Notes**: 使用 Task + Platform.runLater 异步处理
