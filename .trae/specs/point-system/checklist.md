# Checklist

## 数据库层
- [ ] `sys_user` 表新增 `points` 字段（INT，默认0）
- [ ] `sys_user` 表新增 `level` 字段（INT，默认0）
- [ ] `sys_user` 表新增 `consecutive_login_days` 字段（INT，默认0）
- [ ] `sys_user` 表新增 `last_login_date` 字段（DATE）
- [ ] `sys_user` 表新增 `profile_completed_reward` 字段（TINYINT，默认0）
- [ ] `bbs_point_rule` 表创建完成，包含17条默认规则
- [ ] `bbs_point_record` 表创建完成
- [ ] `bbs_level_config` 表创建完成，包含13个等级配置（含新增权限字段）
- [ ] `bbs_ai_usage_record` 表创建完成
- [ ] `bbs_daily_point_limit` 表创建完成
- [ ] 现有用户 retroactive 积分计算正确（发帖5分/篇，评论2分/条）
- [ ] 现有用户等级根据积分正确初始化

## 后端实体与Repository
- [ ] `User.java` 新增 `points`, `level`, `consecutiveLoginDays`, `lastLoginDate`, `profileCompletedReward` 字段
- [ ] `PointRule.java` 实体类创建完成
- [ ] `PointRecord.java` 实体类创建完成
- [ ] `LevelConfig.java` 实体类创建完成
- [ ] `AiUsageRecord.java` 实体类创建完成
- [ ] `DailyPointLimit.java` 实体类创建完成
- [ ] `PointRuleRepository.java` 接口创建完成
- [ ] `PointRecordRepository.java` 接口创建完成
- [ ] `LevelConfigRepository.java` 接口创建完成
- [ ] `AiUsageRecordRepository.java` 接口创建完成
- [ ] `DailyPointLimitRepository.java` 接口创建完成
- [ ] `UserRepository.java` 新增积分排序和排名查询方法

## 后端核心服务与控制器
- [ ] `PointService.java` 创建完成，包含：
  - [ ] 积分规则加载方法
  - [ ] 积分变动方法（支持增加和扣除）
  - [ ] 防重复检查逻辑
  - [ ] 每日上限检查逻辑
  - [ ] 积分余额查询
  - [ ] 积分明细分页查询
  - [ ] 积分排行榜查询（总榜、周榜）
  - [ ] 等级自动计算方法（只升不降）
- [ ] `LevelPrivilegeService.java` 创建完成，包含：
  - [ ] 获取用户当前等级配置
  - [ ] 检查 canPost 权限和每日发帖上限
  - [ ] 检查 canComment 权限
  - [ ] 检查 AI搜索 当日剩余次数
  - [ ] 检查 AI总结 权限和当日剩余次数
  - [ ] 检查 AI生成配图 权限和当日剩余次数
  - [ ] 检查 canAttach 权限和附件大小限制
  - [ ] 检查 canUseGifAvatar 权限
  - [ ] 检查 canDeleteOthersComment 权限
  - [ ] 检查 canViewLikers 权限
  - [ ] 检查 privateMessageLimit 权限
  - [ ] 检查 canSkipModeration 权限
  - [ ] 检查 canApplyAdmin 权限
  - [ ] 检查 nicknameStyle, contentPriority, storeDiscount
- [ ] `PointController.java` 创建完成，包含6个API端点：
  - [ ] `GET /api/bbs/points/me`
  - [ ] `GET /api/bbs/points/me/history`
  - [ ] `GET /api/bbs/points/rank`
  - [ ] `GET /api/bbs/points/rank/weekly`
  - [ ] `GET /api/bbs/level/config`
  - [ ] `GET /api/bbs/level/my-privileges`

## 业务集成（积分发放点）
- [ ] `AuthService.login()` - 每日首次登录发放 DAILY_LOGIN 积分
- [ ] `AuthService.login()` - 连续登录天数更新和奖励发放（2/3/7/15/30天）
- [ ] `BbsPostService.createPost()` - 发帖前检查 canPost 权限（等级>=1）和每日发帖上限（LV.1限制5帖/天）
- [ ] `PostModerationService.processNewPost()` - LV.11+ 跳过审核，先发布再异步宽松审核
- [ ] `PostModerationService.updatePostWithModerationResult()` - 审核通过后发放 PUBLISH_POST 积分
- [ ] `BbsPostService.toggleFeature()` - 加精时发放 POST_FEATURED 积分
- [ ] `BbsPostService.deletePost()` / `BbsReportService.handleDeleteContent()` - 违规删除扣除 POST_DELETED_VIOLATION 积分
- [ ] `BbsCommentService.createComment()` - 评论前检查 canComment 权限（等级>=1）
- [ ] `BbsCommentService.createComment()` - 评论成功后发放 PUBLISH_COMMENT 积分
- [ ] `BbsCommentService.createComment()` - 给帖子作者发放 RECEIVED_COMMENT 积分
- [ ] `BbsCommentService.deleteComment()` - 检查 canDeleteOthersComment 权限（等级>=6）
- [ ] `BbsCommentService.deleteComment()` - 违规删除扣除 COMMENT_DELETED_VIOLATION 积分
- [ ] `BbsLikeService.toggleLike()` / `BbsPostService.toggleLike()` - 首次点赞给作者发放 RECEIVED_LIKE 积分
- [ ] `BbsCommentService.toggleLike()` - 首次点赞给评论作者发放 RECEIVED_LIKE 积分
- [ ] 点赞防重复：取消再点赞不重复加分
- [ ] 点赞列表查询 - 检查 canViewLikers 权限（等级>=8）
- [ ] `BbsFollowService.toggleFollow()` - 首次关注给被关注者发放 RECEIVED_FOLLOW 积分
- [ ] 关注防重复：取消再关注不重复加分
- [ ] `BbsReportService.handleReport()` - 有效举报发放 REPORT_VALID 积分
- [ ] `BbsReportService.handleReport()` - 恶意举报扣除 FALSE_REPORT 积分
- [ ] `BbsUserService.updateCurrentUser()` - 资料完善度>=80%发放 PROFILE_COMPLETE 积分
- [ ] `BbsMessageService.sendMessage()` - 检查未互关用户私信条数限制（LV.9+ 允许3条）

## AI功能权限限制
- [ ] `AiSearchService.aiSearch()` - 检查用户等级和当日AI搜索剩余次数
- [ ] `ContentSummaryService.summarizePost()` - 检查用户等级（>=1）和当日剩余次数
- [ ] `AiImageService.generateImage()` - 检查用户等级（>=2）和当日剩余次数
- [ ] `AiImageController.generateImage()` - 返回权限不足错误信息

## 附件/头像权限限制
- [ ] `BbsFileService.uploadAttachment()` - 检查 canAttach 权限和附件大小限制
- [ ] `BbsFileService.uploadImage()` - 检查 GIF 格式时 canUseGifAvatar 权限
- [ ] `PostPublishController.validateAttachmentFile()` - 前端检查附件大小

## 帖子排序与审核
- [ ] `BbsPostService.getPostList()` - LV.10+ 用户帖子获得排序权重加成
- [ ] `PostModerationService.processNewPost()` - LV.11+ 帖子免审核，先发布再异步宽松审核

## 现有接口扩展
- [ ] `BbsUserService.getUserStatistics()` 返回积分、等级、本周增长、距离下一等级、商店折扣
- [ ] `BbsUserService.getUserStatisticsDetail()` 新增积分趋势数据
- [ ] `BbsUserService.getUserProfile()` 返回积分、等级、等级名称、昵称样式、商店折扣
- [ ] `BbsUserService.getCurrentUser()` 返回积分、等级、等级名称、昵称样式、商店折扣
- [ ] `BbsUserService.register()` 新用户初始化积分为0，等级为0

## 前端模型与请求层
- [ ] `com.teach.javafx.models.User` 新增 `points`, `level`, `levelName`, `nicknameStyle`, `storeDiscount` 字段
- [ ] `com.teach.javafx.models.PointRecord` 模型类创建完成
- [ ] `com.teach.javafx.models.LevelConfig` 模型类创建完成
- [ ] `HttpRequestUtil.java` 新增以下方法：
  - [ ] `getMyPoints()`
  - [ ] `getPointHistory(pageNum, pageSize)`
  - [ ] `getPointRank(pageNum, pageSize)`
  - [ ] `getWeeklyPointRank()`
  - [ ] `getLevelConfig()`
  - [ ] `getMyPrivileges()`

## 前端页面
- [ ] `personal-profile.fxml` 统计网格新增"山竹瓣"和等级卡片
- [ ] `PersonalProfileController.java` 正确显示积分和等级数据，显示等级图标，根据nicknameStyle显示昵称样式
- [ ] `user-home.fxml` 统计区域新增"山竹瓣"和等级显示
- [ ] `UserHomeController.java` 正确显示积分和等级数据，显示等级图标，根据nicknameStyle显示昵称样式
- [ ] `point-history.fxml` 创建完成
- [ ] `PointHistoryController.java` 创建完成，支持分页
- [ ] `point-rank.fxml` 创建完成
- [ ] `PointRankController.java` 创建完成，支持总榜/周榜切换，高亮当前用户

## 菜单集成
- [ ] 个人中心菜单新增 "山竹瓣明细" 入口
- [ ] 个人中心菜单新增 "山竹瓣排行" 入口
- [ ] 页面导航正常工作

## 前端权限提示
- [ ] 发帖/评论按钮处显示等级限制提示
- [ ] AI功能按钮处显示剩余次数
- [ ] 附件上传处显示大小限制
- [ ] 私信页面显示未互关用户剩余条数
- [ ] 昵称显示处根据nicknameStyle应用样式（普通/加粗/加粗标红）

## 验证
- [ ] 每日登录积分增加正确
- [ ] 连续登录2/3/7/15/30天额外奖励正确
- [ ] 发帖后积分增加正确（审核通过后）
- [ ] 评论后积分增加正确
- [ ] 被点赞后积分增加正确
- [ ] 被评论后积分增加正确
- [ ] 被关注后积分增加正确
- [ ] 帖子加精后积分增加正确
- [ ] 完善资料后积分增加正确
- [ ] 有效举报后积分增加正确
- [ ] 违规帖子删除后积分扣除正确
- [ ] 违规评论删除后积分扣除正确
- [ ] 恶意举报后积分扣除正确
- [ ] 同一天多次登录不重复加分
- [ ] 点赞取消再点赞不重复加分
- [ ] 关注取消再关注不重复加分
- [ ] 积分明细记录完整准确
- [ ] 积分排行榜数据正确
- [ ] 周榜只统计本周增长
- [ ] 等级权限控制正确（LV.0不能发帖/评论）
- [ ] LV.1每日发帖上限5条
- [ ] LV.11+发帖免审核
- [ ] LV.10+帖子排序权重加成
- [ ] AI搜索次数限制正确
- [ ] AI总结权限检查正确
- [ ] AI配图权限检查正确
- [ ] 附件大小限制正确
- [ ] GIF头像权限检查正确
- [ ] 删除他人评论权限检查正确
- [ ] 查看点赞者权限检查正确（LV.8+）
- [ ] 私信条数限制正确（LV.9+允许3条）
- [ ] 昵称样式显示正确（普通/加粗/加粗标红）
