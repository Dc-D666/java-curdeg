-- ===============================================================
-- 添加申诉相关菜单
-- ===============================================================

USE java_2_48;

-- 添加顶部帮助菜单中的"禁言申诉"（id=507）
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (507, 405, 'ban_appeal', '禁言申诉', '1,2,3')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    title = VALUES(title),
    user_type_ids = VALUES(user_type_ids);

-- 添加系统管理菜单下的"申诉管理"（id=509）
INSERT INTO menu (id, pid, name, title, user_type_ids) 
VALUES (509, 500, 'ban_appeal_admin', '申诉管理', '1,2')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    title = VALUES(title),
    user_type_ids = VALUES(user_type_ids);

-- 验证菜单是否添加成功
SELECT * FROM menu WHERE id IN (507, 509);

-- 完成！
