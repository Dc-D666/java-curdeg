package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BbsConversationRepository extends JpaRepository<BbsConversation, Long> {

    @Query("SELECT c FROM BbsConversation c WHERE (c.user1Id = :userId OR c.user2Id = :userId) ORDER BY c.lastMessageTime DESC")
    List<BbsConversation> findByUserIdOrderByLastMessageTimeDesc(@Param("userId") Integer userId);

    @Query("SELECT c FROM BbsConversation c WHERE (c.user1Id = :userId1 AND c.user2Id = :userId2) OR (c.user1Id = :userId2 AND c.user2Id = :userId1)")
    Optional<BbsConversation> findByUserPair(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    @Query("SELECT COUNT(c) FROM BbsConversation c WHERE (c.user1Id = :userId OR c.user2Id = :userId) AND (c.user1UnreadCount > 0 OR c.user2UnreadCount > 0)")
    long countUnreadConversations(@Param("userId") Integer userId);
}
