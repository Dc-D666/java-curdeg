-- ===============================================================
-- 完整校园论坛菜单数据脚本
-- 功能：填充完整的菜单树
-- 用户类型：1-super, 2-admin, 3-user
-- ===============================================================

USE java_2_48;

-- ===============================================================
-- 0. 先删除旧菜单数据，重新填充
-- ===============================================================
DELETE FROM menu WHERE pid = 200;
DELETE FROM menu WHERE pid = 300;
DELETE FROM menu WHERE pid = 500;
DELETE FROM menu WHERE id IN (200, 300, 500);

-- ===============================================================
-- 1. 校园论坛一级菜单及二级菜单
-- ===============================================================

-- 一级菜单：校园论坛 (id=200)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (200, NULL, NULL, '校园论坛', '1,2,3');

-- 二级菜单：帖子列表 (pid=200)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (201, 200, 'post-list', '帖子列表', '1,2,3');

-- 二级菜单：发布帖子 (pid=200)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (202, 200, 'post-publish', '发布帖子', '1,2,3');

-- ===============================================================
-- 2. 个人中心一级菜单及二级菜单
-- ===============================================================

-- 一级菜单：个人中心 (id=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (300, NULL, NULL, '个人中心', '1,2,3');

-- 二级菜单：个人资料 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (301, 300, 'personal-profile', '个人资料', '1,2,3');

-- 二级菜单：我的帖子 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (302, 300, 'my-posts', '我的帖子', '1,2,3');

-- 二级菜单：我的收藏 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (303, 300, 'my-favorites', '我的收藏', '1,2,3');

-- 二级菜单：我的关注 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (304, 300, 'my-followers', '我的关注', '1,2,3');

-- 二级菜单：我的通知 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (305, 300, 'my-notification', '我的通知', '1,2,3');

-- 二级菜单：我的举报 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (306, 300, 'my-report', '我的举报', '1,2,3');

-- 二级菜单：数据统计 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (307, 300, 'user-statistics', '数据统计', '1,2,3');

-- 二级菜单：修改密码 (pid=300)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (308, 300, 'password-change', '修改密码', '1,2,3');

-- ===============================================================
-- 3. 系统管理一级菜单及二级菜单（仅管理员）
-- ===============================================================

-- 一级菜单：系统管理 (id=500)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (500, NULL, NULL, '系统管理', '1,2');

-- 二级菜单：举报处理 (pid=500)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (501, 500, 'admin-report', '举报处理', '1,2');

-- 二级菜单：系统概览 (pid=500)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (502, 500, 'system_summary_panel', '系统概览', '1,2');

-- 二级菜单：数据统计 (pid=500)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (503, 500, 'statistics', '数据统计', '1,2');

-- 二级菜单：菜单管理 (pid=500)
INSERT IGNORE INTO menu (id, pid, name, title, user_type_ids) VALUES (504, 500, 'menu-panel', '菜单管理', '1,2');

-- ===============================================================
-- 验证菜单数据
-- ===============================================================

-- 查看完整菜单树
SELECT id, pid, name, title, user_type_ids FROM menu ORDER BY id ASC;

-- 查看一级菜单
SELECT * FROM menu WHERE pid IS NULL ORDER BY id ASC;

-- 查看校园论坛的子菜单
SELECT * FROM menu WHERE pid = 200 ORDER BY id ASC;

-- 查看个人中心的子菜单
SELECT * FROM menu WHERE pid = 300 ORDER BY id ASC;

-- 查看系统管理的子菜单
SELECT * FROM menu WHERE pid = 500 ORDER BY id ASC;

-- ===============================================================
-- 完成！
-- ===============================================================
