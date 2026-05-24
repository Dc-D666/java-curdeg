# 帖子检索功能 - 后端实现计划

## Repo研究结论

项目已具备完整的前后端分离架构：
- 后端：Spring Boot 3.2.5 + Spring Data JPA
- 数据库：MySQL，已有 `bbs_post` 表和全文索引脚本
- 现有 API：`/api/bbs/post/list` 已支持基础搜索

需要修改的文件：
1. `BbsPost.java` - 添加 matchScore 字段
2. `BbsPostRepository.java` - 添加搜索查询方法
3. `BbsPostService.java` - 添加搜索业务逻辑
4. `BbsPostController.java` - 添加搜索 API 端点

## 需修改的文件和模块

| 文件 | 修改类型 | 说明 |
|------|---------|------|
| `BbsPost.java` | 扩展 | 添加 @Transient Double matchScore 字段 |
| `BbsPostRepository.java` | 扩展 | 添加 searchPostsByType() 查询方法 |
| `BbsPostService.java` | 扩展 | 添加 searchPosts() 服务方法 |
| `BbsPostController.java` | 扩展 | 添加 GET /api/bbs/post/search 端点 |

## 实现步骤

### 步骤 1：扩展 BbsPost.java 模型
在模型中添加匹配度分数字段
- 在 `@Transient` 字段区域添加
```java
@Transient
private Double matchScore;
// getter/setter
```

### 步骤 2：扩展 BbsPostRepository.java
添加三种搜索模式的查询方法：
1. `searchPostsByType() - 根据类型查询
- title 模式：只在 title LIKE %keyword%
- fulltext 模式：使用 MATCH AGAINST 全文搜索
- ai 模式：暂与 fulltext 相同（预留）

### 步骤 3：扩展 BbsPostService.java
添加 `searchPosts()` 方法：
- 验证参数（keyword 必填
- 根据 searchType 调用 repository
- 填充作者和板块信息
- 返回分页结果

### 步骤 4：扩展 BbsPostController.java
添加 `/api/bbs/post/search 接口：
- 接收参数：keyword, searchType, pageNum, pageSize
- 参数验证
- 返回标准格式响应

## 潜在依赖和注意事项

- ✅ 全文索引 SQL 脚本已就绪
- 全文搜索使用 MySQL MATCH AGAINST 语法
- 保持与现有 API 响应格式一致
- 复用现有的 `fillPostAuthorInfo()` 方法

## 风险处理

- 全文搜索关键词过短时回退到 LIKE 搜索
- 异常处理与现有代码保持一致
