
package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsModerationLogRepository extends JpaRepository<BbsModerationLog, Long>{
    List<BbsModerationLog>findByPostIdOrderByCreateTimeDesc(Long postId);
    List<BbsModerationLog>findByModeratorIdOrderByCreateTimeDesc(Integer moderatorId);
}
