# 第五阶段：业务流程与数据统计完整操作 - 实现计划

## 概述
第五阶段主要实现：
- 数据库表扩展（违禁词表、举报表、通知表）
- 本地违禁词过滤 + 自动下架
- 自动更新计数补全（浏览数、点赞数）

---

## [ ] Task 1: 创建数据库模型类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 BbsSensitiveWord.java（违禁词模型）
  - 创建 BbsReport.java（举报模型）
  - 创建 BbsNotification.java（通知模型）
- **Success Criteria**:
  - 所有模型类创建完成
  - 字段定义正确
  - 使用 JPA 注解
- **Test Requirements**:
  - `programmatic` TR-1.1: 检查模型类创建
  - `programmatic` TR-1.2: 检查字段和注解

---

## [ ] Task 2: 创建 Repository 接口
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 创建 BbsSensitiveWordRepository.java
  - 创建 BbsReportRepository.java
  - 创建 BbsNotificationRepository.java
- **Success Criteria**:
  - 所有 Repository 接口创建完成
  - 继承 JpaRepository
- **Test Requirements**:
  - `programmatic` TR-2.1: 检查 Repository 接口

---

## [ ] Task 3: 创建违禁词过滤工具类
- **Priority**: P0
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 创建 SensitiveWordFilter.java
  - 实现 filterNormalWord() 方法
  - 实现 checkSevereWord() 方法
  - 使用 @PostConstruct 预加载违禁词到内存
- **Success Criteria**:
  - 工具类创建完成
  - 两个过滤方法实现正确
  - 预加载逻辑正确
- **Test Requirements**:
  - `programmatic` TR-3.1: 检查工具类创建
  - `programmatic` TR-3.2: 测试过滤逻辑

---

## [ ] Task 4: 修改帖子发布/编辑 - 违禁词过滤
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 修改 BbsPostService.createPost()
  - 修改 BbsPostService.updatePost()
  - 集成违禁词过滤
  - 处理普通违规（替换为 ***）
  - 处理严重违规（自动下架）
  - 更新 post_count 逻辑
- **Success Criteria**:
  - 普通违规词被替换
  - 严重违规帖子被下架
  - post_count 更新正确
- **Test Requirements**:
  - `programmatic` TR-4.1: 测试普通违规过滤
  - `programmatic` TR-4.2: 测试严重违规下架
  - `programmatic` TR-4.3: 测试 post_count 更新

---

## [ ] Task 5: 修改评论发布/编辑 - 违禁词过滤
- **Priority**: P0
- **Depends On**: Task 3
- **Description**: 
  - 修改 BbsCommentService.createComment()
  - 修改 BbsCommentService.updateComment()
  - 集成违禁词过滤
  - 处理普通违规（替换为 ***）
  - 处理严重违规（自动下架）
  - 更新 comment_count 逻辑
- **Success Criteria**:
  - 普通违规词被替换
  - 严重违规评论被下架
  - comment_count 更新正确
- **Test Requirements**:
  - `programmatic` TR-5.1: 测试普通违规过滤
  - `programmatic` TR-5.2: 测试严重违规下架
  - `programmatic` TR-5.3: 测试 comment_count 更新

---

## [ ] Task 6: 实现浏览数自动更新
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 修改 BbsPostService.getPostDetail()
  - 每次获取帖子详情时 view_count +1
  - 只对 status=1 的帖子更新
- **Success Criteria**:
  - 浏览数正常增加
  - 只对正常帖子更新
- **Test Requirements**:
  - `programmatic` TR-6.1: 测试浏览数增加

---

## [ ] Task 7: 实现点赞功能（简化版）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在 BbsPostController 新增点赞接口
  - 在 BbsPostService 实现点赞逻辑
  - 每次点赞 like_count +1
  - 只对 status=1 的帖子更新
  - 添加 @PreAuthorize 权限控制
- **Success Criteria**:
  - 点赞数正常增加
  - 权限控制正确
- **Test Requirements**:
  - `programmatic` TR-7.1: 测试点赞数增加
  - `programmatic` TR-7.2: 测试权限控制

---

## [ ] Task 8: 前端 - 完善 HttpRequestUtil
- **Priority**: P1
- **Depends On**: Task 7
- **Description**: 
  - 添加 likePost() 方法
- **Success Criteria**:
  - API 方法添加完成
- **Test Requirements**:
  - `programmatic` TR-8.1: 检查 API 方法

---

## [ ] Task 9: 前端 - 实现点赞功能
- **Priority**: P1
- **Depends On**: Task 8
- **Description**: 
  - 修改 PostDetailController.likeButton 点击事件
  - 调用点赞 API
  - 成功后更新按钮文本和状态
  - 显示成功提示
- **Success Criteria**:
  - 点赞功能正常
  - UI 状态更新正确
- **Test Requirements**:
  - `human-judgement` TR-9.1: 测试点赞流程

---

## [ ] Task 10: IDE 诊断检查
- **Priority**: P1
- **Depends On**: All previous tasks
- **Description**: 
  - 运行 GetDiagnostics 检查所有修改的文件
  - 确保无编译错误
- **Success Criteria**:
  - 无任何诊断错误或警告
- **Test Requirements**:
  - `programmatic` TR-10.1: 运行诊断检查并通过

---

## 数据库表设计参考

### BbsSensitiveWord (违禁词表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| word | String | 违禁词 |
| level | Integer | 等级 (1=普通, 2=严重) |
| createTime | Date | 创建时间 |
| updateTime | Date | 更新时间 |

### BbsReport (举报表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| reporterId | Long | 举报者 ID |
| targetType | Integer | 对象类型 (1=帖子, 2=评论) |
| targetId | Long | 对象 ID |
| reason | String | 举报理由 |
| status | Integer | 状态 (0=待处理, 1=已处理) |
| handlerId | Long | 处理人 ID |
| handleType | Integer | 处理方式 (1=删除, 2=驳回) |
| handleRemark | String | 处理备注 |
| handleTime | Date | 处理时间 |
| createTime | Date | 创建时间 |
| updateTime | Date | 更新时间 |

### BbsNotification (通知表)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键 |
| receiverId | Long | 接收者 ID |
| content | String | 通知内容 |
| isRead | Integer | 是否已读 (0=未读, 1=已读) |
| createTime | Date | 创建时间 |

---

## 功能实现优先级

### 核心必须实现 (P0)
1. 数据库模型类
2. Repository 接口
3. 违禁词过滤工具类
4. 帖子违禁词过滤
5. 评论违禁词过滤
6. 浏览数更新
7. 点赞功能

### 可选完善 (P1)
8. 前端 API 方法
9. 前端点赞 UI
10. 举报流程
11. 通知功能
12. 数据统计
