package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
