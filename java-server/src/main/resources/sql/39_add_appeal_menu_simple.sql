-- ===============================================================
-- 简单添加申诉管理到左侧菜单
-- ===============================================================

USE java_2_48;

-- 首先查看系统管理的菜单ID
-- 执行: SELECT * FROM menu WHERE title LIKE '%系统%' 或 title LIKE '%管理%'
-- 找到系统管理的id后，将下面的 X 替换为该ID

-- 假设系统管理的ID是5（需要根据实际查询结果调整）
-- 添加申诉管理菜单
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (509, 5, 'ban_appeal_admin', '申诉管理', '1,2')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    title = VALUES(title),
    user_type_ids = VALUES(user_type_ids);

-- 验证是否添加成功
SELECT * FROM menu WHERE id = 509;

-- 查看系统管理的所有子菜单
SELECT * FROM menu WHERE pid = 5;

-- 完成！
