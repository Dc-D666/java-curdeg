package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsUserBrowseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsUserBrowseHistoryRepository extends JpaRepository<BbsUserBrowseHistory, Long> {

    Optional<BbsUserBrowseHistory> findByUserIdAndPostId(Integer userId, Long postId);

    List<BbsUserBrowseHistory> findByUserIdOrderByBrowseTimeDesc(Integer userId);

    Page<BbsUserBrowseHistory> findByUserIdOrderByBrowseTimeDesc(Integer userId, Pageable pageable);

    @Query("SELECT h.postId FROM BbsUserBrowseHistory h WHERE h.userId = :userId ORDER BY h.browseTime DESC")
    List<Long> findRecentPostIdsByUserId(@Param("userId") Integer userId);

    @Query("SELECT h.postId FROM BbsUserBrowseHistory h WHERE h.userId = :userId ORDER BY h.browseTime DESC LIMIT :limit")
    List<Long> findRecentPostIdsByUserIdWithLimit(@Param("userId") Integer userId, @Param("limit") int limit);

    long countByUserId(Integer userId);

    void deleteByUserIdAndPostId(Integer userId, Long postId);

    @Query("SELECT DISTINCT h.postId FROM BbsUserBrowseHistory h WHERE h.userId = :userId")
    List<Long> findDistinctPostIdsByUserId(@Param("userId") Integer userId);
}
