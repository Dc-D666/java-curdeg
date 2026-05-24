# 举报-受理-处理-通知简化闭环实现计划

## [x] Task 1: 后端创建 BbsReportService
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 BbsReportService 类
  - 实现提交举报业务逻辑
  - 实现查询我的举报列表
  - 实现管理员查询举报列表
  - 实现处理举报（删除内容/驳回举报）
- **Success Criteria**:
  - 所有举报相关业务逻辑实现完整
- **Test Requirements**:
  - `programmatic` TR-1.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-1.2: 检查业务逻辑完整性 ✓
- **Notes**: 复用现有框架（CommonMethod, SecurityUtils等）

## [x] Task 2: 后端创建 BbsNotificationService
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 BbsNotificationService 类
  - 实现查询未读通知数量
  - 实现查询我的通知列表
  - 实现标记通知已读
  - 实现创建通知的辅助方法
- **Success Criteria**:
  - 所有通知相关业务逻辑实现完整
- **Test Requirements**:
  - `programmatic` TR-2.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-2.2: 检查业务逻辑完整性 ✓
- **Notes**: 通知创建会在举报处理中调用

## [x] Task 3: 后端创建 BbsReportController
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - POST /api/bbs/report - 提交举报
  - GET /api/bbs/report/my-list - 查询我的举报
  - GET /api/bbs/report/admin-list - 管理员查询举报
  - POST /api/bbs/report/{id}/handle - 处理举报
- **Success Criteria**:
  - 所有举报接口正确实现权限控制
- **Test Requirements**:
  - `programmatic` TR-3.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-3.2: 检查权限控制（学生/管理员）✓
- **Notes**: 使用 @PreAuthorize 注解

## [x] Task 4: 后端创建 BbsNotificationController
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - GET /api/bbs/notification/unread-count - 查询未读数量
  - GET /api/bbs/notification/my-list - 查询我的通知
  - POST /api/bbs/notification/{id}/read - 标记已读
- **Success Criteria**:
  - 所有通知接口正确实现
- **Test Requirements**:
  - `programmatic` TR-4.1: IDE 诊断无错误 ✓
  - `human-judgement` TR-4.2: 检查接口参数校验 ✓
- **Notes**: 通知内容格式要清晰

## [x] Task 5: IDE 诊断检查
- **Priority**: P0
- **Depends On**: Task 1-4
- **Description**: 
  - 运行 GetDiagnostics 检查所有代码
- **Success Criteria**:
  - 无任何编译错误和警告
- **Test Requirements**:
  - `programmatic` TR-5.1: 诊断结果为空 ✓
- **Notes**: 这是后端完成的标志
