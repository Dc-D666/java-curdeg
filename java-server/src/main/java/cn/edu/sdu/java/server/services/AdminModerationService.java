
package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsBoard;
import cn.edu.sdu.java.server.models.BbsModerationLog;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.repositorys.BbsBoardRepository;
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

    public AdminModerationService(BbsPostRepository bbsPostRepository,
                                  BbsNotificationRepository bbsNotificationRepository,
                                  BbsModerationLogRepository bbsModerationLogRepository,
                                  UserRepository userRepository,
                                  BbsBoardRepository bbsBoardRepository) {
        this.bbsPostRepository = bbsPostRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.bbsModerationLogRepository = bbsModerationLogRepository;
        this.userRepository = userRepository;
        this.bbsBoardRepository = bbsBoardRepository;
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

        post.setModerationStatus(newStatus);
        post.setModerationViolationLevel(violationLevel);
        post.setModerationViolationType(violationType);
        post.setModerationRemark(remark);
        post.setModerationTime(DateTimeTool.parseDateTime(new java.util.Date()));
        post.setModeratorId(moderatorId);

        if ("reject".equals(newStatus)) {
            post.setStatus(0);
        } else if ("pass".equals(newStatus)) {
            post.setStatus(1);
        }

        bbsPostRepository.save(post);

        saveModerationLog(post, oldStatus, newStatus, violationLevel, violationType, remark, moderatorId);
        sendUserNotification(post, decision, remark);
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
