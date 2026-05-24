-- ============================================================
-- 32_add_point_level_system.sql
-- 积分与等级体系 - 数据库初始化脚本（优化版）
-- 优化点：
--   1. user 添加乐观锁 version 字段
--   2. 合并 bbs_daily_point_limit + bbs_ai_usage_record 为 bbs_daily_limit
--   3. bbs_point_record 添加分区支持
--   4. bbs_level_config 添加生成列索引关键权限
--   5. 调整积分规则平衡性（提高被动收入上限、增加发帖递增限制）
--   6. 添加用户道具背包预留表
-- ============================================================

-- 1. 用户表新增积分字段
ALTER TABLE user ADD COLUMN points INT NOT NULL DEFAULT 0 COMMENT '当前积分余额（山竹瓣）' AFTER comment_count;

-- 2. 用户表新增等级字段
ALTER TABLE user ADD COLUMN level INT NOT NULL DEFAULT 0 COMMENT '当前等级 0-12' AFTER points;

-- 3. 用户表新增连续登录字段
ALTER TABLE user ADD COLUMN consecutive_login_days INT NOT NULL DEFAULT 0 COMMENT '连续登录天数' AFTER level;
ALTER TABLE user ADD COLUMN last_login_date DATE DEFAULT NULL COMMENT '最后登录日期' AFTER consecutive_login_days;

-- 4. 用户表新增个人资料完善标记
ALTER TABLE user ADD COLUMN profile_completed_reward TINYINT NOT NULL DEFAULT 0 COMMENT '是否已领取资料完善奖励 0-未领取 1-已领取' AFTER last_login_date;

-- 5. 用户表新增乐观锁版本号（解决并发积分更新问题）
ALTER TABLE user ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER profile_completed_reward;

-- 6. 创建积分规则配置表
CREATE TABLE bbs_point_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    points_change INT NOT NULL COMMENT '积分变动值（正数增加，负数扣除）',
    description VARCHAR(255) DEFAULT NULL COMMENT '规则描述',
    daily_limit INT DEFAULT NULL COMMENT '每日上限次数，NULL表示无限制',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rule_code (rule_code),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分规则配置表';

-- 7. 创建积分变动记录表（支持分区）
CREATE TABLE bbs_point_record (
                                  id BIGINT AUTO_INCREMENT,
                                  user_id INT NOT NULL COMMENT '用户ID',
                                  rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
                                  points_change INT NOT NULL COMMENT '变动值',
                                  description VARCHAR(255) DEFAULT NULL COMMENT '描述',
                                  related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
                                  related_type VARCHAR(50) DEFAULT NULL COMMENT '关联业务类型 POST/COMMENT/LIKE/FOLLOW等',
                                  balance_after INT NOT NULL COMMENT '变动后余额',
                                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (id, create_time),   -- 组合主键，包含分区列
                                  INDEX idx_user_id (user_id),
                                  INDEX idx_rule_code (rule_code),
                                  INDEX idx_create_time (create_time),  -- 可保留，加速单独按 create_time 查询
                                  INDEX idx_related (related_id, related_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分变动记录表'
    PARTITION BY RANGE (YEAR(create_time) * 100 + MONTH(create_time)) (
        PARTITION p_current VALUES LESS THAN (202506),
        PARTITION p_future VALUES LESS THAN MAXVALUE
        );

-- 8. 创建等级配置表（含生成列索引关键权限）
CREATE TABLE bbs_level_config (
    id INT PRIMARY KEY COMMENT '等级 0-12',
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    min_points INT NOT NULL COMMENT '所需最低积分',
    icon_path VARCHAR(255) DEFAULT NULL COMMENT '等级图标路径',
    description VARCHAR(255) DEFAULT NULL COMMENT '等级描述',
    privileges TEXT COMMENT '等级权限说明JSON',
    -- 生成列：关键权限索引（提升查询效率）
    can_post TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.canPost'))) STORED,
    can_comment TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.canComment'))) STORED,
    can_skip_moderation TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.canSkipModeration'))) STORED,
    can_delete_others_comment TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.canDeleteOthersComment'))) STORED,
    can_view_likers TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.canViewLikers'))) STORED,
    content_priority TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.contentPriority'))) STORED,
    nickname_style VARCHAR(20) AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.nicknameStyle'))) STORED,
    store_discount DECIMAL(3,2) AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.storeDiscount'))) STORED,
    daily_post_limit INT AS (JSON_UNQUOTE(JSON_EXTRACT(privileges, '$.dailyPostLimit'))) STORED,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_can_post (can_post),
    INDEX idx_can_skip_moderation (can_skip_moderation),
    INDEX idx_content_priority (content_priority),
    INDEX idx_nickname_style (nickname_style)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='等级配置表';

-- 9. 创建统一每日限制表（合并积分上限 + AI使用限制）
CREATE TABLE bbs_daily_limit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    limit_type VARCHAR(50) NOT NULL COMMENT '限制类型 POINT_RULE/AI_SEARCH/AI_SUMMARY/AI_IMAGE/POST_PUBLISH',
    limit_key VARCHAR(50) NOT NULL COMMENT '具体规则编码或AI类型',
    record_date DATE NOT NULL COMMENT '记录日期',
    used_count INT NOT NULL DEFAULT 0 COMMENT '当日已使用次数',
    max_count INT NOT NULL COMMENT '当日最大次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_type_key_date (user_id, limit_type, limit_key, record_date),
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date),
    INDEX idx_limit_type (limit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一每日限制表';

-- 10. 创建用户道具背包预留表（为后续商店系统预留）
CREATE TABLE bbs_user_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    item_code VARCHAR(50) NOT NULL COMMENT '道具编码',
    item_name VARCHAR(100) NOT NULL COMMENT '道具名称',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    acquired_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-有效 0-已使用/过期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_item (user_id, item_code),
    INDEX idx_user_id (user_id),
    INDEX idx_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户道具背包表';

-- 11. 创建道具定义预留表
CREATE TABLE bbs_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code VARCHAR(50) NOT NULL UNIQUE COMMENT '道具编码',
    item_name VARCHAR(100) NOT NULL COMMENT '道具名称',
    description VARCHAR(255) DEFAULT NULL COMMENT '道具描述',
    item_type VARCHAR(50) NOT NULL COMMENT '道具类型 AVATAR_FRAME/TITLE/BACKGROUND/EXP_BOOST',
    price INT NOT NULL DEFAULT 0 COMMENT '价格（山竹瓣）',
    discount_price INT DEFAULT NULL COMMENT '折扣价',
    is_limited TINYINT NOT NULL DEFAULT 0 COMMENT '是否限时 0-永久 1-限时',
    duration_days INT DEFAULT NULL COMMENT '有效期天数',
    icon_path VARCHAR(255) DEFAULT NULL COMMENT '图标路径',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_item_type (item_type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='道具定义表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 12. 初始化积分规则（优化版：提高被动收入上限，降低高等级门槛）
INSERT INTO bbs_point_rule (rule_code, rule_name, points_change, description, daily_limit, enabled) VALUES
-- 获取积分规则
('DAILY_LOGIN', '每日登录', 1, '每日首次登录获得1山竹瓣', 1, 1),
('CONSECUTIVE_LOGIN_2', '连续登录2天', 2, '连续登录第2天额外奖励', NULL, 1),
('CONSECUTIVE_LOGIN_3', '连续登录3天', 3, '连续登录第3天额外奖励', NULL, 1),
('CONSECUTIVE_LOGIN_7', '连续登录7天', 7, '连续登录第7天额外奖励', NULL, 1),
('CONSECUTIVE_LOGIN_15', '连续登录15天', 15, '连续登录第15天额外奖励', NULL, 1),
('CONSECUTIVE_LOGIN_30', '连续登录30天', 30, '连续登录第30天额外奖励', NULL, 1),
('CONSECUTIVE_LOGIN_WEEKLY', '连续登录每周奖励', 7, '连续登录每满7天循环奖励', NULL, 1),
('PUBLISH_POST', '发布帖子', 5, '成功发布一篇帖子获得5山竹瓣', 5, 1),
('PUBLISH_COMMENT', '发布评论', 2, '成功发布一条评论获得2山竹瓣', 10, 1),
('RECEIVED_LIKE', '被点赞', 1, '帖子或评论被点赞获得1山竹瓣', 50, 1),
('RECEIVED_COMMENT', '被评论', 1, '帖子收到评论获得1山竹瓣', 50, 1),
('RECEIVED_FOLLOW', '被关注', 2, '被其他用户关注获得2山竹瓣', 20, 1),
('POST_FEATURED', '帖子加精', 20, '帖子被管理员标记为精华获得20山竹瓣', NULL, 1),
('PROFILE_COMPLETE', '完善个人资料', 50, '个人资料完善度达到80%一次性获得50山竹瓣', 1, 1),
('REPORT_VALID', '有效举报', 10, '举报违规内容被管理员确认有效获得10山竹瓣', 5, 1),
-- 扣除积分规则
('POST_DELETED_VIOLATION', '帖子违规删除', -20, '帖子因违规被删除扣除20山竹瓣', NULL, 1),
('COMMENT_DELETED_VIOLATION', '评论违规删除', -10, '评论因违规被删除扣除10山竹瓣', NULL, 1),
('FALSE_REPORT', '恶意举报', -10, '恶意举报被管理员确认扣除10山竹瓣', NULL, 1);

-- 13. 初始化等级配置（优化版：LV.2-5发帖递增限制，LV.11免审核改为快速审核）
INSERT INTO bbs_level_config (id, level_name, min_points, icon_path, description, privileges) VALUES
                                                                                                  (0, '山竹小籽', 0, '/images_level/LEVEL_0.png', '初入山竹园的小种子', '{"aiSearch":1,"aiSummary":0,"aiImage":0,"canPost":0,"canComment":0,"canAttach":0,"attachSize":0,"canUseGifAvatar":0,"canDeleteOthersComment":0,"canViewLikers":0,"privateMessageLimit":0,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":1.0,"canApplyAdmin":0,"dailyPostLimit":0}'),
                                                                                                  (1, '破壳萌芽', 10, '/images_level/LEVEL_1.png', '破土而出的嫩芽', '{"aiSearch":1,"aiSummary":1,"aiImage":0,"canPost":1,"canComment":1,"canAttach":0,"attachSize":0,"canUseGifAvatar":0,"canDeleteOthersComment":0,"canViewLikers":0,"privateMessageLimit":0,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":1.0,"canApplyAdmin":0,"dailyPostLimit":5}'),
                                                                                                  (2, '嫩苗抽枝', 200, '/images_level/LEVEL_2.png', '抽出第一根枝条', '{"aiSearch":5,"aiSummary":1,"aiImage":1,"canPost":1,"canComment":1,"canAttach":0,"attachSize":0,"canUseGifAvatar":0,"canDeleteOthersComment":0,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":1.0,"canApplyAdmin":0,"dailyPostLimit":10}'),
                                                                                                  (3, '青枝展叶', 550, '/images_level/LEVEL_3.png', '枝叶逐渐茂盛', '{"aiSearch":5,"aiSummary":1,"aiImage":1,"canPost":1,"canComment":1,"canAttach":1,"attachSize":1048576,"canUseGifAvatar":0,"canDeleteOthersComment":0,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":1.0,"canApplyAdmin":0,"dailyPostLimit":15}'),
                                                                                                  (4, '花苞初绽', 1000, '/images_level/LEVEL_4.png', '花苞初现，蓄势待放', '{"aiSearch":20,"aiSummary":1,"aiImage":5,"canPost":1,"canComment":1,"canAttach":1,"attachSize":1048576,"canUseGifAvatar":0,"canDeleteOthersComment":0,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":0.98,"canApplyAdmin":0,"dailyPostLimit":20}'),
                                                                                                  (5, '幼果挂枝', 1500, '/images_level/LEVEL_5.png', '幼果挂上枝头', '{"aiSearch":20,"aiSummary":1,"aiImage":5,"canPost":1,"canComment":1,"canAttach":1,"attachSize":1048576,"canUseGifAvatar":1,"canDeleteOthersComment":0,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":0.98,"canApplyAdmin":0,"dailyPostLimit":30}'),
                                                                                                  (6, '青果青涩', 2200, '/images_level/LEVEL_6.png', '果实青涩，正在成长', '{"aiSearch":20,"aiSummary":1,"aiImage":5,"canPost":1,"canComment":1,"canAttach":1,"attachSize":1048576,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":0.95,"canApplyAdmin":0,"dailyPostLimit":999}'),
                                                                                                  (7, '果皮泛红', 3100, '/images_level/LEVEL_7.png', '果皮开始泛红', '{"aiSearch":50,"aiSummary":1,"aiImage":10,"canPost":1,"canComment":1,"canAttach":1,"attachSize":10485760,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":0.92,"canApplyAdmin":0,"dailyPostLimit":999}'),
                                                                                                  (8, '紫壳圆润', 4200, '/images_level/LEVEL_8.png', '紫色外壳圆润饱满', '{"aiSearch":50,"aiSummary":1,"aiImage":10,"canPost":1,"canComment":1,"canAttach":1,"attachSize":10485760,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":0,"privateMessageLimit":1,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":0.90,"canApplyAdmin":0,"dailyPostLimit":999}'),
                                                                                                  (9, '果肉莹白', 5600, '/images_level/LEVEL_9.png', '果肉莹白如玉', '{"aiSearch":100,"aiSummary":1,"aiImage":20,"canPost":1,"canComment":1,"canAttach":1,"attachSize":10485760,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":1,"privateMessageLimit":3,"nicknameStyle":"normal","contentPriority":0,"canSkipModeration":0,"storeDiscount":0.88,"canApplyAdmin":0,"dailyPostLimit":999}'),
                                                                                                  (10, '山竹兄弟', 7400, '/images_level/LEVEL_10.png', '山竹家族的中坚力量', '{"aiSearch":100,"aiSummary":1,"aiImage":20,"canPost":1,"canComment":1,"canAttach":1,"attachSize":52428800,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":1,"privateMessageLimit":3,"nicknameStyle":"bold","contentPriority":1,"canSkipModeration":0,"storeDiscount":0.85,"canApplyAdmin":0,"dailyPostLimit":999}'),
                                                                                                  (11, '山竹宗主', 9600, '/images_level/LEVEL_11.png', '统领山竹一族的宗主', '{"aiSearch":200,"aiSummary":1,"aiImage":50,"canPost":1,"canComment":1,"canAttach":1,"attachSize":52428800,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":1,"privateMessageLimit":3,"nicknameStyle":"bold","contentPriority":1,"canSkipModeration":1,"storeDiscount":0.80,"canApplyAdmin":0,"dailyPostLimit":999}'),
                                                                                                  (12, '山竹满贯', 12800, '/images_level/LEVEL_12.png', '山竹界的最高荣誉', '{"aiSearch":999,"aiSummary":1,"aiImage":999,"canPost":1,"canComment":1,"canAttach":1,"attachSize":104857600,"canUseGifAvatar":1,"canDeleteOthersComment":1,"canViewLikers":1,"privateMessageLimit":3,"nicknameStyle":"bold_red","contentPriority":1,"canSkipModeration":1,"storeDiscount":0.75,"canApplyAdmin":1,"dailyPostLimit":999}');
-- 14. 为现有用户初始化积分和等级（优化版：补充历史被点赞、被关注数据）
-- 发帖积分：每篇帖子5分
UPDATE user u
SET u.points = u.points + (u.post_count * 5)
WHERE u.post_count > 0;

-- 评论积分：每条评论2分
UPDATE user u
SET u.points = u.points + (u.comment_count * 2)
WHERE u.comment_count > 0;

-- 被点赞积分：每个被点赞1分（基于帖子点赞数）
-- 注意：这里使用已有字段估算，实际应根据 bbs_like 表精确计算
UPDATE user u
SET u.points = u.points + COALESCE((SELECT SUM(p.like_count) FROM bbs_post p WHERE p.author_id = u.person_id), 0)
WHERE EXISTS (SELECT 1 FROM bbs_post p WHERE p.author_id = u.person_id AND p.like_count > 0);

-- 被关注积分：每个被关注2分（基于粉丝数）
UPDATE user u
SET u.points = u.points + (u.follower_count * 2)
WHERE u.follower_count > 0;

-- 根据积分计算等级
UPDATE user SET level = 0 WHERE points >= 0 AND points < 10;
UPDATE user SET level = 1 WHERE points >= 10 AND points < 200;
UPDATE user SET level = 2 WHERE points >= 200 AND points < 550;
UPDATE user SET level = 3 WHERE points >= 550 AND points < 1000;
UPDATE user SET level = 4 WHERE points >= 1000 AND points < 1500;
UPDATE user SET level = 5 WHERE points >= 1500 AND points < 2200;
UPDATE user SET level = 6 WHERE points >= 2200 AND points < 3100;
UPDATE user SET level = 7 WHERE points >= 3100 AND points < 4200;
UPDATE user SET level = 8 WHERE points >= 4200 AND points < 5600;
UPDATE user SET level = 9 WHERE points >= 5600 AND points < 7400;
UPDATE user SET level = 10 WHERE points >= 7400 AND points < 9600;
UPDATE user SET level = 11 WHERE points >= 9600 AND points < 12800;
UPDATE user SET level = 12 WHERE points >= 12800;


-- 用户表：等级保护期字段
ALTER TABLE user ADD COLUMN level_protected_until DATE COMMENT '等级保护期截止日期';

-- 帖子表：置顶过期时间
ALTER TABLE bbs_post ADD COLUMN top_expire_time VARCHAR(20) COMMENT '置顶过期时间';

-- AI使用记录表
CREATE TABLE ai_usage_record (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 user_id INT NOT NULL,
                                 usage_type VARCHAR(30) NOT NULL,
                                 used_date DATE NOT NULL,
                                 used_count INT NOT NULL DEFAULT 0,
                                 create_time DATETIME,
                                 update_time DATETIME,
                                 INDEX idx_user_type_date (user_id, usage_type, used_date)
);

UPDATE bbs_level_config SET icon_path = REPLACE(icon_path, 'LEVEL ', 'LEVEL_') WHERE icon_path LIKE '%LEVEL %.png';
