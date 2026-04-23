-- ===============================================================
-- 更新现有通知的 type 和 title 字段（完全兼容MySQL安全更新模式）
-- ===============================================================

USE java_2_48;

-- 方案1：临时禁用安全更新（最可靠的方案）
SET SQL_SAFE_UPDATES = 0;

-- 举报处理通知（内容包含"举报"）
UPDATE bbs_notification 
SET type = 2, title = '举报处理通知' 
WHERE id IS NOT NULL AND content LIKE '%举报%';

-- 帖子审核通知（内容包含"帖子"和"审核"）
UPDATE bbs_notification 
SET type = 3, title = '帖子审核通知' 
WHERE id IS NOT NULL AND content LIKE '%帖子%' AND content LIKE '%审核%';

-- 评论回复通知（内容包含"评论"或"回复"）
UPDATE bbs_notification 
SET type = 4, title = '评论回复通知' 
WHERE id IS NOT NULL AND (content LIKE '%评论%' OR content LIKE '%回复%');

-- 剩余的设为系统通知（确保所有记录都有值）
UPDATE bbs_notification 
SET type = 1, title = '系统通知' 
WHERE id IS NOT NULL AND (type IS NULL OR title IS NULL);

-- 重新启用安全更新模式
SET SQL_SAFE_UPDATES = 1;

-- ===============================================================
-- 方案2：使用明确的主键范围（如果不允许禁用安全更新）
-- ===============================================================
-- 如果方案1仍然失败，请使用以下方案，先查询最大ID，然后按范围更新
--
-- -- 第一步：查询最大ID
-- SELECT MAX(id) INTO @max_id FROM bbs_notification;
--
-- -- 举报处理通知
-- UPDATE bbs_notification 
-- SET type = 2, title = '举报处理通知' 
-- WHERE id BETWEEN 1 AND @max_id AND content LIKE '%举报%';
--
-- -- 帖子审核通知
-- UPDATE bbs_notification 
-- SET type = 3, title = '帖子审核通知' 
-- WHERE id BETWEEN 1 AND @max_id AND content LIKE '%帖子%' AND content LIKE '%审核%';
--
-- -- 评论回复通知
-- UPDATE bbs_notification 
-- SET type = 4, title = '评论回复通知' 
-- WHERE id BETWEEN 1 AND @max_id AND (content LIKE '%评论%' OR content LIKE '%回复%');
--
-- -- 剩余的设为系统通知
-- UPDATE bbs_notification 
-- SET type = 1, title = '系统通知' 
-- WHERE id BETWEEN 1 AND @max_id AND (type IS NULL OR title IS NULL);

-- ===============================================================
-- 验证更新结果
-- ===============================================================
SELECT 
    type,
    title,
    COUNT(*) AS count,
    GROUP_CONCAT(id ORDER BY id SEPARATOR ',') AS ids
FROM bbs_notification
GROUP BY type, title
ORDER BY type;

-- 查看所有通知的详细信息（可选）
-- SELECT id, type, title, LEFT(content, 50) AS content_preview, is_read, create_time
-- FROM bbs_notification
-- ORDER BY id DESC;
