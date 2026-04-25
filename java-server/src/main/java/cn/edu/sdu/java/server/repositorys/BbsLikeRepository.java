package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsLikeRepository extends JpaRepository<BbsLike, Long> {
    
    Optional<BbsLike> findByPostIdAndUserId(Long postId, Integer userId);
    
    boolean existsByPostIdAndUserId(Long postId, Integer userId);
    
    long countByPostId(Long postId);
    
    void deleteByPostIdAndUserId(Long postId, Integer userId);
    
    void deleteByPostId(Long postId);

    @Query("SELECT COUNT(l) FROM BbsLike l WHERE l.postId IN (SELECT p.id FROM BbsPost p WHERE p.authorId = :authorId)")
    long countLikesByAuthorId(@Param("authorId") Integer authorId);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_like " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyLikeTrend(@Param("days") Integer days);
}
