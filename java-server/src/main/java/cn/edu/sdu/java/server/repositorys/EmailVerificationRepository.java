package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndVerificationType(String email, String verificationType);

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.verificationType = :type AND ev.isVerified = false AND ev.expireTime > :now ORDER BY ev.createTime DESC")
    Optional<EmailVerification> findValidVerification(@Param("email") String email,
                                                       @Param("type") String type,
                                                       @Param("now") Date now);

    @Query("SELECT COUNT(ev) FROM EmailVerification ev WHERE ev.email = :email AND ev.verificationType = :type AND DATE(ev.createTime) = CURDATE()")
    Long countTodaySend(@Param("email") String email, @Param("type") String type);
}
