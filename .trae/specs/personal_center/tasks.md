# 校园论坛个人中心后端 - 实现计划

## [ ] Task 1: 扩展 Repository 层（添加统计查询方法）
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 在 BbsPostRepository 中添加按用户ID查询帖子的方法
  - 在 BbsFavoriteRepository 中添加按用户ID查询收藏的方法
  - 在 BbsLikeRepository 中添加统计用户获赞数的方法
  - 在 BbsCommentRepository 中添加统计用户评论获赞数的方法
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-5
- **Test Requirements**:
  - `programmatic` TR-1.1: 可以通过 authorId 查询用户的帖子
  - `programmatic` TR-1.2: 可以通过 userId 查询用户的收藏
  - `programmatic` TR-1.3: 可以统计用户所有帖子获得的点赞数
  - `programmatic` TR-1.4: 可以统计用户所有评论获得的点赞数

## [ ] Task 2: 实现用户统计数据 Service 层
- **Priority**: high
- **Depends On**: Task 1
- **Description**: 
  - 在 BbsUserService 中添加获取用户统计数据的方法
  - 计算用户的总获赞数（帖子点赞+评论点赞）
  - 计算用户的被收藏总数
  - 计算用户的总浏览量
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-2.1: 统计数据正确包含所有要求的字段
  - `programmatic` TR-2.2: 获赞数正确累加帖子和评论的点赞

## [ ] Task 3: 实现我的帖子 API
- **Priority**: high
- **Depends On**: Task 1
- **Description**: 
  - 在 BbsUserController 中添加获取我的帖子的接口
  - 在 BbsUserService 中实现对应的业务逻辑
  - 支持分页查询
- **Acceptance Criteria Addressed**: AC-2
- **Test Requirements**:
  - `programmatic` TR-3.1: GET /api/bbs/user/me/posts 返回用户的帖子列表
  - `programmatic` TR-3.2: 支持 pageNum 和 pageSize 参数
  - `programmatic` TR-3.3: 只返回当前登录用户的帖子

## [ ] Task 4: 实现我的收藏 API
- **Priority**: high
- **Depends On**: Task 1
- **Description**: 
  - 在 BbsUserController 中添加获取我的收藏的接口
  - 在 BbsUserService 中实现对应的业务逻辑
  - 支持分页查询
- **Acceptance Criteria Addressed**: AC-3
- **Test Requirements**:
  - `programmatic` TR-4.1: GET /api/bbs/user/me/favorites 返回收藏的帖子列表
  - `programmatic` TR-4.2: 支持分页参数
  - `programmatic` TR-4.3: 只返回当前登录用户的收藏

## [ ] Task 5: 完善关注/粉丝列表 API
- **Priority**: high
- **Depends On**: None
- **Description**: 
  - 检查并完善 BbsFollowController 和 BbsFollowService
  - 确保关注列表和粉丝列表API正常工作
- **Acceptance Criteria Addressed**: AC-4
- **Test Requirements**:
  - `programmatic` TR-5.1: 关注列表API正常返回数据
  - `programmatic` TR-5.2: 粉丝列表API正常返回数据
  - `programmatic` TR-5.3: 支持分页功能

## [ ] Task 6: 实现获取统计数据 API
- **Priority**: high
- **Depends On**: Task 2
- **Description**: 
  - 在 BbsUserController 中添加获取统计数据的接口
  - 返回完整的用户统计信息
- **Acceptance Criteria Addressed**: AC-5
- **Test Requirements**:
  - `programmatic` TR-6.1: GET /api/bbs/user/me/statistics 返回统计数据
  - `programmatic` TR-6.2: 统计数据包含所有要求的字段

## [ ] Task 7: 实现密码修改 API
- **Priority**: medium
- **Depends On**: None
- **Description**: 
  - 在 BbsUserController 中添加修改密码的接口
  - 在 BbsUserService 中实现密码验证和更新逻辑
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `programmatic` TR-7.1: POST /api/bbs/user/me/password 可以修改密码
  - `programmatic` TR-7.2: 原密码验证失败时返回错误
  - `programmatic` TR-7.3: 新密码需要符合复杂度要求

## [ ] Task 8: 集成测试和验证
- **Priority**: high
- **Depends On**: Task 3, Task 4, Task 5, Task 6, Task 7
- **Description**: 
  - 对所有新增API进行完整测试
  - 验证功能正确性
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6
- **Test Requirements**:
  - `programmatic` TR-8.1: 所有API能正常调用并返回正确数据
  - `programmatic` TR-8.2: 未登录用户访问受保护API返回401
