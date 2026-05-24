
package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsModerationLogRepository extends JpaRepository<BbsModerationLog, Long>{
    List<BbsModerationLog>findByPostIdOrderByCreateTimeDesc(Long postId);
    List<BbsModerationLog>findByModeratorIdOrderByCreateTimeDesc(Integer moderatorId);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT COUNT(*) FROM bbs_moderation_log", nativeQuery = true)
    Long countTotalModerations();

    @Query(value = "SELECT DATE(create_time) as date, " +
           "SUM(CASE WHEN new_status = 'pass' THEN 1 ELSE 0 END) as pass_count, " +
           "SUM(CASE WHEN new_status = 'reject' THEN 1 ELSE 0 END) as reject_count, " +
           "SUM(CASE WHEN new_status = 'manual' THEN 1 ELSE 0 END) as manual_count " +
           "FROM bbs_moderation_log " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyModerationTrend(@Param("days") Integer days);

    @Query(value = "SELECT violation_type, COUNT(*) as count FROM bbs_moderation_log " +
           "WHERE violation_type IS NOT NULL AND violation_type != '' " +
           "GROUP BY violation_type ORDER BY count DESC", nativeQuery = true)
    List<Object[]> countViolationTypes();

    @Query(value = "SELECT COUNT(*) FROM bbs_moderation_log WHERE new_status = 'pass'", nativeQuery = true)
    Long countPassedModerations();

    @Query(value = "SELECT COUNT(*) FROM bbs_moderation_log WHERE new_status = 'reject'", nativeQuery = true)
    Long countRejectedModerations();
}
