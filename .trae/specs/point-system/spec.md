# 山竹瓣积分与等级体系 Spec（优化版）

## Why
当前校园论坛系统缺乏用户激励机制，用户发帖、评论、互动等行为没有量化反馈。新增以"山竹瓣"为单位的积分体系，配合0-12级成长等级，可以激励用户活跃，量化社区贡献，并为不同等级用户开放差异化功能权限（AI功能、附件、头像、私信、审核等）。

## What Changes
- 新增积分字段到 User 实体和数据库表（别称"山竹瓣"）
- 新增等级字段 `level`（0-12）及对应图标
- 新增连续登录追踪字段
- 新增乐观锁 `version` 字段（解决并发积分更新问题）
- 新增积分规则配置表 `bbs_point_rule`（可配置化积分规则）
- 新增积分记录表 `bbs_point_record`（按月分区，记录积分变动明细）
- 新增等级配置表 `bbs_level_config`（含生成列索引关键权限）
- 新增统一每日限制表 `bbs_daily_limit`（合并积分上限 + AI使用限制）
- 新增用户道具背包表 `bbs_user_item`（为后续商店系统预留）
- 新增道具定义表 `bbs_item`（为后续商店系统预留）
- 新增每日发帖限制检查（LV.1-5 递增限制）
- 新增私信限制检查（未互关用户发送条数）
- 新增帖子排序优先级权重
- 新增商店折扣字段
- 在业务服务中嵌入积分发放/扣除逻辑（使用原子UPDATE + 乐观锁）
- 新增积分查询/排行榜 API 和前端页面
- 在个人资料页、用户主页显示积分和等级
- 新增积分明细页面、积分排行榜页面
- 新增等级权限检查（发帖、评论、AI功能、附件、GIF头像、删除他人评论、查看点赞者、私信条数、免审核、昵称样式等）
- 菜单系统新增积分相关入口

## Impact
- Affected specs: 用户系统、帖子系统、评论系统、点赞系统、关注系统、通知系统、统计系统、AI功能系统、私信系统
- Affected code:
  - Backend: User.java, UserRepository.java, BbsUserService.java, BbsUserController.java, BbsPostService.java, BbsCommentService.java, BbsLikeService.java, BbsFollowService.java, BbsReportService.java, PostModerationService.java, AuthService.java, AiSearchService.java, ContentSummaryService.java, AiImageService.java, BbsMessageService.java, StatisticsService.java, StatisticsController.java
  - Frontend: User.java, PersonalProfileController.java, UserHomeController.java, PostPublishController.java, PostDetailController.java, AiImageDialogController.java, ChatViewController.java, HttpRequestUtil.java, personal-profile.fxml, user-home.fxml
  - New files: PointRule.java, PointRecord.java, LevelConfig.java, DailyLimit.java, UserItem.java, Item.java, PointRuleRepository.java, PointRecordRepository.java, LevelConfigRepository.java, DailyLimitRepository.java, UserItemRepository.java, ItemRepository.java, PointService.java, PointController.java, PointRankController.java, PointHistoryController.java, point-rank.fxml, point-history.fxml, LevelPrivilegeService.java

## ADDED Requirements

### Requirement: 积分字段存储
The system SHALL 在用户表 `sys_user` 中新增 `points` 字段（INT，默认0），用于存储用户当前"山竹瓣"余额。

#### Scenario: 新用户注册
- **WHEN** 新用户注册成功
- **THEN** 用户积分初始化为 0

### Requirement: 等级体系
The system SHALL 提供0-12级等级体系，每级有对应的名称、所需积分、等级图标和权限：

| 等级 | 名称 | 所需积分 | 图标 |
|------|------|---------|------|
| 0 | 山竹小籽 | 0 | LEVEL 0.png |
| 1 | 破壳萌芽 | 10 | LEVEL 1.png |
| 2 | 嫩苗抽枝 | 200 | LEVEL 2.png |
| 3 | 青枝展叶 | 550 | LEVEL 3.png |
| 4 | 花苞初绽 | 1000 | LEVEL 4.png |
| 5 | 幼果挂枝 | 1500 | LEVEL 5.png |
| 6 | 青果青涩 | 2200 | LEVEL 6.png |
| 7 | 果皮泛红 | 3100 | LEVEL 7.png |
| 8 | 紫壳圆润 | 4200 | LEVEL 8.png |
| 9 | 果肉莹白 | 5600 | LEVEL 9.png |
| 10 | 山竹兄弟 | 7400 | LEVEL 10.png |
| 11 | 山竹宗主 | 9600 | LEVEL 11.png |
| 12 | 山竹满贯 | 12800 | LEVEL 12.png |

### Requirement: 等级权限控制
The system SHALL 根据用户等级控制以下功能权限：

| 权限 | LV.0 | LV.1 | LV.2 | LV.3 | LV.4 | LV.5 | LV.6 | LV.7 | LV.8 | LV.9 | LV.10 | LV.11 | LV.12 |
|------|------|------|------|------|------|------|------|------|------|------|-------|-------|-------|
| 基础浏览/点赞 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 发帖 | ❌ | ✅(5/天) | ✅(10/天) | ✅(15/天) | ✅(20/天) | ✅(30/天) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 评论 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| AI搜索(次/日) | 1 | 1 | 5 | 5 | 20 | 20 | 20 | 50 | 50 | 100 | 100 | 200 | 999 |
| AI总结帖子 | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| AI生成配图(次/日) | ❌ | ❌ | 1 | 1 | 5 | 5 | 5 | 10 | 10 | 20 | 20 | 50 | 999 |
| 附件上传 | ❌ | ❌ | ❌ | ✅(1MB) | ✅(1MB) | ✅(1MB) | ✅(1MB) | ✅(10MB) | ✅(10MB) | ✅(10MB) | ✅(50MB) | ✅(50MB) | ✅(100MB) |
| GIF动态头像 | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 删除他人评论 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 查看点赞者 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 未互关私信条数 | 0 | 0 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 3 | 3 | 3 | 3 |
| 昵称样式 | 普通 | 普通 | 普通 | 普通 | 普通 | 普通 | 普通 | 普通 | 普通 | 普通 | 加粗 | 加粗 | 加粗标红 |
| 内容优先推荐 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| 发帖免审核 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅(快速审核) | ✅(快速审核) |
| 商店折扣 | 无 | 无 | 无 | 无 | 98折 | 98折 | 95折 | 92折 | 9折 | 88折 | 85折 | 8折 | 75折 |
| 申请管理员 | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

### Requirement: 积分规则配置
The system SHALL 提供可配置的积分规则，通过 `bbs_point_rule` 表管理：

| 规则编码 | 行为描述 | 积分值 | 每日上限 |
|---------|---------|--------|---------|
| DAILY_LOGIN | 每日登录 | +1 | 1 |
| CONSECUTIVE_LOGIN_2 | 连续登录2天 | +2 | - |
| CONSECUTIVE_LOGIN_3 | 连续登录3天 | +3 | - |
| CONSECUTIVE_LOGIN_7 | 连续登录7天 | +7 | - |
| CONSECUTIVE_LOGIN_15 | 连续登录15天 | +15 | - |
| CONSECUTIVE_LOGIN_30 | 连续登录30天 | +30 | - |
| CONSECUTIVE_LOGIN_WEEKLY | 连续登录每周奖励 | +7 | - |
| PUBLISH_POST | 发布帖子 | +5 | 5 |
| PUBLISH_COMMENT | 发布评论 | +2 | 10 |
| RECEIVED_LIKE | 被点赞 | +1 | 50 |
| RECEIVED_COMMENT | 被评论 | +1 | 50 |
| RECEIVED_FOLLOW | 被关注 | +2 | 20 |
| POST_FEATURED | 帖子加精 | +20 | - |
| PROFILE_COMPLETE | 完善个人资料(80%) | +50 | 1 |
| REPORT_VALID | 有效举报 | +10 | 5 |
| POST_DELETED_VIOLATION | 帖子违规删除 | -20 | - |
| COMMENT_DELETED_VIOLATION | 评论违规删除 | -10 | - |
| FALSE_REPORT | 恶意举报 | -10 | - |

### Requirement: 积分变动记录
The system SHALL 在 `bbs_point_record` 表中记录每次积分变动明细，字段包括：
- `id` 主键
- `user_id` 用户ID
- `rule_code` 规则编码
- `points_change` 变动值（正数增加，负数减少）
- `description` 描述
- `related_id` 关联业务ID
- `related_type` 关联业务类型
- `balance_after` 变动后余额
- `create_time` 创建时间

### Requirement: 积分发放触发点
The system SHALL 在以下业务操作中自动发放/扣除积分：

#### Scenario: 每日登录
- **WHEN** 用户每日首次登录
- **THEN** 用户积分增加 DAILY_LOGIN 规则配置的积分值
- **AND** 检查连续登录天数，达到2/3/7/15/30天时额外发放对应奖励
- **AND** 连续登录超过30天后，每满7天循环发放 CONSECUTIVE_LOGIN_WEEKLY 奖励

#### Scenario: 用户发帖
- **WHEN** 用户成功发布帖子（审核通过）
- **THEN** 用户积分增加 PUBLISH_POST 规则配置的积分值

#### Scenario: 用户评论
- **WHEN** 用户成功发布评论
- **THEN** 用户积分增加 PUBLISH_COMMENT 规则配置的积分值

#### Scenario: 收到点赞
- **WHEN** 用户的帖子或评论被点赞
- **THEN** 内容作者积分增加 RECEIVED_LIKE 规则配置的积分值

#### Scenario: 被评论
- **WHEN** 用户的帖子收到评论
- **THEN** 帖子作者积分增加 RECEIVED_COMMENT 规则配置的积分值

#### Scenario: 被关注
- **WHEN** 用户被其他用户关注
- **THEN** 被关注用户积分增加 RECEIVED_FOLLOW 规则配置的积分值

#### Scenario: 帖子加精
- **WHEN** 用户的帖子被管理员标记为精华
- **THEN** 帖子作者积分增加 POST_FEATURED 规则配置的积分值

#### Scenario: 完善个人资料
- **WHEN** 用户个人资料完善度达到80%且未领取过奖励
- **THEN** 用户积分增加 PROFILE_COMPLETE 规则配置的积分值

#### Scenario: 有效举报
- **WHEN** 用户举报的违规内容被管理员确认有效
- **THEN** 举报者积分增加 REPORT_VALID 规则配置的积分值

#### Scenario: 内容被删除（违规）
- **WHEN** 用户的帖子或评论因违规被删除
- **THEN** 内容作者积分扣除对应规则配置的积分值

#### Scenario: 恶意举报
- **WHEN** 用户举报被管理员确认为恶意举报
- **THEN** 举报者积分扣除 FALSE_REPORT 规则配置的积分值

### Requirement: 积分防重复机制
The system SHALL 确保同一业务事件不会重复发放积分：
- 点赞后取消再点赞，只发一次积分
- 关注后取消再关注，只发一次积分
- 每日登录积分每天只发一次
- 各规则遵守每日上限限制

### Requirement: 并发安全（乐观锁）
The system SHALL 使用以下机制确保积分变动的并发安全：
- **sys_user 表添加 version 乐观锁字段**
- 积分变动时使用原子UPDATE：`UPDATE sys_user SET points = points + ?, version = version + 1 WHERE user_id = ? AND version = ?`
- 更新失败时自动重试（最多3次）
- 每日限制使用 `INSERT ... ON DUPLICATE KEY UPDATE` 原子操作

### Requirement: 等级自动计算
The system SHALL 在用户积分变动后自动重新计算等级：
- 积分增加时，如果达到下一等级所需积分，自动升级
- 积分减少时，如果低于当前等级所需积分，不降级（等级只升不降）
- **等级保护期**：积分减少后7天内不降级，给用户缓冲期

### Requirement: 等级权限检查
The system SHALL 在以下操作前检查用户等级权限：
- 发帖前检查 `canPost` 权限和每日发帖上限（LV.1:5/天, LV.2:10/天, LV.3:15/天, LV.4:20/天, LV.5:30/天）
- 评论前检查 `canComment` 权限
- AI搜索前检查当日剩余次数
- AI总结前检查 `aiSummary` 权限和当日剩余次数
- AI生成配图前检查 `aiImage` 权限和当日剩余次数
- 上传附件前检查 `canAttach` 权限和附件大小限制
- 上传头像时检查 `canUseGifAvatar` 权限（GIF格式）
- 删除评论时检查 `canDeleteOthersComment` 权限（删除他人评论）
- 查看点赞列表时检查 `canViewLikers` 权限
- 发送私信时检查 `privateMessageLimit`（未互关用户限制）
- 发帖时检查 `skipModeration` 权限（LV.11+ 快速审核：先发布再异步宽松审核）
- 申请管理员时检查 `canApplyAdmin` 权限

### Requirement: 帖子排序权重
The system SHALL 根据用户等级调整帖子在广场的排序权重：
- LV.10+ 用户的帖子获得更高的排序权重，优先展示

### Requirement: 发帖快速审核（LV.11+）
The system SHALL 对 LV.11+ 用户的帖子采用快速审核策略：
- 帖子先直接发布通过，用户可立即看到
- 后台异步以更宽松标准提交AI审核
- 如审核发现问题，再标记为待处理或隐藏
- 保留事后追责机制

### Requirement: 商店折扣
The system SHALL 根据用户等级提供商店折扣：
- LV.0-3: 无折扣 (1.0)
- LV.4-5: 98折 (0.98)
- LV.6: 95折 (0.95)
- LV.7: 92折 (0.92)
- LV.8: 9折 (0.90)
- LV.9: 88折 (0.88)
- LV.10: 85折 (0.85)
- LV.11: 8折 (0.80)
- LV.12: 75折 (0.75)

### Requirement: 道具系统预留
The system SHALL 预留道具系统接口：
- `bbs_item` 表：道具定义（头像框、称号、背景、经验加成等）
- `bbs_user_item` 表：用户道具背包
- 道具价格支持折扣计算

### Requirement: 积分查询 API
The system SHALL 提供以下 REST API：
- `GET /api/bbs/points/me` - 获取当前用户积分余额、等级、今日变动
- `GET /api/bbs/points/me/history?pageNum=&pageSize=` - 获取当前用户积分明细
- `GET /api/bbs/points/rank?pageNum=&pageSize=` - 获取积分排行榜
- `GET /api/bbs/points/rank/weekly` - 获取本周积分增长排行榜
- `GET /api/bbs/level/config` - 获取等级配置列表
- `GET /api/bbs/level/my-privileges` - 获取当前用户等级权限

### Requirement: 前端积分展示
The system SHALL 在以下页面展示"山竹瓣"积分和等级信息：

#### Scenario: 个人资料页
- **WHEN** 用户查看个人资料页
- **THEN** 在统计卡片区域显示当前"山竹瓣"余额和等级图标+名称

#### Scenario: 用户主页
- **WHEN** 用户查看他人主页
- **THEN** 在统计区域显示该用户"山竹瓣"余额和等级图标+名称

#### Scenario: 积分明细页
- **WHEN** 用户进入积分明细页面
- **THEN** 显示"山竹瓣"变动历史列表，包含时间、行为、变动值、余额

#### Scenario: 积分排行榜页
- **WHEN** 用户进入积分排行榜页面
- **THEN** 显示全站用户"山竹瓣"排名，高亮当前用户位置

### Requirement: 菜单集成
The system SHALL 在个人中心菜单下新增：
- "山竹瓣明细" 子菜单
- "山竹瓣排行" 子菜单

## MODIFIED Requirements

### Requirement: 现有用户统计接口
**BbsUserService.getUserStatistics()** 和 **getUserStatisticsDetail()** 需新增积分相关统计：
- 当前"山竹瓣"余额
- 当前等级和等级名称
- 本周"山竹瓣"增长
- 距离下一等级所需"山竹瓣"
- 商店折扣率

### Requirement: 现有用户资料接口
**BbsUserService.getUserProfile()** 和 **BbsUserService.getCurrentUser()** 需返回用户积分、等级、等级名称、昵称样式、商店折扣字段。

### Requirement: 现有前端 User 模型
**com.teach.javafx.models.User** 需新增 `points`、`level`、`levelName`、`nicknameStyle`、`storeDiscount` 字段。

### Requirement: AI功能限制
**AiSearchService.aiSearch()** 需在调用前检查用户当日AI搜索剩余次数。
**ContentSummaryService.summarizePost()** 需在调用前检查用户等级权限和当日剩余次数。
**AiImageService.generateImage()** 需在调用前检查用户等级权限和当日剩余次数。

### Requirement: 发帖/评论权限
**BbsPostService.createPost()** 需在发帖前检查用户等级是否满足 `canPost` 权限和每日发帖上限。
**BbsCommentService.createComment()** 需在评论前检查用户等级是否满足 `canComment` 权限。

### Requirement: 附件上传限制
**BbsFileService.uploadAttachment()** 需根据用户等级检查附件大小限制。

### Requirement: 头像上传限制
**BbsFileService.uploadImage()** 需根据用户等级检查是否允许GIF格式头像。

### Requirement: 评论删除权限
**BbsCommentService.deleteComment()** 需根据用户等级检查是否允许删除他人评论。

### Requirement: 点赞列表权限
**BbsLikeService.getPostLikers()** / **BbsCommentService.getCommentLikers()** 需根据用户等级检查是否允许查看点赞者列表。

### Requirement: 私信限制
**BbsMessageService.sendMessage()** 需根据用户等级检查未互关用户的私信条数限制。

### Requirement: 帖子排序
**BbsPostService.getPostList()** 需根据发帖用户等级调整帖子排序权重。

### Requirement: 发帖审核
**PostModerationService.processNewPost()** 需根据用户等级判断是否采用快速审核流程。

## REMOVED Requirements
无移除需求。
