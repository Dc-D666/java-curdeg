package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BbsReportRepository extends JpaRepository<BbsReport, Long> {

    Page<BbsReport> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Page<BbsReport> findByReporterIdOrderByCreateTimeDesc(Long reporterId, Pageable pageable);
}
