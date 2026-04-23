-- 为 Person 表添加隐私设置字段
-- 隐私设置枚举：PUBLIC（全员可见）, FOLLOWING（相互关注可见）, PRIVATE（仅自己可见）

ALTER TABLE person 
ADD COLUMN name_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '姓名隐私设置',
ADD COLUMN dept_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '学院隐私设置',
ADD COLUMN gender_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '性别隐私设置',
ADD COLUMN birthday_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '生日隐私设置',
ADD COLUMN email_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '邮箱隐私设置',
ADD COLUMN phone_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '电话隐私设置',
ADD COLUMN address_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '地址隐私设置',
ADD COLUMN introduce_privacy VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '个人简介隐私设置';
