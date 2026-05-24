
# AI搜索关键词重构优化 - Product Requirement Document

## Overview
- **Summary**: 优化现有AI搜索功能，在原有的全文搜索基础上，增加AI关键词重构步骤。AI先分析用户的自然语言问题，提取3-5个相关搜索关键词，再用这些关键词进行更广泛的搜索，最后把更丰富的搜索结果传给AI生成答案。
- **Purpose**: 解决当前AI搜索依赖精确关键词匹配、召回率有限的问题，提升AI搜索的理解能力和搜索质量。
- **Target Users**: 校园论坛所有使用AI搜索功能的用户，尤其是用自然语言提问的用户。

## Goals
- 提升AI搜索的召回率和相关性
- 让AI更好地理解用户的自然语言问题
- 在保持现有架构不变的前提下，提升搜索质量
- 支持新老模式切换（可配置）
- 保持响应时间在可接受范围内

## Non-Goals (Out of Scope)
- 不实现向量化搜索（后续阶段考虑）
- 不修改前端界面（保持UI不变）
- 不实现搜索历史功能
- 不改变AI回答生成的质量（只提升数据召回）

## Background & Context
- **现有系统**: 已实现基于全文搜索的AI搜索功能（ai-search-mvp）+ 流式输出（ai-search-streaming）
- **当前流程**: 用户问题 → 全文搜索匹配 → Top5帖子+评论 → 喂给AI → AI回答
- **局限性**: 依赖精确关键词匹配，无法理解自然语言意图，搜索召回率有限
- **技术栈**: Spring Boot 3.x, Java 21, MySQL, 智谱AI/SDU-AI API

## Functional Requirements
- **FR-1**: 实现AI关键词重构功能 - 用AI分析用户问题，提取3-5个相关搜索关键词
- **FR-2**: 实现扩展搜索功能 - 用重构的关键词进行多次搜索，合并结果
- **FR-3**: 实现结果去重和排序 - 合并多次搜索结果，去重，重新排序
- **FR-4**: 可配置开关 - 在配置文件中可以选择启用/禁用关键词重构功能
- **FR-5**: 日志记录 - 记录关键词重构过程，便于调试和优化
- **FR-6**: 保持向后兼容 - 保留原有API接口，不影响现有功能
- **FR-7**: 优化流式输出 - 在关键词重构版本中也保持流式输出功能

## Non-Functional Requirements
- **NFR-1**: 首字响应时间 &lt; 5秒（包括关键词重构时间）
- **NFR-2**: 完整响应时间 &lt; 30秒
- **NFR-3**: AI API调用次数控制在合理范围内（最多2次）
- **NFR-4**: 代码可读性好，易于维护和扩展

## Constraints
- **Technical**: 必须使用现有的技术栈，不引入新的依赖
- **Business**: 保持API调用成本与现有方案一致，开发周期控制在2周内
- **Dependencies**: 依赖现有的ModerationConfig（AI API配置），依赖现有的BbsPostService搜索功能

## Assumptions
- AI API可以稳定提供服务
- 用户问题可以被AI正确分析并提取关键词
- 搜索性能可以承受增加的搜索请求量
- 现有流式输出架构可以适配新流程

## Acceptance Criteria

### AC-1: AI关键词重构功能正常工作
- **Given**: 用户输入自然语言问题
- **When**: 系统处理用户问题
- **Then**: AI成功分析并提取3-5个相关搜索关键词
- **Verification**: `programmatic`

### AC-2: 扩展搜索功能正常工作
- **Given**: AI已提取重构关键词
- **When**: 系统执行搜索
- **Then**: 使用重构关键词进行多次搜索，合并结果，去重
- **Verification**: `programmatic`

### AC-3: 可配置开关正常工作
- **Given**: 配置文件中的设置
- **When**: 系统启动或运行
- **Then**: 根据配置启用或禁用关键词重构功能
- **Verification**: `programmatic`

### AC-4: 向后兼容性保持
- **Given**: 旧版本客户端
- **When**: 调用原有API接口
- **Then**: 接口正常工作，功能不受影响
- **Verification**: `programmatic`

### AC-5: 响应时间在可接受范围
- **Given**: 正常网络条件
- **When**: 用户执行AI搜索
- **Then**: 首字响应时间 &lt; 5秒，完整响应时间 &lt; 30秒
- **Verification**: `programmatic`

### AC-6: 搜索质量有提升
- **Given**: 同一测试问题集
- **When**: 对比新旧搜索结果
- **Then**: 新方法的搜索相关性和召回率有明显提升（主观评估）
- **Verification**: `human-judgment`

## Open Questions
- [ ] 是否需要记录关键词重构结果，用于后续分析和优化？
- [ ] 关键词数量和搜索结果数量是否需要做成可配置参数？
- [ ] 是否需要让用户在前端可以看到重构的关键词？
- [ ] 搜索结果的排序策略是否需要调整（混合原有关键词和新关键词的结果）？

