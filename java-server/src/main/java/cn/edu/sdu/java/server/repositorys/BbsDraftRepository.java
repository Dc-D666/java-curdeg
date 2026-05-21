package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsDraftRepository extends JpaRepository<BbsDraft, Long> {
    List<BbsDraft> findByUserIdOrderByUpdateTimeDesc(Integer userId);
    Optional<BbsDraft> findByIdAndUserId(Long id, Integer userId);
    void deleteByIdAndUserId(Long id, Integer userId);
}
