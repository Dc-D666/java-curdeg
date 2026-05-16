package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsMessageRepository extends JpaRepository<BbsMessage, Long> {

    List<BbsMessage> findByConversationIdOrderByCreateTimeAsc(Long conversationId);

    Page<BbsMessage> findByConversationIdOrderByCreateTimeDesc(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM BbsMessage m WHERE m.conversationId = :conversationId AND m.receiverId = :userId AND m.isRead = false")
    long countUnreadByConversationIdAndUserId(@Param("conversationId") Long conversationId, @Param("userId") Integer userId);

    @Query("UPDATE BbsMessage m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.receiverId = :userId AND m.isRead = false")
    @Modifying
    void markAllAsRead(@Param("conversationId") Long conversationId, @Param("userId") Integer userId);

    @Query("SELECT COUNT(m) FROM BbsMessage m WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.conversationId IN (SELECT c.id FROM BbsConversation c WHERE (c.user1Id = :senderId AND c.user2Id = :receiverId) OR (c.user1Id = :receiverId AND c.user2Id = :senderId))")
    long countMessagesBetweenUsers(@Param("senderId") Integer senderId, @Param("receiverId") Integer receiverId);
}
