package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BbsCommentLikeRepository extends JpaRepository<BbsCommentLike, Long> {
    
    Optional<BbsCommentLike> findByCommentIdAndUserId(Long commentId, Integer userId);
    
    boolean existsByCommentIdAndUserId(Long commentId, Integer userId);
    
    long countByCommentId(Long commentId);
    
    void deleteByCommentIdAndUserId(Long commentId, Integer userId);
    
    void deleteByCommentId(Long commentId);
}
