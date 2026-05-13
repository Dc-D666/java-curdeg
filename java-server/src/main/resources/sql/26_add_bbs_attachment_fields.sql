bbs_post-- 帖子与评论附件字段扩展
-- 安全脚本：字段不存在时才添加，重复执行不会报错。

SET @schema_name = DATABASE();

SET @post_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'bbs_post'
      AND COLUMN_NAME = 'attachment_infos'
);

SET @post_sql = IF(
    @post_column_exists = 0,
    'ALTER TABLE bbs_post ADD COLUMN attachment_infos TEXT COMMENT ''帖子附件元数据JSON'' AFTER image_urls',
    'SELECT ''bbs_post.attachment_infos already exists'''
);
PREPARE post_stmt FROM @post_sql;
EXECUTE post_stmt;
DEALLOCATE PREPARE post_stmt;

SET @comment_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'bbs_comment'
      AND COLUMN_NAME = 'attachment_infos'
);

SET @comment_sql = IF(
    @comment_column_exists = 0,
    'ALTER TABLE bbs_comment ADD COLUMN attachment_infos TEXT COMMENT ''评论附件元数据JSON'' AFTER image_urls',
    'SELECT ''bbs_comment.attachment_infos already exists'''
);
PREPARE comment_stmt FROM @comment_sql;
EXECUTE comment_stmt;
DEALLOCATE PREPARE comment_stmt;
