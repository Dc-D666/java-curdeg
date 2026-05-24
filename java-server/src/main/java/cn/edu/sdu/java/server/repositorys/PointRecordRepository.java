package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PointRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointRecordRepository extends JpaRepository<PointRecord, Long> {

    List<PointRecord> findByUserIdOrderByCreateTimeDesc(Integer userId);

    Page<PointRecord> findByUserIdOrderByCreateTimeDesc(Integer userId, Pageable pageable);

    List<PointRecord> findByUserIdAndRuleCodeOrderByCreateTimeDesc(Integer userId, String ruleCode);

    @Query("SELECT SUM(pr.pointsChange) FROM PointRecord pr WHERE pr.userId = :userId AND pr.ruleCode = :ruleCode AND pr.createTime >= :startTime")
    Integer sumPointsChangeByUserIdAndRuleCodeAndCreateTimeAfter(@Param("userId") Integer userId,
                                                                   @Param("ruleCode") String ruleCode,
                                                                   @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(pr) FROM PointRecord pr WHERE pr.userId = :userId AND pr.ruleCode = :ruleCode AND pr.createTime >= :startTime")
    Long countByUserIdAndRuleCodeAndCreateTimeAfter(@Param("userId") Integer userId,
                                                      @Param("ruleCode") String ruleCode,
                                                      @Param("startTime") LocalDateTime startTime);

    List<PointRecord> findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(Integer userId,
                                                                              LocalDateTime startTime,
                                                                              LocalDateTime endTime);

    List<PointRecord> findByCreateTimeAfter(LocalDateTime time);

    List<PointRecord> findByUserIdAndCreateTimeAfter(Integer userId, LocalDateTime time);
}
