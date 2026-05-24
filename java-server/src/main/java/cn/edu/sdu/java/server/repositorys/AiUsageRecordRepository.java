package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.AiUsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AiUsageRecordRepository extends JpaRepository<AiUsageRecord, Long> {

    Optional<AiUsageRecord> findByUserIdAndUsageTypeAndUsedDate(Integer userId, String usageType, LocalDate usedDate);
}
