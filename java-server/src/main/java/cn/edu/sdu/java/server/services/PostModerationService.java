
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsFollow;
import cn.edu.sdu.java.server.models.BbsModerationLog;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import cn.edu.sdu.java.server.repositorys.BbsFollowRepository;
import cn.edu.sdu.java.server.payload.response.ModerationResult;
import cn.edu.sdu.java.server.repositorys.BbsModerationLogRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.DateTimeTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PostModerationService {

    private final BbsPostRepository bbsPostRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final BbsModerationLogRepository bbsModerationLogRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;
    private final ObjectMapper objectMapper;
    private final BbsFollowRepository bbsFollowRepository;

    public PostModerationService(BbsPostRepository bbsPostRepository,
                                  BbsNotificationRepository bbsNotificationRepository,
                                  BbsModerationLogRepository bbsModerationLogRepository,
                                  UserRepository userRepository,
                                  ContentModerationService contentModerationService,
                                  ObjectMapper objectMapper,
                                  BbsFollowRepository bbsFollowRepository) {
        this.bbsPostRepository = bbsPostRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.bbsModerationLogRepository = bbsModerationLogRepository;
        this.userRepository = userRepository;
        this.contentModerationService = contentModerationService;
        this.objectMapper = objectMapper;
        this.bbsFollowRepository = bbsFollowRepository;
    }

    @Async
    @Transactional
    public void moderatePostAsync(Long postId) {
        log.info("===== 开始异步审核帖子 =====");
        log.info("帖子ID：{}", postId);
        try {
            BbsPost post = bbsPostRepository.findById(postId).orElse(null);
            if (post == null) {
                log.warn("帖子不存在，ID：{}", postId);
                return;
            }
            log.info("找到帖子 - 标题：{}，作者ID：{}，当前状态：{}", 
                post.getTitle(), 
                post.getAuthorId(),
                post.getModerationStatus());

            log.info("调用AI内容审核...");
            ModerationResult result = contentModerationService.moderateContent(post.getTitle(), post.getContent());
            
            log.info("AI审核结果 - auditResult={}, violationLevel={}, violationType={}", 
                result.getAuditResult(),
                result.getViolationLevel(),
                result.getViolationType());

            updatePostWithModerationResult(post, result);

            log.info("===== 帖子审核完成 =====");
            log.info("最终状态：{} -> {}", 
                post.getModerationStatus(), 
                result.getAuditResult());

        } catch (Exception e) {
            log.error("===== 审核帖子时发生异常 =====", e);
            log.error("错误堆栈：", e);
            try {
                BbsPost post = bbsPostRepository.findById(postId).orElse(null);
                if (post != null) {
                    log.warn("使用降级处理 - 转为人工审核");
                    updatePostWithModerationResult(post, ModerationResult.manual());
                }
            } catch (Exception ex) {
                log.error("降级处理也失败了", ex);
            }
        }
    }

    @Transactional
    public void updatePostWithModerationResult(BbsPost post, ModerationResult result) {
        log.info("===== 更新帖子审核状态 =====");
        String oldStatus = post.getModerationStatus();
        String newStatus = result.getAuditResult();
        Integer oldPostStatus = post.getStatus();
        
        log.info("旧审核状态={}, 新审核状态={}, 旧帖子状态={}", oldStatus, newStatus, oldPostStatus);

        post.setModerationStatus(newStatus);
        post.setModerationViolationLevel(result.getViolationLevel());
        post.setModerationViolationType(result.getViolationType());
        post.setModerationSuggestion(result.getSuggestion());
        post.setModerationConfidence(result.getConfidence());
        post.setModerationRemark(result.getRemark());
        post.setModerationTime(DateTimeTool.parseDateTime(new java.util.Date()));

        log.info("设置审核详细信息 - 违规级别={}, 违规类型={}, 置信度={}", 
            result.getViolationLevel(),
            result.getViolationType(),
            result.getConfidence());

        if (result.getViolationFragments() != null && !result.getViolationFragments().isEmpty()) {
            try {
                post.setModerationViolationFragments(objectMapper.writeValueAsString(result.getViolationFragments()));
                log.info("违规片段：{}", result.getViolationFragments());
            } catch (Exception e) {
                log.error("序列化违规片段失败", e);
            }
        }

        // 处理状态变化和用户发帖数
        if ("reject".equals(newStatus)) {
            post.setStatus(0);
            log.info("帖子设为不可见（status=0）");
            
            // 如果之前是可见的，需要减少用户发帖数
            if (oldPostStatus != null && oldPostStatus == 1) {
                updateUserPostCount(post.getAuthorId().intValue(), -1);
            }
        } else if ("pass".equals(newStatus)) {
            post.setStatus(1);
            log.info("帖子设为可见（status=1）");
            
            // 如果之前不是可见的，需要增加用户发帖数
            if (oldPostStatus == null || oldPostStatus != 1) {
                updateUserPostCount(post.getAuthorId().intValue(), 1);
                // 发送关注者通知
                sendFollowerNotifications(post);
            }
        } else if ("manual".equals(newStatus)) {
            log.info("保持帖子不可见，等待人工审核（status保持0）");
            // 如果之前是可见的，需要减少用户发帖数
            if (oldPostStatus != null && oldPostStatus == 1) {
                updateUserPostCount(post.getAuthorId().intValue(), -1);
            }
        }

        log.info("保存帖子到数据库...");
        bbsPostRepository.save(post);

        log.info("保存审核日志...");
        saveModerationLog(post, oldStatus, result);
        
        log.info("发送通知...");
        sendNotifications(post, result);
        log.info("===== 审核状态更新完成 =====");
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
            List<BbsFollow> followers = bbsFollowRepository.findByFollowingId(post.getAuthorId().intValue());
            if (!followers.isEmpty()) {
                Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                String authorNickname = authorOptional.map(User::getNickname).orElse("用户");
                
                List<BbsNotification> notifications = new ArrayList<>();
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

    private void saveModerationLog(BbsPost post, String oldStatus, ModerationResult result) {
        BbsModerationLog log = new BbsModerationLog();
        log.setPostId(post.getId());
        log.setOldStatus(oldStatus);
        log.setNewStatus(result.getAuditResult());
        log.setViolationLevel(result.getViolationLevel());
        log.setViolationType(result.getViolationType());
        log.setRemark(result.getRemark());
        bbsModerationLogRepository.save(log);
    }

    private void sendNotifications(BbsPost post, ModerationResult result) {
        sendUserNotification(post, result);

        if ("manual".equals(result.getAuditResult())) {
            sendAdminNotification(post);
        }
    }

    private void sendUserNotification(BbsPost post, ModerationResult result) {
        String message;
        switch (result.getAuditResult()) {
            case "pass":
                message = String.format("您的帖子「%s」审核通过", post.getTitle());
                break;
            case "reject":
                message = String.format("您的帖子「%s」审核未通过：%s", post.getTitle(),
                        result.getSuggestion() != null ? result.getSuggestion() : "内容违规");
                break;
            case "manual":
            default:
                message = String.format("您的帖子「%s」正在人工审核中", post.getTitle());
                break;
        }

        BbsNotification notification = new BbsNotification();
        notification.setReceiverId(post.getAuthorId().longValue());
        notification.setTitle("帖子审核通知");
        notification.setContent(message);
        notification.setType(7);
        bbsNotificationRepository.save(notification);
    }

    private void sendAdminNotification(BbsPost post) {
        try {
            List<User> admins = userRepository.findAdmins();
            String message = String.format("有新的帖子需要审核：「%s」", post.getTitle());

            for (User admin : admins) {
                BbsNotification notification = new BbsNotification();
                notification.setReceiverId(admin.getPersonId().longValue());
                notification.setTitle("待审核帖子通知");
                notification.setContent(message);
                notification.setType(8);
                bbsNotificationRepository.save(notification);
            }
        } catch (Exception e) {
            log.error("发送管理员通知失败", e);
        }
    }
}
