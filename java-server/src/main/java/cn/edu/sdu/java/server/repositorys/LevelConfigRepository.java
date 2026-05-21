package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.LevelConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelConfigRepository extends JpaRepository<LevelConfig, Integer> {

    Optional<LevelConfig> findByLevelName(String levelName);

    @Query("SELECT lc FROM LevelConfig lc WHERE lc.minPoints <= :points ORDER BY lc.minPoints DESC")
    Page<LevelConfig> findCurrentLevelByPoints(@Param("points") Integer points, Pageable pageable);

    @Query("SELECT lc FROM LevelConfig lc WHERE lc.minPoints > :points ORDER BY lc.minPoints ASC")
    Page<LevelConfig> findNextLevelByPoints(@Param("points") Integer points, Pageable pageable);

    @Query("SELECT lc FROM LevelConfig lc ORDER BY lc.id")
    List<LevelConfig> findAllOrdered();

    Optional<LevelConfig> findFirstByIdGreaterThanOrderByIdAsc(Integer id);
}
