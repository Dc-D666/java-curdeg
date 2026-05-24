-- 收藏帖子功能 - 安全的数据库变更脚本
-- 执行日期: 2026-04-11
-- 这个版本会先检查列是否存在，避免报错

-- ============================================
-- 1. 创建收藏表 bbs_favorite
-- ============================================
CREATE TABLE IF NOT EXISTS bbs_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id INT NOT NULL COMMENT '用户ID',
    create_time VARCHAR(255) COMMENT '创建时间',
    UNIQUE KEY uk_post_user (post_id, user_id) COMMENT '帖子-用户唯一约束',
    INDEX idx_post_id (post_id) COMMENT '帖子ID索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ============================================
-- 2. 在 bbs_post 表中添加 favorite_count 列（安全版本）
-- ============================================
-- 使用存储过程来安全地添加列
DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_not_exists$$

CREATE PROCEDURE add_column_if_not_exists()
BEGIN
    -- 检查列是否存在
    IF NOT EXISTS (
        SELECT * FROM information_schema.columns 
        WHERE table_schema = DATABASE() 
        AND table_name = 'bbs_post' 
        AND column_name = 'favorite_count'
    ) THEN
        -- 列不存在，添加列
        ALTER TABLE bbs_post 
        ADD COLUMN favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数量'
        AFTER view_count;
        
        SELECT 'Column favorite_count added successfully' AS result;
    ELSE
        SELECT 'Column favorite_count already exists' AS result;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL add_column_if_not_exists();

-- 删除存储过程
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- ============================================
-- 3. （可选）如果有现有数据，可以初始化收藏数量
-- ============================================
-- 注意：这一步只有在你需要根据现有收藏记录更新计数时才需要执行
-- UPDATE bbs_post p 
-- SET favorite_count = (
--     SELECT COUNT(*) FROM bbs_favorite f WHERE f.post_id = p.id
-- );
