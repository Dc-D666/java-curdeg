# 帖子检索功能 - 后端规格文档

## 概述
扩展现有的帖子搜索API，支持三种搜索模式：
1. **标题检索** - 只在标题中搜索关键词
2. **全文检索** - 使用MySQL全文索引在标题和内容中搜索
3. **AI检索** - 预留接口，为未来AI集成做准备

## 数据库状态检查
- ✅ 全文索引已在 `09_add_post_fulltext_index.sql` 中定义（idx_post_search）
- ✅ 无需新增表或字段

## API 设计

### 搜索接口
**路径**: `GET /api/bbs/post/search`

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| searchType | String | 否 | 搜索类型：title（默认）\| fulltext \| ai |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认20 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

## 后端实现要点

### 1. 扩展 BbsPostRepository
添加新的查询方法支持不同搜索类型

### 2. 扩展 BbsPostService
添加 `searchPosts()` 方法处理搜索逻辑

### 3. 扩展 BbsPostController
添加新的搜索接口端点

### 4. BbsPost 模型增强（可选）
为AI检索预留 `matchScore` 临时字段（匹配度分数）
