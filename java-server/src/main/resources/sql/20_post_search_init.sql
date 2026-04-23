-- ===============================================================
-- 帖子检索功能 - 数据库初始化脚本
-- 一键运行，安全无风险（已做索引存在性检查）
-- ===============================================================

USE java_2_48;

-- ===============================================================
-- 步骤1：确保全文索引存在（如果不存在则创建）
-- ===============================================================
SET @index_exists = (
    SELECT COUNT(1) 
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'bbs_post' 
    AND INDEX_NAME = 'idx_post_search'
);

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE bbs_post ADD FULLTEXT INDEX idx_post_search (title, content)',
    'SELECT ''索引 idx_post_search 已存在，跳过'' AS status');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ===============================================================
-- 步骤2：验证和完成
-- ===============================================================
SHOW INDEX FROM bbs_post WHERE Key_name = 'idx_post_search';

SELECT '帖子检索功能数据库初始化完成！' AS message;
