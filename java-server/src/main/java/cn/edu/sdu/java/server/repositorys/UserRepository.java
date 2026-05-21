
package cn.edu.sdu.java.server.repositorys;


import cn.edu.sdu.java.server.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByPersonNum(String perNum);
    Optional<User> findByPersonPersonId(Integer personId);


    Boolean existsByUserName(String userName);
    @Query(value="select count(*) from User where lastLoginTime >?1")
    Integer countLastLoginTime(String date);
    @Query(value = "select userType.id, count(personId) from User group by userType.id" )
    List<?> getCountList();

    // ==================== 社区业务新增查询方法 ====================

    Optional<User> findByStudentId(String studentId);

    Boolean existsByStudentId(String studentId);

    @Query("SELECT u FROM User u WHERE (:keyword IS NULL OR :keyword = '' OR u.nickname LIKE %:keyword% OR u.studentId LIKE %:keyword%)")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    List<User> findTop10ByIsBannedOrderByPostCountDescCommentCountDesc(Boolean isBanned);

    @Query("SELECT u FROM User u WHERE u.userType.id = 1 OR u.userType.id = 2")
    List<User> findAdmins();

    // ==================== 统计功能扩展 ====================

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count FROM user " +
           "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
           "GROUP BY DATE(create_time) ORDER BY date", nativeQuery = true)
    List<Object[]> countDailyUserGrowth(@Param("days") Integer days);

    List<User> findTop20ByIsBannedOrderByPostCountDesc(Boolean isBanned);
    List<User> findTop20ByIsBannedOrderByCommentCountDesc(Boolean isBanned);

    @Query(value = "SELECT " +
           "CASE WHEN post_count = 0 THEN '0' " +
           "WHEN post_count BETWEEN 1 AND 5 THEN '1-5' " +
           "WHEN post_count BETWEEN 6 AND 20 THEN '6-20' " +
           "WHEN post_count BETWEEN 21 AND 50 THEN '21-50' " +
           "ELSE '50+' END as activity_range, " +
           "COUNT(*) as count FROM user GROUP BY activity_range ORDER BY activity_range", nativeQuery = true)
    List<Object[]> countUserActivityDistribution();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isBanned = true")
    Long countBannedUsers();

    List<User> findByIsBanned(Boolean isBanned);

    // 使用JPA内置方法，更可靠
    @Override
    long count();

    // 自定义SQL查询
    @Query(value = "SELECT COUNT(*) FROM sys_user", nativeQuery = true)
    Long countTotalUsers();

    @Query(value = "SELECT COUNT(*) FROM sys_user WHERE DATE(create_time) = CURDATE()", nativeQuery = true)
    Long countTodayNewUsers();

    // 简化的活跃用户查询，避免复杂的LEFT JOIN
    @Query(value = "SELECT COUNT(DISTINCT u.person_id) FROM sys_user u " +
           "WHERE u.person_id IN (SELECT p.author_id FROM bbs_post p WHERE p.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) " +
           "OR u.person_id IN (SELECT c.author_id FROM bbs_comment c WHERE c.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) " +
           "OR u.person_id IN (SELECT l.user_id FROM bbs_like l WHERE l.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)) " +
           "OR u.person_id IN (SELECT f.user_id FROM bbs_favorite f WHERE f.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY))", nativeQuery = true)
    Long countMonthlyActiveUsers();

    // ==================== 积分/等级系统查询方法 ====================

    List<User> findAllByOrderByPointsDesc(Pageable pageable);

    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.points > :points")
    Integer findUserRankByPoints(@Param("points") Integer points);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.points = :points WHERE u.personId = :userId")
    int updatePointsOnly(@Param("userId") Integer userId, @Param("points") Integer points);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET level = :level WHERE person_id = :userId", nativeQuery = true)
    void updateLevel(@Param("userId") Integer userId, @Param("level") Integer level);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET post_count = post_count + :delta WHERE person_id = :userId", nativeQuery = true)
    void updatePostCount(@Param("userId") Integer userId, @Param("delta") Integer delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET following_count = following_count + :delta WHERE person_id = :userId", nativeQuery = true)
    void updateFollowingCount(@Param("userId") Integer userId, @Param("delta") Integer delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET follower_count = follower_count + :delta WHERE person_id = :userId", nativeQuery = true)
    void updateFollowerCount(@Param("userId") Integer userId, @Param("delta") Integer delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET comment_count = comment_count + :delta WHERE person_id = :userId", nativeQuery = true)
    void updateCommentCount(@Param("userId") Integer userId, @Param("delta") Integer delta);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET level_protected_until = :protectedUntil WHERE person_id = :userId", nativeQuery = true)
    void updateLevelProtectedUntil(@Param("userId") Integer userId, @Param("protectedUntil") java.time.LocalDate protectedUntil);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET last_login_time = :lastLoginTime, login_count = :loginCount, consecutive_login_days = :consecutiveDays, last_login_date = :lastLoginDate WHERE person_id = :userId", nativeQuery = true)
    void updateLoginInfo(@Param("userId") Integer userId, @Param("lastLoginTime") String lastLoginTime, @Param("loginCount") Integer loginCount, @Param("consecutiveDays") Integer consecutiveDays, @Param("lastLoginDate") String lastLoginDate);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET nickname = :nickname, avatar_url = :avatarUrl, signature = :signature WHERE person_id = :userId", nativeQuery = true)
    void updateProfile(@Param("userId") Integer userId, @Param("nickname") String nickname, @Param("avatarUrl") String avatarUrl, @Param("signature") String signature);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET password = :password WHERE person_id = :userId", nativeQuery = true)
    void updatePassword(@Param("userId") Integer userId, @Param("password") String password);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET profile_completed_reward = :reward WHERE person_id = :userId", nativeQuery = true)
    void updateProfileCompletedReward(@Param("userId") Integer userId, @Param("reward") Integer reward);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET user_name = :userName WHERE person_id = :userId", nativeQuery = true)
    void updateUserName(@Param("userId") Integer userId, @Param("userName") String userName);

    @Query("SELECT u FROM User u WHERE u.person.email = :email")
    Optional<User> findByPersonEmail(@Param("email") String email);
}
