# AI检索功能性能优化 - Implementation Plan

## Overview

本任务列表描述解决AI检索功能导致应用卡死问题的具体实现方案。

---

## Task 1: 优化前端PostSearchController - 核心优化 [P0]

### Priority: P0
### Depends On: None

### Description

优化`java-fx/src/main/java/com/teach/javafx/controller/PostSearchController.java`中的`aiSearch()`方法，解决UI卡死问题。

#### 具体修改点：

1. **实现缓冲更新机制**（第652-660行）
   - 添加缓冲StringBuilder积累内容
   - 使用Timeline或AnimationTimer定期刷新UI（每150ms）
   - 流式过程中使用单个Text节点，而非每个片段创建新节点

2. **移除重复渲染**（第673-674行）
   - 流式过程中：只更新纯文本，不做Markdown解析
   - 完成时：只做一次完整的Markdown解析和渲染

3. **移除/减少日志输出**（多处）
   - 移除System.out.println调试日志
   - 保留必要的错误日志

4. **增加搜索取消机制**
   - 添加AtomicBoolean标记搜索是否继续
   - 切换搜索模式/点击新搜索时取消旧搜索

### Acceptance Criteria Addressed
- AC1: UI不卡死
- AC2: 搜索可取消
- AC3: 内容显示正常
- AC4: 日志输出减少
- AC5: 编译通过

### Test Requirements
- **programmatic**: 编译成功
- **human-judgment**: 实际测试搜索功能，检查UI响应性

### Notes
- 缓冲时间建议：150ms（平衡响应性和性能）
- 流式过程中使用单Text节点的setText()方法更新，比追加新节点更高效

---

## Task 2: 优化后端AiSearchService - 日志优化 [P1]

### Priority: P1
### Depends On: None

### Description

优化`java-server/src/main/java/cn/edu/sdu/java/server/services/AiSearchService.java`中的日志输出。

#### 具体修改点：

1. **减少流式内容日志**（第183行）
   - 移除或注释掉`log.info("接收到AI流式内容: " + content);`
   - 保留关键节点的日志（开始、完成、错误）

2. **减少其他调试日志**
   - 移除或降低级别一些频繁输出的日志

### Acceptance Criteria Addressed
- AC4: 日志输出减少
- AC5: 编译通过

### Test Requirements
- **programmatic**: 编译成功
- **human-judgment**: 检查运行时日志输出量

---

## Task 3: 优化后端BbsPostController - 日志优化 [P1]

### Priority: P1
### Depends On: None

### Description

优化`java-server/src/main/java/cn/edu/sdu/java/server/controllers/BbsPostController.java`中的日志输出。

#### 具体修改点：

1. **减少SSE事件日志**（第197行、第221行、第232行等）
   - 移除或注释掉详细的事件日志
   - 保留关键流程日志

### Acceptance Criteria Addressed
- AC4: 日志输出减少
- AC5: 编译通过

### Test Requirements
- **programmatic**: 编译成功

---

## Task 4: 编译与测试验证 [P0]

### Priority: P0
### Depends On: Task 1, Task 2, Task 3

### Description

完成所有代码修改后进行编译和测试验证。

#### 具体工作：

1. **编译前端项目**
   ```bash
   cd java-fx
   mvnw.cmd clean compile
   ```

2. **编译后端项目**
   ```bash
   cd java-server
   mvnw.cmd clean compile
   ```

3. **功能测试**
   - 测试AI搜索功能
   - 验证UI响应性
   - 检查内容显示是否正确

### Acceptance Criteria Addressed
- AC1: UI不卡死
- AC2: 搜索可取消
- AC3: 内容显示正常
- AC4: 日志输出减少
- AC5: 编译通过

### Test Requirements
- **programmatic**: 编译成功
- **human-judgment**: 实际功能测试验证

---

## Implementation Order

1. **Task 1** (P0): 优化前端 - 最关键，解决主要问题
2. **Task 2** (P1): 优化后端AiSearchService
3. **Task 3** (P1): 优化后端BbsPostController
4. **Task 4** (P0): 编译测试验证

---

## Risk & Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| 缓冲导致延迟感 | Medium | Low | 使用较短缓冲时间(150ms)，实际测试调整 |
| Markdown解析有bug | Medium | Medium | 仔细测试，保证最终渲染正确 |
| 取消机制不完善 | Low | Medium | 充分测试各种场景下的取消行为 |
