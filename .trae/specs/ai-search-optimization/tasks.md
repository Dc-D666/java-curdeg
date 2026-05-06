
# AI流式搜索卡死问题优化方案 - The Implementation Plan

## [ ] Task 1: 优化后端 - 添加内容缓冲和限流机制
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 修改 `BbsPostController.aiSearchStream()` 方法
  - 添加内容缓冲机制，合并短时间内的连续content事件
  - 设置合理的缓冲时间窗口（50-100ms）
  - 确保在AI响应完成或超时时立即发送剩余内容
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 验证后端正确合并content事件
  - `programmatic` TR-1.2: 验证缓冲机制不影响数据完整性
  - `human-judgment` TR-1.3: 验证SSE事件发送频率降低
- **Notes**: 缓冲时间建议设为80ms，平衡实时性和性能

## [ ] Task 2: 优化前端 - 实现内容合并和批量更新
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 修改 `PostSearchController.aiSearch()` 方法
  - 修改 `HttpRequestUtil.aiSearchStream()` 方法的回调处理
  - 前端添加内容缓冲和批量更新机制
  - 减少 Platform.runLater() 调用频率
  - 优化 Markdown 解析逻辑，避免重复解析
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-2.1: 验证前端正确合并内容
  - `programmatic` TR-2.2: 验证Platform.runLater()调用次数显著减少
  - `human-judgment` TR-2.3: 验证UI更新流畅无卡顿
- **Notes**: 前端缓冲时间建议与后端一致或稍长（100ms）

## [ ] Task 3: 整体集成和测试
- **Priority**: medium
- **Depends On**: Task 1, Task 2
- **Description**: 
  - 集成前后端优化
  - 进行完整功能测试
  - 验证性能提升效果
  - 检查所有功能正常工作
- **Acceptance Criteria Addressed**: AC-3, AC-4
- **Test Requirements**:
  - `human-judgment` TR-3.1: 验证应用不再卡死
  - `human-judgment` TR-3.2: 验证搜索体验流畅
  - `human-judgment` TR-3.3: 验证所有功能完整可用
- **Notes**: 建议进行多次测试，包括长文本、短文本等各种场景
