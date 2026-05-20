-- ===============================================================
-- 查看当前菜单表的所有菜单项
-- ===============================================================

USE java_2_48;

-- 查看所有菜单
SELECT * FROM menu ORDER BY id ASC;

-- 查看个人中心的所有子菜单
SELECT * FROM menu WHERE pid = 300 ORDER BY id ASC;

-- 查看最大的菜单ID，方便确定新的ID
SELECT MAX(id) as max_id FROM menu;
