package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.DailyLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyLimitRepository extends JpaRepository<DailyLimit, Long> {

    Optional<DailyLimit> findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(Integer userId,
                                                                           String limitType,
                                                                           String limitKey,
                                                                           LocalDate recordDate);

    List<DailyLimit> findByUserIdAndRecordDate(Integer userId, LocalDate recordDate);

    List<DailyLimit> findByUserIdAndLimitTypeAndRecordDate(Integer userId, String limitType, LocalDate recordDate);

    Boolean existsByUserIdAndLimitTypeAndLimitKeyAndRecordDate(Integer userId,
                                                                String limitType,
                                                                String limitKey,
                                                                LocalDate recordDate);
}
