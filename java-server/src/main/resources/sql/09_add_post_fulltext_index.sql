-- ===============================================================
-- 为帖子表添加全文索引
-- ===============================================================

USE java_2_48;

-- 为 bbs_post 表的 title 和 content 字段添加全文索引
ALTER TABLE bbs_post 
ADD FULLTEXT INDEX idx_post_search (title, content);

-- 验证索引创建
SHOW INDEX FROM bbs_post WHERE Key_name = 'idx_post_search';
