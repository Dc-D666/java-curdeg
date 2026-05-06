
package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsPostRepository extends JpaRepository<BbsPost, Long> {

    @Query("SELECT p FROM BbsPost p WHERE p.status = 1 " +
           "AND (:boardId IS NULL OR p.boardId = :boardId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) " +
           "ORDER BY p.isTop DESC, p.createTime DESC")
    Page<BbsPost> findPostsByCondition(@Param("boardId") Long boardId,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @Query("SELECT p FROM BbsPost p WHERE " +
           "((p.moderationStatus = 'pass' AND p.status = 1) OR " +
           "(p.authorId = :currentUserId OR :isAdmin = true)) " +
           "AND (:boardId IS NULL OR p.boardId = :boardId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) " +
           "ORDER BY p.isTop DESC, p.createTime DESC")
    Page<BbsPost> findPostsByConditionWithModeration(@Param("boardId") Long boardId,
                                                       @Param("keyword") String keyword,
                                                       @Param("currentUserId") Long currentUserId,
                                                       @Param("isAdmin") Boolean isAdmin,
                                                       Pageable pageable);

    Page<BbsPost> findByAuthorIdAndStatusOrderByCreateTimeDesc(Long authorId, Integer status, Pageable pageable);

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_post " +
                   "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                   "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyPostLast7Days();

    List<BbsPost> findTop10ByStatusOrderByLikeCountDescCommentCountDesc(Integer status);

    @Query("SELECT SUM(p.viewCount) FROM BbsPost p WHERE p.authorId = :authorId")
    Integer sumViewCountByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT SUM(p.likeCount) FROM BbsPost p WHERE p.authorId = :authorId")
    Integer sumLikeCountByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT p FROM BbsPost p WHERE p.moderationStatus = 'manual' ORDER BY p.createTime DESC")
    Page<BbsPost> findPendingModerationPosts(Pageable pageable);

    @Query("SELECT p FROM BbsPost p WHERE " +
           "((p.moderationStatus = 'pass' AND p.status = 1) OR " +
           "(p.authorId = :currentUserId OR :isAdmin = true)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) " +
           "ORDER BY p.createTime DESC")
    Page<BbsPost> searchPostsWithModeration(@Param("keyword") String keyword,
                                               @Param("currentUserId") Long currentUserId,
                                               @Param("isAdmin") Boolean isAdmin,
                                               Pageable pageable);

    @Query(value = "SELECT * FROM bbs_post p WHERE " +
           "((p.moderation_status = 'pass' AND p.status = 1) OR " +
           "(p.author_id = :currentUserId OR :isAdmin = true)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "  (:searchType = 'fulltext' AND (p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))) OR " +
           "  (:searchType != 'fulltext' AND p.title LIKE CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.create_time DESC",
           countQuery = "SELECT COUNT(*) FROM bbs_post p WHERE " +
           "((p.moderation_status = 'pass' AND p.status = 1) OR " +
           "(p.author_id = :currentUserId OR :isAdmin = true)) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "  (:searchType = 'fulltext' AND (p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%'))) OR " +
           "  (:searchType != 'fulltext' AND p.title LIKE CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.create_time DESC",
           nativeQuery = true)
    Page<BbsPost> searchPostsWithModerationByType(@Param("keyword") String keyword,
                                                     @Param("searchType") String searchType,
                                                     @Param("currentUserId") Long currentUserId,
                                                     @Param("isAdmin") Boolean isAdmin,
                                                     Pageable pageable);

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM bbs_post " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyPostTrend(@Param("days") Integer days);

    List<BbsPost> findTop20ByStatusOrderByLikeCountDesc(Integer status);
    List<BbsPost> findTop20ByStatusOrderByCommentCountDesc(Integer status);
    List<BbsPost> findTop20ByStatusOrderByViewCountDesc(Integer status);
    List<BbsPost> findTop20ByStatusOrderByFavoriteCountDesc(Integer status);

    @Query(value = "SELECT status, COUNT(*) as count FROM bbs_post GROUP BY status", nativeQuery = true)
    List<Object[]> countPostsByStatus();

    @Query(value = "SELECT CASE WHEN image_urls IS NOT NULL AND image_urls != '' THEN 'with_image' ELSE 'without_image' END as type, COUNT(*) as count FROM bbs_post GROUP BY type", nativeQuery = true)
    List<Object[]> countPostsByImageStatus();

    @Query(value = "SELECT board_id, COUNT(*) as count FROM bbs_post GROUP BY board_id ORDER BY count DESC", nativeQuery = true)
    List<Object[]> countPostsByBoard();

    @Query(value = "SELECT COUNT(*) FROM bbs_post", nativeQuery = true)
    Long countTotalPosts();

    @Query(value = "SELECT COUNT(*) FROM bbs_post WHERE DATE(create_time) = CURDATE()", nativeQuery = true)
    Long countTodayPosts();

    @Query(value = "SELECT COUNT(*) FROM bbs_post WHERE moderation_status = 'pending'", nativeQuery = true)
    Long countPendingModerationPosts();

    @Query("SELECT p FROM BbsPost p WHERE " +
           "p.authorId = :authorId AND " +
           "((p.moderationStatus = 'pass' AND p.status = 1) OR " +
           "(p.authorId = :currentUserId OR :isAdmin = true)) " +
           "ORDER BY p.createTime DESC")
    Page<BbsPost> findUserPostsWithModeration(@Param("authorId") Long authorId,
                                                @Param("currentUserId") Long currentUserId,
                                                @Param("isAdmin") Boolean isAdmin,
                                                Pageable pageable);
}
