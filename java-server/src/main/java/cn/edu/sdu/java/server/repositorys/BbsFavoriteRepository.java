package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsFavoriteRepository extends JpaRepository<BbsFavorite, Long> {
    
    Optional<BbsFavorite> findByPostIdAndUserId(Long postId, Integer userId);
    
    boolean existsByPostIdAndUserId(Long postId, Integer userId);
    
    long countByPostId(Long postId);
    
    void deleteByPostIdAndUserId(Long postId, Integer userId);
    
    void deleteByPostId(Long postId);

    Page<BbsFavorite> findByUserIdOrderByCreateTimeDesc(Integer userId, Pageable pageable);

    long countByUserId(Integer userId);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_favorite " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyFavoriteTrend(@Param("days") Integer days);

    @Query(value = "SELECT DATE(f.create_time) as date, COUNT(*) as count FROM bbs_favorite f " +
           "JOIN bbs_post p ON f.post_id = p.id " +
           "WHERE p.author_id = :authorId AND f.create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(f.create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyReceivedFavoriteTrendByAuthor(@Param("authorId") Long authorId, @Param("days") Integer days);
}
