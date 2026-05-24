# 校园论坛数据统计分析模块 - 验证清单

## 后端API验证

### 数据概览API
- [ ] `/api/bbs/statistics/overview` 返回包含8个关键指标的JSON数据
- [ ] 数据包括：用户总数、今日新增用户、本月活跃用户、帖子总数、今日新增帖子、评论总数、今日新增评论、待审核内容数
- [ ] 数据准确性验证（与数据库实际记录一致）

### 用户统计API
- [ ] `/api/bbs/statistics/user-growth?days=7` 返回7天用户增长数据
- [ ] `/api/bbs/statistics/user-growth?days=30` 返回30天用户增长数据
- [ ] `/api/bbs/statistics/user-growth?days=90` 返回90天用户增长数据
- [ ] `/api/bbs/statistics/user-type-distribution` 返回用户类型分布数据
- [ ] `/api/bbs/statistics/active-users?sortBy=post` 返回按发帖排序的活跃用户
- [ ] `/api/bbs/statistics/active-users?sortBy=comment` 返回按评论排序的活跃用户
- [ ] `/api/bbs/statistics/active-users?sortBy=combined` 返回按综合排序的活跃用户
- [ ] `/api/bbs/statistics/user-activity-distribution` 返回用户活跃度分布数据
- [ ] `/api/bbs/statistics/banned-users` 返回封禁用户统计数据

### 内容统计API
- [ ] `/api/bbs/statistics/post-trend?days=7` 返回7天发帖趋势数据
- [ ] `/api/bbs/statistics/post-trend?days=30` 返回30天发帖趋势数据
- [ ] `/api/bbs/statistics/post-trend?days=90` 返回90天发帖趋势数据
- [ ] `/api/bbs/statistics/board-distribution` 返回板块发帖分布数据
- [ ] `/api/bbs/statistics/hot-posts?sortBy=like` 返回按点赞排序的热门帖子
- [ ] `/api/bbs/statistics/hot-posts?sortBy=comment` 返回按评论排序的热门帖子
- [ ] `/api/bbs/statistics/hot-posts?sortBy=view` 返回按浏览排序的热门帖子
- [ ] `/api/bbs/statistics/hot-posts?sortBy=favorite` 返回按收藏排序的热门帖子
- [ ] `/api/bbs/statistics/post-status-distribution` 返回帖子状态分布数据
- [ ] `/api/bbs/statistics/image-post-ratio` 返回图片发帖占比数据

### 互动统计API
- [ ] `/api/bbs/statistics/comment-trend?days=7` 返回7天评论趋势数据
- [ ] `/api/bbs/statistics/comment-trend?days=30` 返回30天评论趋势数据
- [ ] `/api/bbs/statistics/comment-trend?days=90` 返回90天评论趋势数据
- [ ] `/api/bbs/statistics/like-trend?days=7` 返回7天点赞趋势数据
- [ ] `/api/bbs/statistics/like-trend?days=30` 返回30天点赞趋势数据
- [ ] `/api/bbs/statistics/like-trend?days=90` 返回90天点赞趋势数据
- [ ] `/api/bbs/statistics/favorite-trend?days=7` 返回7天收藏趋势数据
- [ ] `/api/bbs/statistics/favorite-trend?days=30` 返回30天收藏趋势数据
- [ ] `/api/bbs/statistics/favorite-trend?days=90` 返回90天收藏趋势数据
- [ ] `/api/bbs/statistics/follow-trend?days=7` 返回7天关注趋势数据
- [ ] `/api/bbs/statistics/follow-trend?days=30` 返回30天关注趋势数据
- [ ] `/api/bbs/statistics/follow-trend?days=90` 返回90天关注趋势数据
- [ ] `/api/bbs/statistics/hot-comments` 返回热门评论Top10

### 内容安全统计API
- [ ] `/api/bbs/statistics/moderation-overview` 返回审核统计概览数据
- [ ] `/api/bbs/statistics/moderation-trend?days=7` 返回7天审核趋势数据
- [ ] `/api/bbs/statistics/moderation-trend?days=30` 返回30天审核趋势数据
- [ ] `/api/bbs/statistics/moderation-trend?days=90` 返回90天审核趋势数据
- [ ] `/api/bbs/statistics/violation-type-distribution` 返回违规类型分布数据
- [ ] `/api/bbs/statistics/report-overview` 返回举报统计概览数据

### 权限控制验证
- [ ] 非管理员用户访问统计API返回403权限不足
- [ ] 管理员用户可正常访问所有统计API
- [ ] 现有API（`/daily-post`, `/hot-post`, `/active-user`）仍然正常工作

### 性能验证
- [ ] 所有统计API响应时间 < 3秒
- [ ] 大数据量情况下查询性能可接受

---

## 前端UI验证

### 页面布局验证
- [ ] 统计页面包含5个Tab：数据概览、用户统计、内容统计、互动统计、内容安全统计
- [ ] Tab切换流畅，无卡顿
- [ ] 页面布局美观，信息层次清晰

### 数据概览Tab验证
- [ ] 显示8个关键指标卡片
- [ ] 指标数据准确显示
- [ ] 页面加载无错误提示

### 用户统计Tab验证
- [ ] 用户增长趋势图正常显示，支持7天/30天/90天切换
- [ ] 用户类型分布饼图正常显示
- [ ] 活跃用户排行榜正常显示，支持按发帖/评论/综合排序
- [ ] 用户活跃度分布图正常显示
- [ ] 封禁用户统计正常显示

### 内容统计Tab验证
- [ ] 发帖趋势图正常显示，支持7天/30天/90天切换
- [ ] 板块发帖分布图正常显示
- [ ] 热门帖子排行榜正常显示，支持按点赞/评论/浏览/收藏排序
- [ ] 帖子状态分布图正常显示
- [ ] 图片发帖占比图正常显示
- [ ] 原有的"每日发帖量"、"热门帖子Top10"功能正常

### 互动统计Tab验证
- [ ] 评论趋势图正常显示，支持7天/30天/90天切换
- [ ] 点赞趋势图正常显示，支持7天/30天/90天切换
- [ ] 收藏趋势图正常显示，支持7天/30天/90天切换
- [ ] 关注趋势图正常显示，支持7天/30天/90天切换
- [ ] 热门评论Top10正常显示

### 内容安全统计Tab验证
- [ ] 审核统计概览指标卡片正常显示
- [ ] 审核趋势图正常显示（多系列），支持7天/30天/90天切换
- [ ] 违规类型分布图正常显示
- [ ] 举报统计正常显示

### 兼容性验证
- [ ] 原有的"活跃用户Top10"功能正常
- [ ] 所有现有功能不受影响

### 用户体验验证
- [ ] 图表数据加载有适当的加载状态提示
- [ ] 数据加载失败时有友好的错误提示
- [ ] 所有交互响应流畅
- [ ] 数据展示清晰易读

---

## 数据准确性验证

### 关键指标准确性
- [ ] 用户总数与数据库实际用户数一致
- [ ] 今日新增用户数准确
- [ ] 本月活跃用户数准确
- [ ] 帖子总数与数据库实际帖子数一致
- [ ] 今日新增帖子数准确
- [ ] 评论总数与数据库实际评论数一致
- [ ] 今日新增评论数准确
- [ ] 待审核内容数准确

### 图表数据准确性
- [ ] 用户增长趋势数据与实际记录一致
- [ ] 发帖趋势数据与实际记录一致
- [ ] 审核趋势数据与实际记录一致
- [ ] 各类分布数据占比总和为100%

### 排行榜数据准确性
- [ ] 活跃用户排序正确
- [ ] 热门帖子排序正确
- [ ] 热门评论排序正确
