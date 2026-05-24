package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface BbsCommentLikeRepository extends JpaRepository<BbsCommentLike, Long> {

    Optional<BbsCommentLike> findByCommentIdAndUserId(Long commentId, Integer userId);

    boolean existsByCommentIdAndUserId(Long commentId, Integer userId);

    long countByCommentId(Long commentId);

    void deleteByCommentIdAndUserId(Long commentId, Integer userId);

    void deleteByCommentId(Long commentId);

    @Query(value = "SELECT u.person_id as userId, u.nickname as nickname, u.avatar_url as avatarUrl " +
           "FROM bbs_comment_like l JOIN user u ON l.user_id = u.person_id " +
           "WHERE l.comment_id = :commentId ORDER BY l.create_time DESC", nativeQuery = true)
    List<Map<String, Object>> findLikersByCommentId(@Param("commentId") Long commentId);
}
