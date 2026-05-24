-- ===============================================================
-- 浏览历史菜单项修复脚本
-- 执行前请确保已备份数据库！
-- ===============================================================

USE java_2_48;

-- 先查看 ID 309 的菜单项是什么
SELECT * FROM menu WHERE id = 309;

-- 如果 ID 309 被其他菜单占用，先删除
DELETE FROM menu WHERE id = 309;

-- 查看个人中心下最大的菜单 ID
SELECT MAX(id) FROM menu WHERE pid = 300;

-- 添加浏览历史菜单项（使用 ID 310 避免冲突）
INSERT INTO menu (id, pid, name, title, user_type_ids) VALUES (310, 300, 'browse-history', '浏览历史', '1,2,3');

-- 验证
SELECT * FROM menu WHERE pid = 300 ORDER BY id ASC;
