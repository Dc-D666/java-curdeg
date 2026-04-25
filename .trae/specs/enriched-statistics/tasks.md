# 校园论坛数据统计分析模块 - 实现计划

## 模块划分
统计页面采用Tab布局，分为以下几个主要模块：
1. 数据概览（新增）
2. 用户统计（现有基础上扩展）
3. 内容统计（现有基础上扩展）
4. 互动统计（新增）
5. 内容安全统计（新增）

---

## [ ] Task 1: 后端 - 数据概览API
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 实现获取关键指标数据的API
  - 指标包括：用户总数、今日新增用户、本月活跃用户、帖子总数、今日新增帖子、评论总数、今日新增评论、待审核内容数
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic`: API返回数据包含8个关键指标
  - `human-judgment`: 数据准确性验证
- **Notes**: 使用现有Repository进行聚合查询

## [ ] Task 2: 后端 - 用户统计API（扩展）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 实现用户增长趋势API（支持7天/30天/90天）
  - 实现用户类型分布API
  - 实现活跃用户排行榜API（支持按发帖/评论/综合排序）
  - 实现用户活跃度分布API
  - 实现封禁用户统计API
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `programmatic`: 用户增长API支持时间范围参数
  - `programmatic`: 活跃用户API支持排序方式参数
  - `human-judgment`: 数据格式正确

## [ ] Task 3: 后端 - 内容统计API（扩展）
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 实现发帖趋势API（支持7天/30天/90天）
  - 实现板块发帖分布API
  - 实现热门帖子排行榜API（支持按点赞/评论/浏览/收藏排序，Top20）
  - 实现帖子状态分布API
  - 实现图片发帖占比API
- **Acceptance Criteria Addressed**: AC-5, AC-6, AC-7
- **Test Requirements**:
  - `programmatic`: 发帖趋势API支持时间范围参数
  - `programmatic`: 热门帖子API支持排序维度参数
  - `human-judgment`: 数据格式正确

## [ ] Task 4: 后端 - 互动统计API（新增）
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 实现评论趋势API（支持7天/30天/90天）
  - 实现点赞趋势API（支持7天/30天/90天）
  - 实现收藏趋势API（支持7天/30天/90天）
  - 实现关注趋势API（支持7天/30天/90天）
  - 实现热门评论Top10 API
- **Acceptance Criteria Addressed**: AC-12
- **Test Requirements**:
  - `programmatic`: 各趋势API支持时间范围参数
  - `human-judgment`: 数据格式正确

## [ ] Task 5: 后端 - 内容安全统计API（新增）
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 实现审核统计概览API
  - 实现审核趋势API（支持7天/30天/90天）
  - 实现违规类型分布API
  - 实现举报统计API（含类型分布）
- **Acceptance Criteria Addressed**: AC-8, AC-9, AC-10, AC-11
- **Test Requirements**:
  - `programmatic`: 审核趋势API支持时间范围参数
  - `human-judgment`: 数据格式正确

## [ ] Task 6: 后端 - Repository层扩展
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在BbsPostRepository中添加新的查询方法
  - 在BbsCommentRepository中添加新的查询方法
  - 在UserRepository中添加新的查询方法
  - 在BbsReportRepository中添加新的查询方法
  - 在BbsModerationLogRepository中添加新的查询方法
  - 在BbsLikeRepository中添加新的查询方法
  - 在BbsFavoriteRepository中添加新的查询方法
  - 在BbsFollowRepository中添加新的查询方法
- **Acceptance Criteria Addressed**: AC-1 ~ AC-12
- **Test Requirements**:
  - `programmatic`: 各Repository方法能正确执行查询
  - `human-judgment`: 查询结果符合预期

## [ ] Task 7: 后端 - StatisticsController扩展
- **Priority**: P0
- **Depends On**: Task 6
- **Description**: 
  - 保持现有API不变（向前兼容）
  - 添加新的API端点对应Task 1-5
  - 所有API保持@PreAuthorize权限控制
- **Acceptance Criteria Addressed**: AC-13
- **Test Requirements**:
  - `programmatic`: 现有API仍然正常工作
  - `programmatic`: 新API端点可正常访问
  - `programmatic`: 权限控制有效

## [ ] Task 8: 前端 - 页面布局重构
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 重构statistics.fxml，采用新的Tab布局
  - Tab1: 数据概览
  - Tab2: 用户统计
  - Tab3: 内容统计
  - Tab4: 互动统计
  - Tab5: 内容安全统计
  - 保留现有的每日发帖量、热门帖子、活跃用户功能，迁移到对应Tab
- **Acceptance Criteria Addressed**: AC-1 ~ AC-12
- **Test Requirements**:
  - `human-judgment`: 页面布局美观，Tab切换流畅
  - `human-judgment`: 现有功能正常显示

## [ ] Task 9: 前端 - 数据概览UI实现
- **Priority**: P0
- **Depends On**: Task 1, Task 7
- **Description**: 
  - 实现8个关键指标卡片UI
  - 调用后端API获取数据并展示
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgment`: 指标卡片显示正常
  - `human-judgment`: 数据加载无错误

## [ ] Task 10: 前端 - 用户统计UI实现
- **Priority**: P0
- **Depends On**: Task 2, Task 7, Task 8
- **Description**: 
  - 实现用户增长趋势图（LineChart）
  - 实现用户类型分布图（PieChart）
  - 实现活跃用户排行榜（TableView，支持排序方式选择）
  - 实现用户活跃度分布图（BarChart）
  - 实现封禁用户统计
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4
- **Test Requirements**:
  - `human-judgment`: 图表显示正常
  - `human-judgment`: 排序切换功能正常

## [ ] Task 11: 前端 - 内容统计UI实现
- **Priority**: P0
- **Depends On**: Task 3, Task 7, Task 8
- **Description**: 
  - 实现发帖趋势图（LineChart）
  - 实现板块发帖分布图（PieChart/BarChart）
  - 实现热门帖子排行榜（TableView，支持排序维度选择，Top20）
  - 实现帖子状态分布图（PieChart）
  - 实现图片发帖占比统计（PieChart）
- **Acceptance Criteria Addressed**: AC-5, AC-6, AC-7
- **Test Requirements**:
  - `human-judgment`: 图表显示正常
  - `human-judgment`: 排序切换功能正常

## [ ] Task 12: 前端 - 互动统计UI实现
- **Priority**: P1
- **Depends On**: Task 4, Task 7, Task 8
- **Description**: 
  - 实现评论趋势图（LineChart）
  - 实现点赞趋势图（LineChart）
  - 实现收藏趋势图（LineChart）
  - 实现关注趋势图（LineChart）
  - 实现热门评论Top10（TableView）
- **Acceptance Criteria Addressed**: AC-12
- **Test Requirements**:
  - `human-judgment`: 图表显示正常
  - `human-judgment`: 时间范围切换功能正常

## [ ] Task 13: 前端 - 内容安全统计UI实现
- **Priority**: P1
- **Depends On**: Task 5, Task 7, Task 8
- **Description**: 
  - 实现审核统计概览（指标卡片）
  - 实现审核趋势图（LineChart，多系列）
  - 实现违规类型分布图（PieChart）
  - 实现举报统计（指标卡片+类型分布）
- **Acceptance Criteria Addressed**: AC-8, AC-9, AC-10, AC-11
- **Test Requirements**:
  - `human-judgment`: 图表显示正常
  - `human-judgment`: 数据加载无错误

## [ ] Task 14: 前端 - HttpRequestUtil扩展
- **Priority**: P0
- **Depends On**: Task 7
- **Description**: 
  - 添加调用新API的方法
  - 保持现有方法不变
- **Acceptance Criteria Addressed**: AC-1 ~ AC-12
- **Test Requirements**:
  - `programmatic`: 新方法能正确调用API
  - `programmatic`: 现有方法不受影响

## [ ] Task 15: 前端 - 模型类扩展
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 按需扩展或新增前端模型类以支持新的统计数据结构
- **Acceptance Criteria Addressed**: AC-1 ~ AC-12
- **Test Requirements**:
  - `human-judgment`: 数据能正确解析和显示

## [ ] Task 16: 集成测试与验证
- **Priority**: P0
- **Depends On**: Task 1-15
- **Description**: 
  - 完整测试所有新功能
  - 验证现有功能不受影响
  - 性能测试（确保API响应时间<3秒）
  - 权限验证
- **Acceptance Criteria Addressed**: AC-1 ~ AC-13
- **Test Requirements**:
  - `programmatic`: 所有API正常工作
  - `human-judgment`: 页面功能完整，用户体验良好

---

## 实施建议顺序
1. 后端基础设施（Task 6: Repository扩展）
2. 后端API开发（Task 1-5, Task 7）
3. 前端基础设施（Task 14-15）
4. 前端页面重构（Task 8）
5. 前端各模块实现（Task 9-13）
6. 集成测试（Task 16）
