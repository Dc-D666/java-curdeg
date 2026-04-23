package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsFollowRepository extends JpaRepository<BbsFollow, Long> {

    Optional<BbsFollow> findByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    boolean existsByFollowerIdAndFollowingId(Integer followerId, Integer followingId);

    List<BbsFollow> findByFollowerId(Integer followerId);

    List<BbsFollow> findByFollowingId(Integer followingId);

    Page<BbsFollow> findByFollowerId(Integer followerId, Pageable pageable);

    Page<BbsFollow> findByFollowingId(Integer followingId, Pageable pageable);

    long countByFollowerId(Integer followerId);

    long countByFollowingId(Integer followingId);

    void deleteByFollowerIdAndFollowingId(Integer followerId, Integer followingId);
}
