-- ===============================================================
-- 校园论坛数据库表设计脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 1. 扩展 sys_user 表（添加社区业务字段）
-- ===============================================================
ALTER TABLE sys_user
ADD COLUMN student_id VARCHAR(20) NOT NULL COMMENT '学号（社区用户的唯一标识）' AFTER person_id,
ADD COLUMN nickname VARCHAR(50) NOT NULL COMMENT '昵称（社区展示名）' AFTER student_id,
ADD COLUMN avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像URL' AFTER nickname,
ADD COLUMN signature VARCHAR(200) DEFAULT NULL COMMENT '个性签名' AFTER avatar_url,
ADD COLUMN post_count INT NOT NULL DEFAULT 0 COMMENT '发帖数' AFTER signature,
ADD COLUMN comment_count INT NOT NULL DEFAULT 0 COMMENT '回帖数' AFTER post_count,
ADD COLUMN violation_count INT NOT NULL DEFAULT 0 COMMENT '违规记录数' AFTER comment_count,
ADD COLUMN is_banned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否被禁言（0=否，1=是）' AFTER violation_count,
ADD UNIQUE INDEX idx_student_id (student_id),
ADD INDEX idx_nickname (nickname),
ADD INDEX idx_post_count (post_count),
ADD INDEX idx_comment_count (comment_count),
ADD INDEX idx_violation_count (violation_count),
ADD INDEX idx_is_banned (is_banned);

-- ===============================================================
-- 2. 创建板块表 bbs_board
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_board (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '板块ID',
    name VARCHAR(50) NOT NULL COMMENT '板块名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '板块描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '板块排序（数字越小越靠前）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX idx_name (name),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- 插入5条测试数据
INSERT INTO bbs_board (name, description, sort_order) VALUES
('学习交流', '分享学习心得、讨论课程问题', 1),
('校园生活', '校园日常、活动分享、生活求助', 2),
('公告通知', '官方公告、重要通知', 3),
('资源分享', '学习资料、软件工具、电子书分享', 4),
('闲聊水区', '轻松聊天、话题讨论', 5);

-- ===============================================================
-- 3. 创建帖子表 bbs_post
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_post (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    title VARCHAR(100) NOT NULL COMMENT '帖子标题',
    content TEXT NOT NULL COMMENT '帖子内容',
    image_urls TEXT DEFAULT NULL COMMENT '图片URL列表（逗号分隔）',
    board_id BIGINT NOT NULL COMMENT '所属板块ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    is_top TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶（0=否，1=是）',
    is_featured TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加精（0=否，1=是）',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '帖子状态（0=下架，1=正常）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_title (title),
    INDEX idx_board_id (board_id),
    INDEX idx_author_id (author_id),
    INDEX idx_like_count (like_count),
    INDEX idx_comment_count (comment_count),
    INDEX idx_view_count (view_count),
    INDEX idx_is_top (is_top),
    INDEX idx_is_featured (is_featured),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ===============================================================
-- 4. 创建评论表 bbs_comment
-- ===============================================================
CREATE TABLE IF NOT EXISTS bbs_comment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '所属帖子ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID（NULL=顶级评论）',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '评论状态（0=下架，1=正常）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_post_id (post_id),
    INDEX idx_author_id (author_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_like_count (like_count),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看sys_user表结构
DESC sys_user;

-- 查看bbs_board表结构
DESC bbs_board;

-- 查看bbs_board测试数据
SELECT * FROM bbs_board ORDER BY sort_order ASC;

-- 查看bbs_post表结构
DESC bbs_post;

-- 查看bbs_comment表结构
DESC bbs_comment;
