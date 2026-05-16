package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsCommentRepository extends JpaRepository<BbsComment, Long> {

    List<BbsComment> findByPostIdAndParentIdIsNullAndStatusOrderByCreateTimeAsc(Long postId, Integer status);

    List<BbsComment> findByParentIdAndStatusOrderByCreateTimeAsc(Long parentId, Integer status);

    List<BbsComment> findByAuthorIdAndStatusOrderByCreateTimeDesc(Long authorId, Integer status);

    List<BbsComment> findByPostId(Long postId);

    List<BbsComment> findByParentId(Long parentId);

    void deleteByPostId(Long postId);

    void deleteByParentId(Long parentId);

    @Query("SELECT SUM(c.likeCount) FROM BbsComment c WHERE c.authorId = :authorId")
    Integer sumLikeCountByAuthorId(@Param("authorId") Long authorId);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_comment " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyCommentTrend(@Param("days") Integer days);

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_comment " +
           "WHERE author_id = :authorId AND create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyCommentTrendByAuthor(@Param("authorId") Long authorId, @Param("days") Integer days);

    List<BbsComment> findTop10ByStatusOrderByLikeCountDesc(Integer status);

    List<BbsComment> findTop5ByPostIdAndStatusOrderByLikeCountDesc(Long postId, Integer status);

    @Query(value = "SELECT * FROM bbs_comment WHERE post_id = :postId AND status = :status ORDER BY like_count DESC LIMIT :n", nativeQuery = true)
    List<BbsComment> findTopNByPostIdAndStatusOrderByLikeCountDesc(@Param("postId") Long postId, @Param("status") Integer status, @Param("n") int n);

    // 使用JPA内置方法，更可靠
    @Override
    long count();

    @Query(value = "SELECT COUNT(*) FROM bbs_comment", nativeQuery = true)
    Long countTotalComments();

    @Query(value = "SELECT COUNT(*) FROM bbs_comment WHERE DATE(create_time) = CURDATE()", nativeQuery = true)
    Long countTodayComments();
}
