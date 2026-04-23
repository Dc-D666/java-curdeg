package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsNotificationRepository extends JpaRepository<BbsNotification, Long> {

    Page<BbsNotification> findByReceiverIdOrderByCreateTimeDesc(Long receiverId, Pageable pageable);

    List<BbsNotification> findByReceiverIdAndIsReadOrderByCreateTimeDesc(Long receiverId, Integer isRead);

    List<BbsNotification> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);

    Long countByReceiverIdAndIsRead(Long receiverId, Integer isRead);
}
