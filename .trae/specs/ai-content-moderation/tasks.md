
# AI内容审核功能 - Implementation Plan

## [x] Task 1: 数据库设计与迁移
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 扩展bbs_post表，添加审核相关字段
  - 创建审核历史记录表（可选，用于审计）
  - 创建数据库迁移SQL脚本
  - 更新BbsPost实体类
- **Acceptance Criteria Addressed**: AC1
- **Test Requirements**:
  - `programmatic`: 数据库表结构包含新增字段
  - `programmatic`: 实体类映射正确
- **Notes**: 使用版本化SQL脚本，确保可回滚

## [x] Task 2: 添加HTTP客户端依赖
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在pom.xml中添加OkHttp或Spring WebClient依赖
  - 配置RestTemplate或WebClient Bean
- **Acceptance Criteria Addressed**: FR2
- **Test Requirements**:
  - `programmatic`: 依赖正确添加
  - `programmatic`: HTTP客户端Bean可正常注入
- **Notes**: 建议使用Spring WebClient（响应式，性能更好）

## [x] Task 3: 实现AI审核服务类
- **Priority**: P0
- **Depends On**: Task 2
- **Description**: 
  - 创建ContentModerationService类
  - 将example.py中的审核逻辑移植为Java实现
  - 实现审核结果解析
  - 添加错误处理和降级逻辑
- **Acceptance Criteria Addressed**: AC2, AC3, AC4
- **Test Requirements**:
  - `programmatic`: 能正确调用AI API并解析响应
  - `programmatic`: API调用失败时有降级处理
- **Notes**: API密钥从配置文件读取，不要硬编码

## [x] Task 4: 改造发帖流程
- **Priority**: P0
- **Depends On**: Task 1, Task 3
- **Description**: 
  - 修改BbsPostService.createPost方法
  - 发帖后初始状态设为"审核中"
  - 异步调用AI审核服务
  - 审核完成后更新帖子状态
- **Acceptance Criteria Addressed**: AC1, AC2, AC3, AC4
- **Test Requirements**:
  - `programmatic`: 发帖后初始状态正确
  - `programmatic`: 异步审核任务能正常执行
- **Notes**: 使用@Async注解实现异步处理

## [x] Task 5: 实现帖子列表可见性控制
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 修改BbsPostRepository查询逻辑
  - 根据用户角色和帖子审核状态过滤可见帖子
  - 只有作者和管理员能看到非审核通过的帖子
- **Acceptance Criteria Addressed**: AC1
- **Test Requirements**:
  - `programmatic`: 普通用户只能看到审核通过的帖子
  - `programmatic`: 作者能看到自己所有状态的帖子
  - `programmatic`: 管理员能看到所有帖子
- **Notes**: 需要在查询时传入当前用户ID和角色

## [x] Task 6: 实现审核结果通知功能
- **Priority**: P1
- **Depends On**: Task 4
- **Description**: 
  - 审核完成后创建通知
  - 通知内容包含审核结果
  - 待人工审核时通知管理员
- **Acceptance Criteria Addressed**: AC2, AC3, AC4
- **Test Requirements**:
  - `programmatic`: 审核完成后通知创建成功
  - `programmatic`: 待人工审核时管理员收到通知
- **Notes**: 复用现有的BbsNotification系统

## [ ] Task 7: 前端修改 - 模型和UI状态显示
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 更新前端Post模型，添加审核状态字段
  - 实现审核状态文本和颜色映射
  - 修改PostListController.statusColumn的显示逻辑
- **Acceptance Criteria Addressed**: AC5
- **Test Requirements**:
  - `human-judgment`: 不同审核状态显示对应颜色
  - `human-judgment`: 状态文本显示正确
- **Notes**: 审核中/待人工审核-橙色，通过-绿色，违规-红色

## [x] Task 8: 实现管理员人工审核功能
- **Priority**: P1
- **Depends On**: Task 1, Task 4
- **Description**: 
  - 创建管理员审核接口
  - 实现审核通过/拒绝操作
  - 更新帖子状态和通知用户
  - （可选）添加管理员审核页面UI
- **Acceptance Criteria Addressed**: AC6
- **Test Requirements**:
  - `programmatic`: 管理员能审核帖子
  - `programmatic`: 审核后状态正确更新
  - `programmatic`: 用户收到审核结果通知
- **Notes**: 需要管理员权限验证

## [x] Task 9: 添加审核配置项
- **Priority**: P2
- **Depends On**: None
- **Description**: 
  - 在application.yml中添加审核相关配置
  - API地址、密钥、超时时间等
  - 支持开关控制是否启用AI审核
- **Acceptance Criteria Addressed**: FR2
- **Test Requirements**:
  - `programmatic`: 配置项能正确读取
  - `programmatic`: 可以通过配置关闭AI审核
- **Notes**: 为不同环境准备配置模板

## [ ] Task 10: 编写单元测试和集成测试
- **Priority**: P2
- **Depends On**: Task 3, Task 4, Task 5, Task 8
- **Description**: 
  - 为AI审核服务编写单元测试
  - 为发帖流程编写集成测试
  - 测试各种审核结果场景
  - 测试错误处理和降级逻辑
- **Acceptance Criteria Addressed**: AC1, AC2, AC3, AC4, AC6
- **Test Requirements**:
  - `programmatic`: 单元测试覆盖率 &gt; 70%
  - `programmatic`: 主要流程集成测试通过
- **Notes**: 使用Mock模拟AI API调用
