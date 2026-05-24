package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUserIdOrderByCreateTimeDesc(Integer userId);
    List<Feedback> findAllByOrderByCreateTimeDesc();
}
