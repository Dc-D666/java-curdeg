-- ===============================================================
-- 【完整重置脚本】
-- 1. 删除所有表
-- 2. 重新创建基础框架核心表
-- 3. 创建社区业务表
-- 4. 插入测试数据
-- ===============================================================

USE java_2_48;

-- ===============================================================
-- 第一步：删除所有表（按依赖关系顺序）
-- ===============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS bbs_comment;
DROP TABLE IF EXISTS bbs_post;
DROP TABLE IF EXISTS bbs_board;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS user_type;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS statistics_day;
DROP TABLE IF EXISTS student_statistics;
DROP TABLE IF EXISTS student_leave;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS score;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS family_member;
DROP TABLE IF EXISTS fee;
DROP TABLE IF EXISTS menu;
DROP TABLE IF EXISTS modify_log;
DROP TABLE IF EXISTS request_log;
DROP TABLE IF EXISTS system_info;
DROP TABLE IF EXISTS dictionary;

SET FOREIGN_KEY_CHECKS = 1;

-- ===============================================================
-- 第二步：重新创建基础框架核心表
-- ===============================================================

-- 1. person 表（人员信息表）
CREATE TABLE person (
    person_id INT NOT NULL AUTO_INCREMENT COMMENT '人员ID',
    num VARCHAR(20) NOT NULL COMMENT '人员编号',
    name VARCHAR(50) DEFAULT NULL COMMENT '人员名称',
    type VARCHAR(2) DEFAULT NULL COMMENT '人员类型（0管理员 1学生 2教师）',
    dept VARCHAR(50) DEFAULT NULL COMMENT '学院',
    card VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    gender VARCHAR(2) DEFAULT NULL COMMENT '性别（1男 2女）',
    birthday VARCHAR(10) DEFAULT NULL COMMENT '出生日期',
    email VARCHAR(60) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '电话',
    address VARCHAR(20) DEFAULT NULL COMMENT '地址',
    introduce VARCHAR(1000) DEFAULT NULL COMMENT '个人简介',
    photo LONGBLOB DEFAULT NULL COMMENT '照片',
    PRIMARY KEY (person_id),
    UNIQUE KEY idx_num (num)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员信息表';

-- 2. user_type 表（用户类型表）
CREATE TABLE user_type (
    id INT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(20) DEFAULT NULL COMMENT '角色名称',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户类型表';

-- 插入三种角色
INSERT INTO user_type (name) VALUES 
('ROLE_SUPER'),
('ROLE_ADMIN'),
('ROLE_STUDENT');

-- 3. user 表（用户账号表，含社区业务扩展字段）
CREATE TABLE user (
    person_id INT NOT NULL COMMENT '用户ID',
    user_type_id INT DEFAULT NULL COMMENT '用户类型ID',
    user_name VARCHAR(20) NOT NULL COMMENT '登录账号',
    password VARCHAR(60) NOT NULL COMMENT '密码',
    login_count INT DEFAULT NULL COMMENT '登录次数',
    last_login_time VARCHAR(20) DEFAULT NULL COMMENT '最后登录时间',
    create_time VARCHAR(20) DEFAULT NULL COMMENT '创建时间',
    creator_id INT DEFAULT NULL COMMENT '创建者ID',
    
    -- 社区业务扩展字段
    student_id VARCHAR(20) NOT NULL COMMENT '学号（社区用户的唯一标识）',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称（社区展示名）',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    signature VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
    post_count INT NOT NULL DEFAULT 0 COMMENT '发帖数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '回帖数',
    violation_count INT NOT NULL DEFAULT 0 COMMENT '违规记录数',
    is_banned TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否被禁言（0=否，1=是）',
    
    PRIMARY KEY (person_id),
    UNIQUE KEY idx_user_name (user_name),
    UNIQUE KEY idx_student_id (student_id),
    KEY idx_user_type_id (user_type_id),
    KEY idx_nickname (nickname),
    KEY idx_post_count (post_count),
    KEY idx_comment_count (comment_count),
    KEY idx_violation_count (violation_count),
    KEY idx_is_banned (is_banned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

-- ===============================================================
-- 第三步：创建社区业务表
-- ===============================================================

-- 4. bbs_board 表（板块表）
CREATE TABLE bbs_board (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '板块ID',
    name VARCHAR(50) NOT NULL COMMENT '板块名称',
    description VARCHAR(200) DEFAULT NULL COMMENT '板块描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '板块排序（数字越小越靠前）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY idx_name (name),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- 5. bbs_post 表（帖子表）
CREATE TABLE bbs_post (
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
    KEY idx_title (title),
    KEY idx_board_id (board_id),
    KEY idx_author_id (author_id),
    KEY idx_like_count (like_count),
    KEY idx_comment_count (comment_count),
    KEY idx_view_count (view_count),
    KEY idx_is_top (is_top),
    KEY idx_is_featured (is_featured),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 6. bbs_comment 表（评论表）
CREATE TABLE bbs_comment (
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
    KEY idx_post_id (post_id),
    KEY idx_author_id (author_id),
    KEY idx_parent_id (parent_id),
    KEY idx_like_count (like_count),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ===============================================================
-- 第四步：插入测试数据
-- ===============================================================

-- 插入板块测试数据
INSERT INTO bbs_board (name, description, sort_order) VALUES
('学习交流', '分享学习心得、讨论课程问题', 1),
('校园生活', '校园日常、活动分享、生活求助', 2),
('公告通知', '官方公告、重要通知', 3),
('资源分享', '学习资料、软件工具、电子书分享', 4),
('闲聊水区', '轻松聊天、话题讨论', 5);

-- 插入测试人员和用户（密码都是 123456，BCrypt加密）
INSERT INTO person (num, name, type, email) VALUES
('2024001', '超级管理员', '0', 'admin@example.com'),
('2024002', '张三', '1', 'zhangsan@example.com'),
('2024003', '李四', '1', 'lisi@example.com');

INSERT INTO user (person_id, user_type_id, user_name, password, student_id, nickname, create_time) VALUES
(1, 1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '2024001', '超级管理员', NOW()),
(2, 3, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '2024002', '张三', NOW()),
(3, 3, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '2024003', '李四', NOW());

-- ===============================================================
-- 验证结果
-- ===============================================================

SHOW TABLES;

SELECT '基础框架表：' AS info;
DESC person;
DESC user_type;
DESC user;

SELECT '社区业务表：' AS info;
DESC bbs_board;
DESC bbs_post;
DESC bbs_comment;

SELECT '板块测试数据：' AS info;
SELECT * FROM bbs_board ORDER BY sort_order ASC;

SELECT '测试用户：' AS info;
SELECT person_id, user_name, student_id, nickname FROM user;
