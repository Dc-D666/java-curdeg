-- ===============================================================
-- 个人中心功能补充脚本
-- 此脚本用于补充个人中心功能所需的数据库字段和表
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 检查并补充 bbs_post 表的字段
-- ===============================================================

-- 检查 favorite_count 字段是否存在，如果不存在则添加
-- 如果报错"Duplicate column name"，说明字段已存在，请注释掉这一行
SET @dbname = DATABASE();
SET @tablename = 'bbs_post';
SET @columnname = 'favorite_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT NOT NULL DEFAULT 0 COMMENT ''收藏数'' AFTER view_count')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 favorite_count 索引（如果不存在）
SET @indexname = 'idx_favorite_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (index_name = @indexname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (', @columnname, ')')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ===============================================================
-- 2. 创建收藏表 bbs_favorite（如果不存在）
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id INT NOT NULL COMMENT '用户ID',
    create_time VARCHAR(20) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_post_user (post_id, user_id),
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ===============================================================
-- 3. 检查并补充 user 表的字段
-- ===============================================================
SET @tablename = 'user';

-- 检查 follower_count 字段
SET @columnname = 'follower_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT NOT NULL DEFAULT 0 COMMENT ''粉丝数''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查 following_count 字段
SET @columnname = 'following_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT NOT NULL DEFAULT 0 COMMENT ''关注数''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 follower_count 索引（如果不存在）
SET @indexname = 'idx_follower_count';
SET @columnname = 'follower_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (index_name = @indexname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (', @columnname, ')')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 添加 following_count 索引（如果不存在）
SET @indexname = 'idx_following_count';
SET @columnname = 'following_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (index_name = @indexname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD INDEX ', @indexname, ' (', @columnname, ')')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ===============================================================
-- 4. 检查并补充 bbs_comment 表的字段
-- ===============================================================
SET @tablename = 'bbs_comment';

-- 检查 image_urls 字段
SET @columnname = 'image_urls';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TEXT COMMENT ''评论图片URL（逗号分隔）'' AFTER content')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查 reply_to_comment_id 字段
SET @columnname = 'reply_to_comment_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT COMMENT ''回复的评论ID'' AFTER parent_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查 reply_to_user_id 字段
SET @columnname = 'reply_to_user_id';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' BIGINT COMMENT ''回复的用户ID'' AFTER reply_to_comment_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查 reply_to_user_nickname 字段
SET @columnname = 'reply_to_user_nickname';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50) COMMENT ''回复的用户昵称'' AFTER reply_to_user_id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- ===============================================================
-- 5. 创建关注关系表 bbs_follow（如果不存在）
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
-- 验证脚本
-- ===============================================================

-- 查看bbs_post表结构
DESC bbs_post;

-- 查看bbs_favorite表结构
DESC bbs_favorite;

-- 查看user表结构
DESC user;

-- 查看bbs_comment表结构
DESC bbs_comment;

-- 查看bbs_follow表结构
DESC bbs_follow;

-- 查看所有表的索引
SHOW INDEX FROM bbs_post;
SHOW INDEX FROM bbs_favorite;
SHOW INDEX FROM user;
SHOW INDEX FROM bbs_comment;
SHOW INDEX FROM bbs_follow;
