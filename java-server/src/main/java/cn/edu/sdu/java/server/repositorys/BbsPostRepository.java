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
}
