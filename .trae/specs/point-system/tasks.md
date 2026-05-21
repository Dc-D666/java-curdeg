# Tasks

## Phase 1: 数据库与后端基础

- [x] Task 1: 数据库表结构变更（SQL已完成：32_add_point_level_system.sql）
  - [x] SubTask 1.1: sys_user 表新增 points, level, consecutive_login_days, last_login_date, profile_completed_reward 字段
  - [x] SubTask 1.2: 创建 bbs_point_rule 表（规则配置）
  - [x] SubTask 1.3: 创建 bbs_point_record 表（积分变动记录）
  - [x] SubTask 1.4: 创建 bbs_level_config 表（等级配置）
  - [x] SubTask 1.5: 创建 bbs_daily_limit 表（合并AI使用限制和发帖限制，替代原bbs_ai_usage_record+bbs_daily_point_limit）
  - [x] SubTask 1.6: 初始化默认积分规则数据
  - [x] SubTask 1.7: 初始化13个等级配置数据（含新增权限：查看点赞者、私信条数、昵称样式、优先推荐、免审核、商店折扣、申请管理员）
  - [x] SubTask 1.8: 为现有用户 retroactive 计算积分和等级

- [x] Task 2: 后端实体与数据访问层
  - [x] SubTask 2.1: 修改 User.java - 新增 points, level, consecutiveLoginDays, lastLoginDate, profileCompletedReward 字段及 getter/setter
  - [x] SubTask 2.2: 创建 PointRule.java 实体类
  - [x] SubTask 2.3: 创建 PointRecord.java 实体类
  - [x] SubTask 2.4: 创建 LevelConfig.java 实体类
  - [x] SubTask 2.5: 创建 DailyLimit.java 实体类（替代AiUsageRecord和DailyPointLimit）
  - [x] SubTask 2.6: 创建 PointRuleRepository.java 接口
  - [x] SubTask 2.7: 创建 PointRecordRepository.java 接口
  - [x] SubTask 2.8: 创建 LevelConfigRepository.java 接口
  - [x] SubTask 2.9: 创建 DailyLimitRepository.java 接口
  - [x] SubTask 2.10: 修改 UserRepository.java - 新增积分排序和排名查询方法

- [x] Task 3: 后端积分核心服务
  - [x] SubTask 3.1: 创建 PointService.java - 核心积分服务
    - [x] 加载积分规则配置
    - [x] 积分变动方法（支持增加和扣除）
    - [x] 防重复检查逻辑
    - [x] 每日上限检查逻辑
    - [x] 积分查询方法（余额、明细、排行榜）
    - [x] 等级自动计算方法（只升不降）
  - [x] SubTask 3.2: 创建 LevelPrivilegeService.java - 等级权限服务
    - [x] 获取用户当前等级配置
    - [x] 检查 canPost, canComment, aiSearchLimit, aiImageLimit, aiSummaryLimit
    - [x] 检查 canAttach, attachSizeLimit, canUseGifAvatar, canDeleteOthersComment
    - [x] 检查 canViewLikers, privateMessageLimit, canSkipModeration, canApplyAdmin
    - [x] 检查 nicknameStyle, contentPriority, storeDiscount
    - [x] 检查AI功能当日剩余次数
    - [x] 检查每日发帖上限
  - [x] SubTask 3.3: 创建 PointController.java - REST API 控制器
    - [x] GET /api/bbs/points/me
    - [x] GET /api/bbs/points/me/history
    - [x] GET /api/bbs/points/rank
    - [x] GET /api/bbs/points/rank/weekly
    - [x] GET /api/bbs/level/config
    - [x] GET /api/bbs/level/my-privileges
    - [x] GET /api/bbs/points/ai-usage (新增：AI使用剩余次数查询)

## Phase 2: 业务集成（积分发放点 + 权限检查）

- [x] Task 4: 登录积分集成
  - [x] SubTask 4.1: 修改 AuthService.login() - 每日首次登录发放积分，更新连续登录天数，检查连续登录奖励
  - [x] SubTask 4.2: 连续登录超过30天后每周奖励 (+7)

- [x] Task 5: 发帖积分集成 + 权限检查
  - [x] SubTask 5.1: 修改 BbsPostService.createPost() - 发帖前检查 canPost 权限（等级>=1）和每日发帖上限
  - [x] SubTask 5.2: 修改 BbsPostService.createPost() - 帖子审核通过后发放 PUBLISH_POST 积分
  - [x] SubTask 5.3: 修改 BbsPostService.deletePost() - 违规删除时扣除 POST_DELETED_VIOLATION 积分
  - [x] SubTask 5.4: 修改 BbsPostService.toggleFeature() - 加精时发放 POST_FEATURED 积分
  - [x] SubTask 5.5: 修改 BbsPostService.getPostList() - LV.10+ 用户帖子获得排序权重加成
  - [x] SubTask 5.6: BbsPost模型新增topExpireTime字段（为置顶卡道具预留）

- [x] Task 6: 评论积分集成 + 权限检查
  - [x] SubTask 6.1: 修改 BbsCommentService.createComment() - 评论前检查 canComment 权限（等级>=1）
  - [x] SubTask 6.2: 修改 BbsCommentService.createComment() - 评论成功后发放 PUBLISH_COMMENT 积分，给帖子作者发放 RECEIVED_COMMENT 积分
  - [x] SubTask 6.3: 修改 BbsCommentService.deleteComment() - 检查 canDeleteOthersComment 权限（删除他人评论需等级>=6）
  - [x] SubTask 6.4: 修改 BbsCommentService.deleteComment() - 违规删除扣除 COMMENT_DELETED_VIOLATION 积分

- [x] Task 7: 点赞积分集成 + 权限检查
  - [x] SubTask 7.1: 修改 BbsPostService.toggleLike() - 首次点赞时给内容作者发放 RECEIVED_LIKE 积分
  - [x] SubTask 7.2: 修改 BbsCommentService.toggleLike() - 首次点赞时给评论作者发放 RECEIVED_LIKE 积分
  - [x] SubTask 7.3: 确保取消点赞不重复发放，再次点赞也不重复发放
  - [x] SubTask 7.4: 修改点赞列表查询 - 检查 canViewLikers 权限（等级>=8）

- [x] Task 8: 关注积分集成
  - [x] SubTask 8.1: 修改 BbsFollowService.toggleFollow() - 首次关注时给被关注者发放 RECEIVED_FOLLOW 积分
  - [x] SubTask 8.2: 确保取消关注不重复发放，再次关注也不重复发放

- [x] Task 9: 举报积分集成
  - [x] SubTask 9.1: 修改 BbsReportService.handleReport() - 举报处理为删除内容时，给举报者发放 REPORT_VALID 积分
  - [x] SubTask 9.2: 修改 BbsReportService.handleReport() - 举报被驳回时，给举报者扣除 FALSE_REPORT 积分

- [x] Task 10: 个人资料完善积分
  - [x] SubTask 10.1: 修改 BbsUserService.updateCurrentUser() - 计算资料完善度，达到80%且未领取过时发放 PROFILE_COMPLETE 积分

- [x] Task 11: AI功能权限限制
  - [x] SubTask 11.1: 修改 AiSearchService.aiSearch() - 检查用户等级和当日AI搜索剩余次数
  - [x] SubTask 11.2: 修改 ContentSummaryService.summarizePost() - 检查用户等级（>=1）和当日剩余次数
  - [x] SubTask 11.3: 修改 AiImageService.generateImage() - 检查用户等级（>=2）和当日剩余次数

- [x] Task 12: 附件/头像权限限制
  - [x] SubTask 12.1: 修改 BbsFileService.uploadAttachment() - 检查 canAttach 权限和附件大小限制
  - [x] SubTask 12.2: 修改 BbsFileService.uploadImage() - 检查 GIF 格式时 canUseGifAvatar 权限

- [x] Task 13: 私信权限限制
  - [x] SubTask 13.1: 修改 BbsMessageService.sendMessage() - 检查未互关用户私信条数限制（LV.9+ 允许3条）

- [x] Task 14: 现有接口扩展
  - [x] SubTask 14.1: 修改 BbsUserService.getUserStatistics() - 返回积分、等级、本周增长、距离下一等级、商店折扣
  - [x] SubTask 14.2: 修改 BbsUserService.getUserStatisticsDetail() - 新增积分趋势数据
  - [x] SubTask 14.3: 修改 BbsUserService.getUserProfile() - 返回积分、等级、等级名称、昵称样式、商店折扣
  - [x] SubTask 14.4: 修改 BbsUserService.getCurrentUser() - 返回积分、等级、等级名称、昵称样式、商店折扣
  - [x] SubTask 14.5: 修改 BbsUserService.register() - 新用户初始化积分为0，等级为0

## Phase 3: 前端集成

- [x] Task 15: 前端模型与请求层
  - [x] SubTask 15.1: 修改 com.teach.javafx.models.User - 新增 points, level, levelName, nicknameStyle, storeDiscount 字段
  - [x] SubTask 15.2: 创建 com.teach.javafx.models.PointRecord 模型类
  - [x] SubTask 15.3: 创建 com.teach.javafx.models.LevelConfig 模型类
  - [x] SubTask 15.4: 修改 HttpRequestUtil.java - 新增积分相关请求方法
    - [x] getMyPoints()
    - [x] getPointHistory(pageNum, pageSize)
    - [x] getPointRank(pageNum, pageSize)
    - [x] getWeeklyPointRank()
    - [x] getLevelConfig()
    - [x] getMyPrivileges()
    - [x] getAiUsage() (新增)

- [x] Task 16: 个人资料页积分展示
  - [x] SubTask 16.1: 修改 personal-profile.fxml - 在统计网格新增"山竹瓣"和等级卡片
  - [x] SubTask 16.2: 修改 PersonalProfileController.java - 绑定积分和等级数据，显示等级图标，根据nicknameStyle显示昵称样式

- [x] Task 17: 用户主页积分展示
  - [x] SubTask 17.1: 修改 user-home.fxml - 在统计区域新增"山竹瓣"和等级显示
  - [x] SubTask 17.2: 修改 UserHomeController.java - 绑定积分和等级数据，显示等级图标，根据nicknameStyle显示昵称样式

- [x] Task 18: 积分明细页面
  - [x] SubTask 18.1: 创建 point-history.fxml - 积分明细页面布局
  - [x] SubTask 18.2: 创建 PointHistoryController.java - 积分明细控制器
  - [x] SubTask 18.3: 支持分页加载积分记录

- [x] Task 19: 积分排行榜页面
  - [x] SubTask 19.1: 创建 point-rank.fxml - 排行榜页面布局
  - [x] SubTask 19.2: 创建 PointRankController.java - 排行榜控制器
  - [x] SubTask 19.3: 支持总榜和周榜切换
  - [x] SubTask 19.4: 高亮当前用户排名

- [x] Task 20: 等级特权页面
  - [x] SubTask 20.1: 创建 level-privileges.fxml - 等级特权页面布局
  - [x] SubTask 20.2: 创建 LevelPrivilegesController.java - 等级特权控制器
  - [x] SubTask 20.3: 显示当前特权（14项权限表格）
  - [x] SubTask 20.4: 显示等级体系（LV0-12分组展示）

- [x] Task 21: 菜单集成
  - [x] SubTask 21.1: 修改 PersonalCenterController.java - 注册新页面导航
  - [x] SubTask 21.2: 确保菜单系统能正确加载积分相关页面

- [x] Task 22: 前端权限提示
  - [x] SubTask 22.1: 发帖按钮等级限制提示 (PostListController, PostPublishController)
  - [x] SubTask 22.2: 评论按钮等级限制提示 (PostDetailController)
  - [x] SubTask 22.3: AI搜索剩余次数显示 (PostSearchController)
  - [x] SubTask 22.4: AI配图剩余次数显示 (PostPublishController)
  - [x] SubTask 22.5: 昵称显示处根据nicknameStyle应用样式 (PostListController, PostSearchController, PostDetailController, CommentItemController)

## Phase 4: 验证与优化

- [ ] Task 23: 功能验证
  - [ ] SubTask 23.1: 验证每日登录积分增加和连续登录奖励
  - [ ] SubTask 23.2: 验证发帖后积分增加（审核通过后）
  - [ ] SubTask 23.3: 验证评论后积分增加
  - [ ] SubTask 23.4: 验证被点赞后积分增加
  - [ ] SubTask 23.5: 验证被评论后积分增加
  - [ ] SubTask 23.6: 验证被关注后积分增加
  - [ ] SubTask 23.7: 验证帖子加精后积分增加
  - [ ] SubTask 23.8: 验证完善资料后积分增加
  - [ ] SubTask 23.9: 验证有效举报后积分增加
  - [ ] SubTask 23.10: 验证违规删除后积分扣除
  - [ ] SubTask 23.11: 验证恶意举报后积分扣除
  - [ ] SubTask 23.12: 验证防重复机制（点赞/关注/登录）
  - [ ] SubTask 23.13: 验证等级权限控制（发帖/评论/AI/附件/头像/删除/点赞列表/私信）
  - [ ] SubTask 23.14: 验证LV.1每日发帖上限
  - [ ] SubTask 23.15: 验证LV.11+发帖快速审核
  - [ ] SubTask 23.16: 验证LV.10+帖子排序权重
  - [ ] SubTask 23.17: 验证昵称样式显示
  - [ ] SubTask 23.18: 验证AI剩余次数显示
  - [ ] SubTask 23.19: 验证发帖/评论按钮等级限制提示

- [ ] Task 24: 数据迁移与脚本更新
  - [ ] SubTask 24.1: 更新 init-database.bat 脚本，包含 32_add_point_level_system.sql

# Task Dependencies
- Task 1 → Task 2 → Task 3（数据库 → 实体/Repository → Service/Controller）
- Task 3 → Task 4,5,6,7,8,9,10,11,12,13,14（核心服务完成后才能集成到业务）
- Task 3 → Task 14（统计接口扩展依赖核心服务）
- Task 2 → Task 15（前端模型依赖后端实体定义）
- Task 15 → Task 16,17,18,19,20,21,22（前端页面依赖请求层）
- Task 17,18,19,20 → Task 21,22（权限提示依赖页面完成）
- Task 4,5,6,7,8,9,10,11,12,13,14,16,17,18,19,20,21,22 → Task 23（验证依赖所有功能完成）
