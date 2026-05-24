-- ===============================================================
-- 【统计功能增强脚本】
-- 为统计功能添加必要的字段和索引（安全执行）
-- 说明：所有ALTER TABLE操作都会检查字段是否存在，避免重复添加错误
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 扩展 bbs_sensitive_word 表：添加触发次数统计字段
-- ===============================================================

-- 添加 trigger_count 字段（记录敏感词被触发的次数）
-- 使用存储过程安全添加字段，避免"Duplicate column name"错误
DELIMITER $$
DROP PROCEDURE IF EXISTS add_column_if_not_exists$$
CREATE PROCEDURE add_column_if_not_exists(
    IN table_name VARCHAR(100),
    IN column_name VARCHAR(100),
    IN column_definition TEXT
)
BEGIN
    DECLARE column_count INT DEFAULT 0;
    SELECT COUNT(*) INTO column_count 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = table_name 
      AND COLUMN_NAME = column_name;
    
    IF column_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` ADD COLUMN `', column_name, '` ', column_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('字段 `', column_name, '` 已添加到表 `', table_name, '`') AS result;
    ELSE
        SELECT CONCAT('字段 `', column_name, '` 已存在于表 `', table_name, '`，跳过') AS result;
    END IF;
END$$
DELIMITER ;

-- 为 bbs_sensitive_word 表添加 trigger_count 字段
CALL add_column_if_not_exists('bbs_sensitive_word', 'trigger_count', 'INT NOT NULL DEFAULT 0 COMMENT "触发次数"');

-- 添加索引优化统计查询
DELIMITER $$
DROP PROCEDURE IF EXISTS add_index_if_not_exists$$
CREATE PROCEDURE add_index_if_not_exists(
    IN table_name VARCHAR(100),
    IN index_name VARCHAR(100),
    IN index_definition TEXT
)
BEGIN
    DECLARE index_count INT DEFAULT 0;
    SELECT COUNT(*) INTO index_count 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = table_name 
      AND INDEX_NAME = index_name;
    
    IF index_count = 0 THEN
        SET @sql = CONCAT('ALTER TABLE `', table_name, '` ADD INDEX `', index_name, '` ', index_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('索引 `', index_name, '` 已添加到表 `', table_name, '`') AS result;
    ELSE
        SELECT CONCAT('索引 `', index_name, '` 已存在于表 `', table_name, '`，跳过') AS result;
    END IF;
END$$
DELIMITER ;

-- 为 bbs_sensitive_word 表的 trigger_count 添加索引
CALL add_index_if_not_exists('bbs_sensitive_word', 'idx_trigger_count', '(trigger_count)');

-- ===============================================================
-- 2. 为其他表添加索引优化统计查询（可选但推荐）
-- ===============================================================

-- 为 bbs_post 添加组合索引优化时间范围查询
CALL add_index_if_not_exists('bbs_post', 'idx_create_time_status', '(create_time, status)');

-- 为 bbs_comment 添加组合索引优化时间范围查询
CALL add_index_if_not_exists('bbs_comment', 'idx_create_time_status', '(create_time, status)');

-- 为 bbs_like 添加索引优化时间范围查询（如果该表有DATETIME类型的create_time）
-- 注意：此步需要检查表结构，此处暂跳过

-- ===============================================================
-- 3. 清理临时存储过程
-- ===============================================================

DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;

-- ===============================================================
-- 验证脚本
-- ===============================================================

SELECT '=== 验证 bbs_sensitive_word 表结构 ===' AS info;
DESC bbs_sensitive_word;

SELECT '=== 验证索引 ===' AS info;
SHOW INDEX FROM bbs_sensitive_word;

SELECT '统计功能增强脚本执行完成！' AS status;
