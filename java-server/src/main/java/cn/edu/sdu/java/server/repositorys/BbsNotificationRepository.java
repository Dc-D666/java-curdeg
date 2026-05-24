package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BbsNotificationRepository extends JpaRepository<BbsNotification, Long> {

    Page<BbsNotification> findByReceiverIdOrderByCreateTimeDesc(Long receiverId, Pageable pageable);

    Page<BbsNotification> findByReceiverIdAndTypeOrderByCreateTimeDesc(Long receiverId, Integer type, Pageable pageable);

    Page<BbsNotification> findByReceiverIdAndIsReadOrderByCreateTimeDesc(Long receiverId, Integer isRead, Pageable pageable);

    Page<BbsNotification> findByReceiverIdAndTypeAndIsReadOrderByCreateTimeDesc(Long receiverId, Integer type, Integer isRead, Pageable pageable);

    List<BbsNotification> findByReceiverIdAndIsReadOrderByCreateTimeDesc(Long receiverId, Integer isRead);

    List<BbsNotification> findByReceiverIdOrderByCreateTimeDesc(Long receiverId);

    List<BbsNotification> findByReceiverIdAndTypeOrderByCreateTimeDesc(Long receiverId, Integer type);

    List<BbsNotification> findByReceiverIdAndTypeAndIsReadOrderByCreateTimeDesc(Long receiverId, Integer type, Integer isRead);

    Long countByReceiverIdAndIsRead(Long receiverId, Integer isRead);

    @Modifying
    @Transactional
    @Query("UPDATE BbsNotification n SET n.isRead = 1 WHERE n.receiverId = :receiverId AND n.isRead = 0")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}
