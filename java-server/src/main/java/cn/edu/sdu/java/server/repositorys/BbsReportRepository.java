package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsReportRepository extends JpaRepository<BbsReport, Long> {

    Page<BbsReport> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Page<BbsReport> findByReporterIdOrderByCreateTimeDesc(Long reporterId, Pageable pageable);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT COUNT(*) FROM bbs_report", nativeQuery = true)
    Long countTotalReports();

    @Query(value = "SELECT COUNT(*) FROM bbs_report WHERE status = 0", nativeQuery = true)
    Long countPendingReports();

    @Query(value = "SELECT target_type, COUNT(*) as count FROM bbs_report GROUP BY target_type ORDER BY count DESC", nativeQuery = true)
    List<Object[]> countReportsByType();

    @Query(value = "SELECT COUNT(*) FROM bbs_report WHERE status = 2", nativeQuery = true)
    Long countValidReports();
}
