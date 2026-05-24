-- ===============================================================
-- 诊断和修复脚本
-- ===============================================================

USE java_2_48;

-- ===============================================================
-- 1. 查看当前user表结构
-- ===============================================================
SELECT '=== 当前user表结构 ===' AS info;
DESC user;

-- ===============================================================
-- 2. 检查字段是否存在
-- ===============================================================
SELECT '=== 检查字段是否存在 ===' AS info;
SELECT 
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'user'
AND COLUMN_NAME IN ('follower_count', 'following_count')
ORDER BY ORDINAL_POSITION;

-- ===============================================================
-- 3. 检查索引是否存在
-- ===============================================================
SELECT '=== 检查索引是否存在 ===' AS info;
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'user'
AND INDEX_NAME IN ('idx_follower_count', 'idx_following_count')
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- ===============================================================
-- 4. 查看bbs_follow表是否存在
-- ===============================================================
SELECT '=== 检查bbs_follow表 ===' AS info;
SHOW TABLES LIKE 'bbs_follow';
