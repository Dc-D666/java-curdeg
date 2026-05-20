-- 修复历史数据：举报处理为“删除内容”的帖子，必须同步为下架和违规状态
UPDATE bbs_post p
JOIN bbs_report r ON r.target_type = 1 AND r.target_id = p.id
SET
    p.status = 0,
    p.moderation_status = 'reject',
    p.moderation_violation_level = COALESCE(p.moderation_violation_level, '一般'),
    p.moderation_violation_type = COALESCE(p.moderation_violation_type, '举报处理'),
    p.moderation_remark = CONCAT('举报成立，内容已删除。处理备注：', COALESCE(r.handle_remark, '无')),
    p.moderation_time = COALESCE(p.moderation_time, r.handle_time)
WHERE r.status = 1
  AND r.handle_type = 1
  AND (p.status <> 0 OR p.moderation_status <> 'reject');
