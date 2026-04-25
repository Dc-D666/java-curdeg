# AI检索功能性能优化 - Product Requirements Document (PRD)

## Overview

### Summary
本文档描述对AI检索功能进行性能优化，解决应用卡死问题。主要问题包括：前端UI线程阻塞、重复渲染、Text节点过多、日志输出过量等。

### Purpose
- 修复AI检索时导致应用卡死的问题
- 提升用户体验，确保搜索过程流畅
- 优化性能，降低资源消耗

### Target Users
- 使用校园论坛系统的所有用户
- 使用AI搜索功能的用户

## Goals
1. ✅ 解决前端UI线程阻塞问题
2. ✅ 优化渲染机制，避免重复渲染
3. ✅ 减少日志输出，提升性能
4. ✅ 实现内容缓冲更新，降低UI刷新频率
5. ✅ 优化SSE连接和超时处理

## Non-Goals
- ❌ 不修改AI搜索的核心算法
- ❌ 不添加新的功能特性
- ❌ 不重构现有代码架构（只做针对性优化）

## Background & Context

### 现有问题分析

根据代码分析，导致应用卡死的主要原因：

#### 1. 前端问题（PostSearchController.java）
- **高频UI更新**：第652-660行，每次收到内容就立即追加Text到TextFlow中
- **重复渲染**：第673-674行，完成时又清空并重新完整解析一次Markdown
- **Text节点爆炸**：流式追加时每个片段都创建一个Text节点，数量多了会导致性能下降
- **日志刷屏**：System.out.println输出过多

#### 2. 后端问题（AiSearchService.java）
- **详细日志输出**：每收到一段内容就输出一次日志（第183行）
- **超时设置较长**：120秒可能不够，但不是主要问题

### 技术架构
- 前端：JavaFX + SSE流式通信
- 后端：Spring Boot + RestTemplate + Java HttpClient

## Functional Requirements

### FR1: 实现内容缓冲更新
- 使用缓冲机制，定期批量更新UI，而不是每个片段都更新
- 缓冲时间间隔建议：100-200ms
- 确保用户体验流畅，不会明显延迟

### FR2: 移除重复渲染
- 流式过程中只显示纯文本，不做Markdown解析
- 只在完成时做一次完整的Markdown解析和渲染
- 或者：流式过程中实时解析，但更高效

### FR3: 优化日志输出
- 移除或减少前端System.out.println日志
- 移除或减少后端详细日志
- 保留必要的错误日志

### FR4: 防止Text节点过多
- 流式过程中减少Text节点创建
- 考虑使用单个Text节点进行更新（setText）
- 或者：合理控制节点数量

### FR5: 增加连接超时和取消机制
- 用户可以取消正在进行的搜索
- 搜索按钮状态管理更完善
- 处理连接超时情况

## Non-Functional Requirements

### NFR1: 性能指标
- UI响应延迟 < 16ms（60fps）
- 内存占用不显著增加
- 搜索过程无明显卡顿

### NFR2: 稳定性
- 不会出现UI完全卡死的情况
- 长时间搜索也能保持响应
- 异常情况有优雅降级处理

### NFR3: 用户体验
- 搜索过程有明确的状态提示
- 加载动画/提示清晰
- 可以随时取消搜索

## Constraints

### Technical
- 必须兼容现有的JavaFX架构
- 不能破坏现有的SSE通信机制
- 保持API接口不变

### Business
- 优化周期：快速完成（优先级高）
- 不能影响其他功能模块

### Dependencies
- 现有代码库结构不变
- 不引入新的第三方库

## Assumptions

1. 搜索功能的核心逻辑是正确的
2. AI服务端API调用是正常的
3. 主要问题在前端渲染和日志层面
4. 用户接受轻微的延迟来换取流畅体验

## Acceptance Criteria

### AC1: UI不卡死 - programmatic
**Given** 用户正在使用AI搜索功能
**When** AI返回大量内容（>500段）
**Then** 应用不会完全卡死，UI保持响应
**Verification** 实际测试，检查UI线程状态

### AC2: 搜索可取消 - human-judgment
**Given** AI搜索正在进行中
**When** 用户点击取消/切换搜索模式
**Then** 当前搜索被终止，UI恢复正常
**Verification** 用户测试体验

### AC3: 内容显示正常 - human-judgment
**Given** AI搜索成功返回内容
**When** 搜索完成
**Then** Markdown格式正确显示，内容完整
**Verification** 用户测试验证

### AC4: 日志输出减少 - programmatic
**Given** 正在进行AI搜索
**When** 搜索过程中
**Then** 控制台日志输出大幅减少（减少>80%）
**Verification** 对比前后日志输出

### AC5: 编译通过 - programmatic
**Given** 所有代码修改完成
**When** 执行编译
**Then** 前后端项目编译成功
**Verification** 运行 mvnw.cmd clean compile

## Open Questions

1. ❓ 缓冲时间设置多少最合适？（100ms, 150ms, 200ms？）
2. ❓ 是否需要增加搜索取消按钮？还是复用现有逻辑？
3. ❓ 流式过程中是否需要部分Markdown渲染？还是纯文本？
