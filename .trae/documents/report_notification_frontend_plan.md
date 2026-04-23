# 举报-通知功能前端实现计划

## [ ] Task 1: 前端创建 Report 和 Notification 数据类
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 创建 Report.java（与后端 BbsReport 字段对应）
  - 创建 Notification.java（与后端 BbsNotification 字段对应）
- **Success Criteria**:
  - 数据类字段完整，有 getter/setter
- **Test Requirements**:
  - `programmatic` TR-1.1: IDE 诊断无错误
- **Notes**: 放在 java-fx 的 model 包下

## [ ] Task 2: 前端 HttpRequestUtil 新增请求方法
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 新增 submitReport() - 提交举报
  - 新增 getMyReportList() - 查询我的举报
  - 新增 getAdminReportList() - 管理员查询举报
  - 新增 handleReport() - 处理举报
  - 新增 getUnreadNotificationCount() - 查询未读数量
  - 新增 getMyNotificationList() - 查询我的通知
  - 新增 readNotification() - 标记已读
- **Success Criteria**:
  - 所有请求方法正确实现
- **Test Requirements**:
  - `programmatic` TR-2.1: IDE 诊断无错误

## [ ] Task 3: 前端修改帖子详情页 - 新增举报按钮
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 在帖子详情页底部新增"举报"按钮
  - 点击按钮弹出 TextInputDialog 输入举报理由
  - 调用后端接口提交举报（targetType=1）
  - 成功后弹出提示
- **Success Criteria**:
  - 可以正常举报帖子
- **Test Requirements**:
  - `programmatic` TR-3.1: IDE 诊断无错误
  - `human-judgement` TR-3.2: 测试举报功能

## [ ] Task 4: 前端创建"我的举报"页面
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建 my-report.fxml（FXML 界面）
  - 创建 MyReportController.java（控制器）
  - 实现加载我的举报列表
  - 实现分页（上一页/下一页）
- **Success Criteria**:
  - 可以查看自己的举报记录
- **Test Requirements**:
  - `programmatic` TR-4.1: IDE 诊断无错误

## [ ] Task 5: 前端创建"我的通知"页面
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建 my-notification.fxml（FXML 界面）
  - 创建 MyNotificationController.java（控制器）
  - 实现未读/已读切换按钮
  - 实现动态显示通知列表
  - 点击通知标记为已读并刷新
- **Success Criteria**:
  - 可以查看和管理通知
- **Test Requirements**:
  - `programmatic` TR-5.1: IDE 诊断无错误

## [ ] Task 6: 前端修改主框架 - 我的菜单
- **Priority**: P0
- **Depends On**: Task 4, Task 5
- **Description**: 
  - 在"我的"菜单下新增"我的举报"子菜单
  - 在"我的"菜单下新增"我的通知"子菜单
  - "我的通知"旁边显示未读数量（红色）
  - 点击菜单项打开对应标签页
- **Success Criteria**:
  - 可以通过主菜单访问举报和通知页面
- **Test Requirements**:
  - `programmatic` TR-6.1: IDE 诊断无错误

## [ ] Task 7: IDE 诊断检查
- **Priority**: P0
- **Depends On**: Task 1-6
- **Description**: 
  - 运行 GetDiagnostics 检查所有前端代码
- **Success Criteria**:
  - 无任何编译错误和警告
- **Test Requirements**:
  - `programmatic` TR-7.1: 诊断结果为空
