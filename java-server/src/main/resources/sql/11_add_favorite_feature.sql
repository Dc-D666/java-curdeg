-- ===============================================================
-- 帖子收藏功能数据库表设计脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 创建收藏表 bbs_favorite
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
-- 2. 扩展 bbs_post 表（添加收藏数字段）
-- 请先检查字段是否已存在，如已存在请跳过
-- ===============================================================

-- 添加 favorite_count 字段
-- 如果报错"Duplicate column name"，说明字段已存在，请注释掉这一行
ALTER TABLE bbs_post ADD COLUMN favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数' AFTER view_count;

-- 添加索引
-- 如果报错"Duplicate key name"，说明索引已存在，请注释掉这一行
ALTER TABLE bbs_post ADD INDEX idx_favorite_count (favorite_count);

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看bbs_favorite表结构
DESC bbs_favorite;

-- 查看bbs_post表结构
DESC bbs_post;

-- 查看索引
SHOW INDEX FROM bbs_favorite;
SHOW INDEX FROM bbs_post;
