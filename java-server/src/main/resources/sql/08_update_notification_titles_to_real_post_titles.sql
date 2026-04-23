-- ===============================================================
-- 更新通知标题为真实帖子标题（兼容MySQL安全更新模式）
-- ===============================================================

USE java_2_48;

-- 临时禁用安全更新模式
SET SQL_SAFE_UPDATES = 0;

-- 创建临时表存储帖子ID和标题的映射
CREATE TEMPORARY TABLE temp_post_titles AS
SELECT id, title FROM bbs_post;

-- 创建索引加速更新
ALTER TABLE temp_post_titles ADD INDEX idx_id (id);

-- 更新评论回复通知（type=4）：从内容中提取帖子ID，关联获取真实标题
UPDATE bbs_notification n
INNER JOIN temp_post_titles p ON 
    n.content REGEXP CONCAT('帖子ID[：: ]+', p.id, '[^0-9]')
    OR n.content REGEXP CONCAT('帖子ID[：: ]+', p.id, '$')
SET n.title = p.title
WHERE n.type = 4 AND n.id > 0;

-- 更新帖子审核通知（type=3）：从内容中提取帖子ID，关联获取真实标题
UPDATE bbs_notification n
INNER JOIN temp_post_titles p ON 
    n.content REGEXP CONCAT('帖子ID[：: ]+', p.id, '[^0-9]')
    OR n.content REGEXP CONCAT('帖子ID[：: ]+', p.id, '$')
SET n.title = p.title
WHERE n.type = 3 AND n.id > 0;

-- 更新举报处理通知（type=2）：从内容中提取帖子ID，关联获取真实标题
UPDATE bbs_notification n
INNER JOIN temp_post_titles p ON 
    n.content REGEXP CONCAT('ID[：: ]+', p.id, '[^0-9]')
    OR n.content REGEXP CONCAT('ID[：: ]+', p.id, '$')
SET n.title = p.title
WHERE n.type = 2 AND n.id > 0;

-- 系统通知保持不变（type=1）

-- 删除临时表
DROP TEMPORARY TABLE temp_post_titles;

-- 重新启用安全更新模式
SET SQL_SAFE_UPDATES = 1;

-- 验证更新结果
SELECT 
    type,
    CASE type
        WHEN 1 THEN '系统通知'
        WHEN 2 THEN '举报处理通知'
        WHEN 3 THEN '帖子审核通知'
        WHEN 4 THEN '评论回复通知'
        ELSE '未知'
    END AS type_name,
    COUNT(*) AS count,
    GROUP_CONCAT(DISTINCT LEFT(title, 30) SEPARATOR '; ') AS title_samples
FROM bbs_notification
GROUP BY type
ORDER BY type;
