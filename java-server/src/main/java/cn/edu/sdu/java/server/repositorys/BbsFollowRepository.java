package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsFollowRepository extends JpaRepository<BbsFollow, Long> {

    Optional<BbsFollow> findByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    boolean existsByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    List<BbsFollow> findByFollowerId(Integer followerId);

    List<BbsFollow> findByFollowingId(Integer followingId);

    Page<BbsFollow> findByFollowerId(Integer followerId, Pageable pageable);

    Page<BbsFollow> findByFollowingId(Integer followingId, Pageable pageable);

    long countByFollowerId(Integer followerId);

    long countByFollowingId(Integer followingId);

    void deleteByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_follow " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyFollowTrend(@Param("days") Integer days);

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_follow " +
           "WHERE follower_id = :userId AND create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyFollowingTrendByUser(@Param("userId") Integer userId, @Param("days") Integer days);

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_follow " +
           "WHERE following_id = :userId AND create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyFollowerTrendByUser(@Param("userId") Integer userId, @Param("days") Integer days);
}
