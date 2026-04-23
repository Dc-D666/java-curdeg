-- ===============================================================
-- 用户关注功能数据库表设计脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 创建关注关系表 bbs_follow
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_follow (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关注关系ID',
    follower_id INT NOT NULL COMMENT '关注者ID（粉丝）',
    following_id INT NOT NULL COMMENT '被关注者ID',
    create_time VARCHAR(20) NOT NULL COMMENT '创建时间',
    update_time VARCHAR(20) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_follower_following (follower_id, following_id),
    INDEX idx_following_id (following_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- ===============================================================
-- 2. 扩展 user 表（添加关注数和粉丝数字段）
-- 请先检查字段是否已存在，如已存在请跳过
-- ===============================================================

-- 添加 follower_count 字段
-- 如果报错"Duplicate column name"，说明字段已存在，请注释掉这一行
ALTER TABLE user ADD COLUMN follower_count INT NOT NULL DEFAULT 0 COMMENT '粉丝数';

-- 添加 following_count 字段
-- 如果报错"Duplicate column name"，说明字段已存在，请注释掉这一行
ALTER TABLE user ADD COLUMN following_count INT NOT NULL DEFAULT 0 COMMENT '关注数';

-- 添加索引
-- 如果报错"Duplicate key name"，说明索引已存在，请注释掉这一行
ALTER TABLE user ADD INDEX idx_follower_count (follower_count);

-- 添加索引
-- 如果报错"Duplicate key name"，说明索引已存在，请注释掉这一行
ALTER TABLE user ADD INDEX idx_following_count (following_count);

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看bbs_follow表结构
DESC bbs_follow;

-- 查看user表结构
DESC user;

-- 查看索引
SHOW INDEX FROM bbs_follow;
SHOW INDEX FROM user;
