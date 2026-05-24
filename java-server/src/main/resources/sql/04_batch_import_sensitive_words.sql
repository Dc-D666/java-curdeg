-- ============================================
-- 敏感词批量导入SQL脚本
-- 使用方法：
-- 1. 准备一个CSV格式的敏感词文件，格式：敏感词,等级
-- 2. 修改下面的文件路径为你的实际路径
-- 3. 在MySQL中执行此脚本
-- ============================================

-- 注意：使用LOAD DATA需要确保MySQL有文件读取权限
-- 如果遇到权限问题，可以尝试：
-- 1. 将文件放在MySQL的secure_file_priv目录下
-- 2. 或者使用Python脚本导入（推荐）

-- 示例CSV文件内容（sensitive_words.csv）：
-- 敏感词1,1
-- 敏感词2,1
-- 严重违规1,2
-- 严重违规2,2

-- 批量导入（请修改文件路径）
/*
LOAD DATA LOCAL INFILE 'D:/java-curdeg/java-server/src/main/resources/sql/sensitive_words.csv'
INTO TABLE bbs_sensitive_word
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(word, level);
*/

-- 如果需要清空表后重新导入（谨慎使用！）
-- TRUNCATE TABLE bbs_sensitive_word;

-- 查看导入结果
SELECT 
    COUNT(*) AS total_count,
    SUM(CASE WHEN level = 1 THEN 1 ELSE 0 END) AS level_1_count,
    SUM(CASE WHEN level = 2 THEN 1 ELSE 0 END) AS level_2_count
FROM bbs_sensitive_word;

-- 查看最近导入的10条记录
SELECT * FROM bbs_sensitive_word ORDER BY id DESC LIMIT 10;
