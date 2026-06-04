package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.BbsBanAppeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BbsBanAppealRepository extends JpaRepository<BbsBanAppeal, Long> {
    
    Page<BbsBanAppeal> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    
    Page<BbsBanAppeal> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    
    List<BbsBanAppeal> findByStatusOrderByCreateTimeDesc(Integer status);
    
    Page<BbsBanAppeal> findAllByOrderByCreateTimeDesc(Pageable pageable);
    
    // 查询非admin用户的申诉 (userType.id != 1 and != 2)
    @Query("SELECT a FROM BbsBanAppeal a WHERE EXISTS (" +
           "SELECT 1 FROM User u WHERE u.personId = a.userId AND " +
           "u.userType.id != 1 AND u.userType.id != 2) ORDER BY a.createTime DESC")
    Page<BbsBanAppeal> findByNonAdminUserOrderByCreateTimeDesc(Pageable pageable);
    
    @Query("SELECT a FROM BbsBanAppeal a WHERE a.status = :status AND EXISTS (" +
           "SELECT 1 FROM User u WHERE u.personId = a.userId AND " +
           "u.userType.id != 1 AND u.userType.id != 2) ORDER BY a.createTime DESC")
    Page<BbsBanAppeal> findByStatusAndNonAdminUserOrderByCreateTimeDesc(@Param("status") Integer status, Pageable pageable);
}
