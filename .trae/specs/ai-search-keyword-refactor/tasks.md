
# AI搜索关键词重构优化 - Implementation Plan

## [ ] Task 1: 设计并实现关键词重构Prompt和AI调用
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 在AiSearchService中新增关键词重构方法
  - 设计高质量的Prompt，让AI分析用户问题并提取3-5个关键词
  - 复用现有的AI API调用机制
  - 实现关键词解析逻辑（从AI返回中提取关键词列表）
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `programmatic` TR-1.1: 给定用户问题，AI能成功返回3-5个关键词
  - `programmatic` TR-1.2: 关键词解析逻辑能正确处理AI的各种返回格式
  - `human-judgement` TR-1.3: 提取的关键词与用户问题相关，质量良好
- **Notes**: Prompt设计要考虑中文语境，提示AI生成适合论坛搜索的关键词

## [ ] Task 2: 实现扩展搜索和结果合并去重
- **Priority**: P0
- **Depends On**: Task 1
- **Description**:
  - 实现多次搜索功能：对每个重构关键词进行搜索
  - 实现结果合并逻辑：合并所有搜索结果
  - 实现去重逻辑：根据帖子ID去除重复项
  - 实现重排序逻辑：综合考虑相关性、热度、时间等因素
  - 配置可调整的搜索数量参数
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-2.1: 多次搜索能正常执行，不抛异常
  - `programmatic` TR-2.2: 去重逻辑能正确识别并去除重复帖子
  - `programmatic` TR-2.3: 排序逻辑能正常工作，结果有序
- **Notes**: 可以先用简单排序策略（时间+热度），后续再优化

## [ ] Task 3: 增加配置项和开关
- **Priority**: P1
- **Depends On**: None
- **Description**:
  - 在application.yml中新增AI搜索优化配置项
  - 新增配置类或扩展ModerationConfig
  - 配置项包括：功能开关、关键词数量、搜索结果数量、超时设置
  - 实现配置读取逻辑
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-3.1: 配置文件能正确加载
  - `programmatic` TR-3.2: 功能开关能正确控制是否启用关键词重构
  - `human-judgement` TR-3.3: 配置项设计合理，易于理解和修改
- **Notes**: 保持配置的向后兼容，默认可以先设为禁用

## [ ] Task 4: 重构AiSearchService集成新功能
- **Priority**: P0
- **Depends On**: Task 1, Task 2, Task 3
- **Description**:
  - 重构现有的aiSearch()方法（非流式）
  - 重构现有的流式搜索流程
  - 根据配置决定是否走关键词重构流程
  - 保留原流程作为fallback
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-4
- **Test Requirements**:
  - `programmatic` TR-4.1: 重构后的代码能正常编译和运行
  - `programmatic` TR-4.2: 开关关闭时，能正常使用原有流程
  - `programmatic` TR-4.3: 开关打开时，能正常使用新流程
- **Notes**: 代码重构要保持清晰，易于维护

## [ ] Task 5: 增加日志记录和监控
- **Priority**: P2
- **Depends On**: Task 4
- **Description**:
  - 在关键步骤增加日志记录
  - 记录关键词重构结果
  - 记录搜索过程的耗时
  - 记录AI调用次数和成本
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `human-judgement` TR-5.1: 日志记录清晰，便于调试
  - `programmatic` TR-5.2: 关键指标有日志输出
- **Notes**: 日志级别要合理，避免过多输出

## [ ] Task 6: 集成测试和验证
- **Priority**: P1
- **Depends On**: Task 4, Task 5
- **Description**:
  - 编写测试用例
  - 进行端到端测试
  - 验证新旧流程都能正常工作
  - 验证响应时间符合要求
  - 主观评估搜索质量提升
- **Acceptance Criteria Addressed**: AC-5, AC-6
- **Test Requirements**:
  - `programmatic` TR-6.1: 端到端测试通过
  - `programmatic` TR-6.2: 响应时间在可接受范围内
  - `human-judgement` TR-6.3: 搜索质量有明显提升
- **Notes**: 测试用例要覆盖典型场景和边界情况

