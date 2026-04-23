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
}
