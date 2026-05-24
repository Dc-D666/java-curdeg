# 帖子检索功能 - 后端验证检查清单

## 数据库层检查
- [ ] SQL脚本执行成功
- [ ] 全文索引 idx_post_search 已创建
- [ ] 能查询到索引信息

## 模型层检查
- [ ] BbsPost.java 新增 matchScore 字段
- [ ] 字段使用 @Transient 注解
- [ ] 编译无错误

## Repository层检查
- [ ] searchPostsByType() 方法添加成功
- [ ] 支持 title 搜索模式
- [ ] 支持 fulltext 搜索模式（使用 MATCH AGAINST）
- [ ] 支持 ai 搜索模式（暂时与fulltext相同）
- [ ] 查询返回分页数据

## Service层检查
- [ ] searchPosts() 服务方法添加成功
- [ ] 根据 searchType 正确分发查询
- [ ] 填充作者信息（authorNickname, authorAvatarUrl）
- [ ] 填充版块信息（boardName）
- [ ] 返回数据格式正确

## Controller层检查
- [ ] GET /api/bbs/post/search 接口添加成功
- [ ] 参数验证正常工作（keyword必填）
- [ ] 默认 searchType=title
- [ ] 默认分页参数正常
- [ ] API响应格式符合标准

## 功能测试检查
- [ ] 标题搜索正常返回结果
- [ ] 全文搜索正常返回结果
- [ ] AI搜索（预留）正常返回结果
- [ ] 分页功能正常工作
- [ ] 无结果时返回空数组而非错误
