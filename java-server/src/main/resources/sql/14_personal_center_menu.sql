-- ===============================================================
-- 个人中心菜单完整脚本
-- 功能：
-- 1. 创建menu表（如果不存在）
-- 2. 添加校园论坛、个人中心、教务管理、系统管理四个一级菜单及其二级菜单
-- 使用前请确保已备份数据库！
-- ===============================================================

-- 使用数据库（请根据实际情况修改数据库名）
USE java_2_48;

-- ===============================================================
-- 0. 创建menu表（如果不存在）
-- ===============================================================

CREATE TABLE IF NOT EXISTS menu (
    id INT NOT NULL COMMENT '菜单ID',
    user_type_ids VARCHAR(255) DEFAULT NULL COMMENT '用户类型ID（逗号分隔）',
    pid INT DEFAULT NULL COMMENT '父菜单ID（NULL表示一级菜单）',
    name VARCHAR(40) DEFAULT NULL COMMENT '菜单名（FXML文件名）',
    title VARCHAR(40) DEFAULT NULL COMMENT '菜单标题（显示名称）',
    PRIMARY KEY (id),
    INDEX idx_pid (pid),
    INDEX idx_user_type_ids (user_type_ids(10))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 确保表结构正确，检查并添加缺失的字段
-- 如果表已经存在，尝试添加字段（不会报错）
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

-- ===============================================================
-- 1. 校园论坛一级菜单及二级菜单
-- ===============================================================

-- 一级菜单：校园论坛 (id=200)
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (200, NULL, '', '校园论坛', '1,2,3');

-- 二级菜单：帖子列表
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (201, 200, 'PostList', '帖子列表', '1,2,3');

-- 二级菜单：发布帖子
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (202, 200, 'PostPublish', '发布帖子', '1,2,3');

-- ===============================================================
-- 2. 个人中心一级菜单及二级菜单
-- ===============================================================

-- 一级菜单：个人中心 (id=300)
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (300, NULL, '', '个人中心', '1,2,3');

-- 二级菜单：个人资料
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (301, 300, 'PersonalProfile', '个人资料', '1,2,3');

-- 二级菜单：我的帖子
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (302, 300, 'MyPosts', '我的帖子', '1,2,3');

-- 二级菜单：我的收藏
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (303, 300, 'MyFavorites', '我的收藏', '1,2,3');

-- 二级菜单：我的关注
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (304, 300, 'MyFollowers', '我的关注', '1,2,3');

-- 二级菜单：我的通知
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (305, 300, 'MyNotification', '我的通知', '1,2,3');

-- 二级菜单：我的举报
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (306, 300, 'MyReport', '我的举报', '1,2,3');

-- 二级菜单：数据统计
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (307, 300, 'UserStatistics', '数据统计', '1,2,3');

-- 二级菜单：修改密码
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (308, 300, 'PasswordChange', '修改密码', '1,2,3');

-- ===============================================================
-- 3. 教务管理一级菜单及二级菜单
-- ===============================================================

-- 一级菜单：教务管理 (id=400)
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (400, NULL, '', '教务管理', '2,3');

-- 二级菜单：学生管理
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (401, 400, 'StudentPanel', '学生管理', '2,3');

-- 二级菜单：课程管理
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (402, 400, 'CoursePanel', '课程管理', '2,3');

-- 二级菜单：成绩管理
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (403, 400, 'ScoreTablePanel', '成绩管理', '2,3');

-- 二级菜单：请假管理
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (404, 400, 'StudentLeavePanel', '请假管理', '2,3');

-- 二级菜单：学生统计
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (405, 400, 'StudentStatisticsPanel', '学生统计', '2,3');

-- ===============================================================
-- 4. 系统管理一级菜单及二级菜单
-- ===============================================================

-- 一级菜单：系统管理 (id=500)
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (500, NULL, '', '系统管理', '3');

-- 二级菜单：举报处理
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (501, 500, 'AdminReport', '举报处理', '3');

-- 二级菜单：系统概览
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (502, 500, 'SystemSummaryPanel', '系统概览', '3');

-- 二级菜单：数据统计
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (503, 500, 'Statistics', '数据统计', '3');

-- 二级菜单：菜单管理
INSERT IGNORE INTO menu (id, pid, name, title, userTypeIds) 
VALUES (504, 500, 'MenuPanel', '菜单管理', '3');

-- ===============================================================
-- 验证脚本
-- ===============================================================

-- 查看menu表结构
DESC menu;

-- 查看所有菜单（按ID排序）
SELECT * FROM menu ORDER BY id ASC;

-- 查看一级菜单（pid为NULL）
SELECT * FROM menu WHERE pid IS NULL ORDER BY id ASC;

-- 查看校园论坛的子菜单
SELECT * FROM menu WHERE pid = 200 ORDER BY id ASC;

-- 查看个人中心的子菜单
SELECT * FROM menu WHERE pid = 300 ORDER BY id ASC;

-- 查看教务管理的子菜单
SELECT * FROM menu WHERE pid = 400 ORDER BY id ASC;

-- 查看系统管理的子菜单
SELECT * FROM menu WHERE pid = 500 ORDER BY id ASC;

-- ===============================================================
-- 完成！
-- ===============================================================
