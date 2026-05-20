
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsBoard;
import cn.edu.sdu.java.server.models.BbsFollow;
import cn.edu.sdu.java.server.models.BbsModerationLog;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.repositorys.BbsBoardRepository;
import cn.edu.sdu.java.server.repositorys.BbsFollowRepository;
import cn.edu.sdu.java.server.repositorys.BbsModerationLogRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.DateTimeTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import java.util.Optional;

@Slf4j
@Service
public class AdminModerationService {

    private final BbsPostRepository bbsPostRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final BbsModerationLogRepository bbsModerationLogRepository;
    private final UserRepository userRepository;
    private final BbsBoardRepository bbsBoardRepository;
    private final BbsFollowRepository bbsFollowRepository;

    public AdminModerationService(BbsPostRepository bbsPostRepository,
                                  BbsNotificationRepository bbsNotificationRepository,
                                  BbsModerationLogRepository bbsModerationLogRepository,
                                  UserRepository userRepository,
                                  BbsBoardRepository bbsBoardRepository,
                                  BbsFollowRepository bbsFollowRepository) {
        this.bbsPostRepository = bbsPostRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.bbsModerationLogRepository = bbsModerationLogRepository;
        this.userRepository = userRepository;
        this.bbsBoardRepository = bbsBoardRepository;
        this.bbsFollowRepository = bbsFollowRepository;
    }

    public Page<BbsPost> getPendingPosts(Pageable pageable) {
        Page<BbsPost> posts = bbsPostRepository.findPendingModerationPosts(pageable);
        posts.getContent().forEach(this::fillPostInfo);
        return posts;
    }

    public Page<BbsPost> getAllPosts(Pageable pageable) {
        Page<BbsPost> posts = bbsPostRepository.findAll(pageable);
        posts.getContent().forEach(this::fillPostInfo);
        return posts;
    }

    private void fillPostInfo(BbsPost post) {
        if (post.getAuthorId() != null) {
            Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                post.setAuthorNickname(author.getNickname());
                String avatarUrl = author.getAvatarUrl();
                if (avatarUrl == null || avatarUrl.isBlank()) {
                    avatarUrl = "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg";
                }
                post.setAuthorAvatarUrl(avatarUrl);
            }
        }
        if (post.getBoardId() != null) {
            Optional<BbsBoard> boardOptional = bbsBoardRepository.findById(post.getBoardId());
            if (boardOptional.isPresent()) {
                post.setBoardName(boardOptional.get().getName());
            }
        }
        // 填充审核人信息
        if (post.getModeratorId() != null) {
            Optional<User> moderatorOptional = userRepository.findById(post.getModeratorId());
            if (moderatorOptional.isPresent()) {
                post.setModeratorNickname(moderatorOptional.get().getNickname());
            }
        }
    }

    @Transactional
    public void manualModerate(Long postId, String decision, String violationLevel,
                               String violationType, String remark, Integer moderatorId) {
        BbsPost post = bbsPostRepository.findById(postId).orElseThrow(() ->
                new RuntimeException("帖子不存在"));

        String oldStatus = post.getModerationStatus();
        String newStatus = decision;
        Integer oldPostStatus = post.getStatus();
        
        log.info("人工审核 - 帖子ID={}, 旧审核状态={}, 新审核状态={}, 旧帖子状态={}", 
            postId, oldStatus, newStatus, oldPostStatus);

        post.setModerationStatus(newStatus);
        post.setModerationViolationLevel(violationLevel);
        post.setModerationViolationType(violationType);
        post.setModerationRemark(remark);
        post.setModerationTime(DateTimeTool.parseDateTime(new java.util.Date()));
        post.setModeratorId(moderatorId);

        // 处理状态变化和用户发帖数
        if ("reject".equals(newStatus)) {
            post.setStatus(0);
            log.info("人工审核：帖子设为不可见（status=0）");
            
            // 如果之前是可见的，需要减少用户发帖数
            if (oldPostStatus != null && oldPostStatus == 1) {
                updateUserPostCount(post.getAuthorId().intValue(), -1);
            }
        } else if ("pass".equals(newStatus)) {
            post.setStatus(1);
            log.info("人工审核：帖子设为可见（status=1）");
            
            // 如果之前不是可见的，需要增加用户发帖数
            if (oldPostStatus == null || oldPostStatus != 1) {
                updateUserPostCount(post.getAuthorId().intValue(), 1);
                // 发送关注者通知
                sendFollowerNotifications(post);
            }
        }

        bbsPostRepository.save(post);

        saveModerationLog(post, oldStatus, newStatus, violationLevel, violationType, remark, moderatorId);
        sendUserNotification(post, decision, remark);
    }
    
    /**
     * 更新用户发帖数
     * @param userId 用户ID
     * @param delta 变化量（+1 或 -1）
     */
    private void updateUserPostCount(Integer userId, int delta) {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                int newCount = Math.max(0, (user.getPostCount() != null ? user.getPostCount() : 0) + delta);
                user.setPostCount(newCount);
                userRepository.save(user);
                log.info("用户 {} 的发帖数更新为 {}", userId, newCount);
            }
        } catch (Exception e) {
            log.error("更新用户发帖数失败", e);
        }
    }
    
    /**
     * 向用户的关注者发送新帖子通知
     */
    private void sendFollowerNotifications(BbsPost post) {
        try {
            java.util.List<BbsFollow> followers = bbsFollowRepository.findByFollowingId(post.getAuthorId().intValue());
            if (!followers.isEmpty()) {
                Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                String authorNickname = authorOptional.map(User::getNickname).orElse("用户");
                
                java.util.List<BbsNotification> notifications = new java.util.ArrayList<>();
                String notificationContent = String.format("用户【%s】发布帖子「%s」", authorNickname, post.getTitle());
                
                for (BbsFollow follow : followers) {
                    BbsNotification notification = new BbsNotification();
                    notification.setReceiverId(follow.getFollowerId().longValue());
                    notification.setType(6);
                    notification.setTitle("关注用户发帖通知");
                    notification.setContent(notificationContent);
                    notifications.add(notification);
                }
                
                bbsNotificationRepository.saveAll(notifications);
                log.info("已向 {} 位关注者发送通知", followers.size());
            }
        } catch (Exception e) {
            log.error("发送关注者通知失败", e);
        }
    }

    private void saveModerationLog(BbsPost post, String oldStatus, String newStatus,
                                   String violationLevel, String violationType, String remark,
                                   Integer moderatorId) {
        BbsModerationLog log = new BbsModerationLog();
        log.setPostId(post.getId());
        log.setModeratorId(moderatorId);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setViolationLevel(violationLevel);
        log.setViolationType(violationType);
        log.setRemark(remark);
        bbsModerationLogRepository.save(log);
    }

    private void sendUserNotification(BbsPost post, String decision, String remark) {
        String message;
        switch (decision) {
            case "pass":
                message = String.format("您的帖子「%s」已通过人工审核", post.getTitle());
                break;
            case "reject":
                message = String.format("您的帖子「%s」未通过人工审核：%s", post.getTitle(),
                        remark != null ? remark : "内容违规");
                break;
            default:
                return;
        }

        BbsNotification notification = new BbsNotification();
        notification.setReceiverId(post.getAuthorId().longValue());
        notification.setTitle("帖子审核通知");
        notification.setContent(message);
        notification.setType(7);
        bbsNotificationRepository.save(notification);
    }
}
