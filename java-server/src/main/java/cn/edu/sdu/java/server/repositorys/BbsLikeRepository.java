package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BbsLikeRepository extends JpaRepository<BbsLike, Long> {
    
    Optional<BbsLike> findByPostIdAndUserId(Long postId, Integer userId);
    
    boolean existsByPostIdAndUserId(Long postId, Integer userId);
    
    long countByPostId(Long postId);
    
    void deleteByPostIdAndUserId(Long postId, Integer userId);
    
    void deleteByPostId(Long postId);
}
