
# AI流式搜索卡死问题优化方案 - Product Requirement Document

## Overview
- **Summary**: 修复AI流式搜索模式下应用卡死的问题，通过添加内容缓冲、限流机制和优化UI更新策略，提升用户体验
- **Purpose**: 解决高频SSE事件和过度UI更新导致的应用无响应问题
- **Target Users**: 校园论坛系统的所有用户

## Goals
1. 解决AI流式搜索时应用卡死问题
2. 优化响应流畅度，提升用户体验
3. 保持现有功能完整性
4. 提高系统稳定性

## Non-Goals (Out of Scope)
- 不修改非流式搜索功能
- 不修改AI API调用逻辑本身
- 不重构核心搜索算法

## Background & Context
当前问题分析：
1. 后端每个AI返回的chunk都立即发送SSE事件，没有缓冲
2. 前端每次收到内容都立即解析并更新UI，Platform.runLater()调用过于频繁
3. 输出完成后还会进行一次完整解析，造成重复工作
4. 高频UI更新阻塞了JavaFX应用线程，导致应用无响应

## Functional Requirements
- **FR-1**: 后端添加内容缓冲机制，合并连续的content事件
- **FR-2**: 前端实现批量UI更新，减少Platform.runLater()调用频率
- **FR-3**: 保持现有搜索结果展示的完整性
- **FR-4**: 提供平滑的流式输出体验

## Non-Functional Requirements
- **NFR-1**: 响应流畅度提升，无明显卡顿
- **NFR-2**: UI更新频率控制在可接受范围（建议50-100ms/次）
- **NFR-3**: 系统资源占用合理，不造成额外开销
- **NFR-4**: 兼容现有所有功能

## Constraints
- **Technical**: 
  - 使用现有技术栈（JavaFX + Spring Boot）
  - 不引入额外依赖
- **Business**:
  - 保持现有功能完整性
  - 不增加用户额外负担

## Assumptions
1. AI API返回正常的流式数据
2. 网络连接基本稳定
3. JavaFX应用线程处理能力有限

## Acceptance Criteria

### AC-1: 后端缓冲机制
- **Given**: 后端收到AI流式返回数据
- **When**: 连续多个content事件在短时间内到达
- **Then**: 后端应该将内容合并后批量发送SSE事件
- **Verification**: programmatic
- **Notes**: 缓冲时间建议设为50-100ms

### AC-2: 前端批量更新
- **Given**: 前端收到多个SSE content事件
- **When**: 在指定时间窗口内
- **Then**: 前端应该合并内容后批量更新UI，而不是每次都更新
- **Verification**: programmatic

### AC-3: 性能提升
- **Given**: 用户使用AI流式搜索
- **When**: 完成一次正常搜索
- **Then**: 应用不会卡死，UI保持响应，用户体验流畅
- **Verification**: human-judgment

### AC-4: 功能完整性
- **Given**: 优化后的系统
- **When**: 用户使用所有搜索相关功能
- **Then**: 所有功能正常工作，与优化前一致
- **Verification**: human-judgment

## Open Questions
- 是否需要让缓冲时间可配置？
- 对于非常长的输出，是否需要分批显示？
