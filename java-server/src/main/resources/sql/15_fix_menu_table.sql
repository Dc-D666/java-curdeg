-- ===============================================================
-- 修复menu表字段脚本
-- 功能：确保menu表有user_type_ids字段
-- ===============================================================

USE java_2_48;

-- 先检查并添加字段user_type_ids（如果不存在）
-- 方法1：尝试添加，如果已存在会报错但没关系
ALTER TABLE menu ADD COLUMN IF NOT EXISTS user_type_ids VARCHAR(255) DEFAULT NULL COMMENT '用户类型ID（逗号分隔）' AFTER id;

-- 方法2：更安全的方式，检查后再添加
SET @dbname = DATABASE();
SET @tablename = 'menu';
SET @columnname = 'user_type_ids';

SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (table_schema = @dbname)
            AND (table_name = @tablename)
            AND (column_name = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) DEFAULT NULL COMMENT "用户类型ID（逗号分隔）" AFTER id')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 验证字段是否存在
DESC menu;
