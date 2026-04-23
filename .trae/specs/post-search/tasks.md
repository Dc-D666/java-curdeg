# 帖子检索功能 - 实现计划

---

## 后端开发任务（优先执行）

## [ ] Backend Task 1: 运行数据库初始化脚本
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 执行 `20_post_search_init.sql` 脚本
  - 确认全文索引创建成功
- **Acceptance Criteria Addressed**: 基础设施
- **Test Requirements**:
  - `programmatic` TR-B1.1: SQL脚本执行成功
  - `programmatic` TR-B1.2: 全文索引 idx_post_search 存在
- **Notes**: SQL脚本已做安全检查，可多次执行

## [ ] Backend Task 2: 扩展 BbsPost.java 模型
- **Priority**: P0
- **Depends On**: Backend Task 1
- **Description**: 
  - 添加 `@Transient Double matchScore` 字段（匹配度分数）
- **Acceptance Criteria Addressed**: 数据模型
- **Test Requirements**:
  - `programmatic` TR-B2.1: 模型编译无错误
- **Notes**: 字段仅用于AI检索模式

## [ ] Backend Task 3: 扩展 BbsPostRepository.java
- **Priority**: P0
- **Depends On**: Backend Task 2
- **Description**: 
  - 添加 `searchPostsByType()` 查询方法
  - 支持三种搜索模式：title / fulltext / ai
  - 全文搜索使用 MATCH AGAINST
- **Acceptance Criteria Addressed**: 数据访问层
- **Test Requirements**:
  - `programmatic` TR-B3.1: Repository方法编译通过
  - `programmatic` TR-B3.2: 标题搜索正常工作
  - `programmatic` TR-B3.3: 全文搜索正常工作
- **Notes**: AI搜索先返回与fulltext相同的结果

## [ ] Backend Task 4: 扩展 BbsPostService.java
- **Priority**: P0
- **Depends On**: Backend Task 3
- **Description**: 
  - 添加 `searchPosts()` 服务方法
  - 根据 searchType 参数调用不同的 Repository 方法
  - 填充作者信息等附属数据
- **Acceptance Criteria Addressed**: 业务逻辑层
- **Test Requirements**:
  - `programmatic` TR-B4.1: Service方法编译通过
  - `programmatic` TR-B4.2: 三种搜索模式都能正常返回结果

## [ ] Backend Task 5: 扩展 BbsPostController.java
- **Priority**: P0
- **Depends On**: Backend Task 4
- **Description**: 
  - 添加 `GET /api/bbs/post/search` 接口端点
  - 处理请求参数验证
- **Acceptance Criteria Addressed**: API层
- **Test Requirements**:
  - `programmatic` TR-B5.1: Controller编译通过
  - `programmatic` TR-B5.2: API接口正常响应
  - `programmatic` TR-B5.3: 参数验证工作正常

---

## 前端开发任务

## [ ] Frontend Task 1: 创建PostSearchController.java控制器
- **Priority**: P0
- **Depends On**: Backend Task 5
- **Description**: 
  - 参考PostListController的结构创建新的PostSearchController
  - 实现搜索模式切换逻辑
  - 实现搜索执行逻辑
  - 实现表格展示和分页逻辑
  - 实现双击打开帖子详情功能
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6
- **Test Requirements**:
  - `programmatic` TR-F1.1: 控制器文件创建成功，无编译错误
  - `programmatic` TR-F1.2: 三种搜索模式可以正常切换
  - `programmatic` TR-F1.3: 点击搜索按钮或按回车可以执行搜索
  - `human-judgement` TR-F1.4: 表格能正确展示搜索结果
  - `programmatic` TR-F1.5: 分页功能正常工作
- **Notes**: 复用PostListController中的表格渲染和分页逻辑

## [ ] Frontend Task 2: 创建post-search.fxml布局文件
- **Priority**: P0
- **Depends On**: Frontend Task 1
- **Description**: 
  - 设计并创建FXML布局
  - 包含搜索模式切换按钮组（ToggleGroup）
  - 包含搜索输入框和搜索按钮
  - 包含结果表格
  - 包含分页控件
  - 添加适当的样式
- **Acceptance Criteria Addressed**: AC-1, AC-7
- **Test Requirements**:
  - `programmatic` TR-F2.1: FXML文件创建成功，无语法错误
  - `human-judgement` TR-F2.2: 布局合理，控件齐全
  - `human-judgement` TR-F2.3: 样式与现有UI风格一致
- **Notes**: 参考post-list.fxml的布局结构

## [ ] Frontend Task 3: 扩展HttpRequestUtil.java（如需要）
- **Priority**: P1
- **Depends On**: None
- **Description**: 
  - 检查是否需要添加新的搜索API调用方法
  - 如需添加，实现支持不同搜索模式的请求方法
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-F3.1: API调用方法正常工作
- **Notes**: 先检查后端API是否已支持搜索类型参数

## [ ] Frontend Task 4: 集成到主菜单系统
- **Priority**: P0
- **Depends On**: Frontend Task 1, Frontend Task 2
- **Description**: 
  - 确保MainFrameController能正确加载"帖子检索"页面
  - 检查menu_info配置是否正确
  - 测试从菜单进入页面的功能
- **Acceptance Criteria Addressed**: AC-1
- **Test Requirements**:
  - `human-judgement` TR-F4.1: 点击菜单项能正常打开页面
  - `programmatic` TR-F4.2: 页面加载无错误
- **Notes**: 菜单id=202，name=PostSearch已配置

## [ ] Frontend Task 5: 完整功能测试与优化
- **Priority**: P1
- **Depends On**: Frontend Task 1, Frontend Task 2, Frontend Task 4
- **Description**: 
  - 端到端测试所有功能
  - 修复发现的问题
  - 优化用户体验
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6, AC-7
- **Test Requirements**:
  - `human-judgement` TR-F5.1: 所有功能正常工作
  - `human-judgement` TR-F5.2: 用户体验良好
