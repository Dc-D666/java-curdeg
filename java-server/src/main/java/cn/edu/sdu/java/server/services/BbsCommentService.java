package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.BbsCommentLike;
import cn.edu.sdu.java.server.models.BbsModerationLog;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.ModerationResult;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
import cn.edu.sdu.java.server.repositorys.BbsCommentLikeRepository;
import cn.edu.sdu.java.server.repositorys.BbsModerationLogRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import cn.edu.sdu.java.server.util.SensitiveWordFilter;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BbsCommentService {

    private final BbsCommentRepository bbsCommentRepository;
    private final BbsPostRepository bbsPostRepository;
    private final UserRepository userRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final BbsCommentLikeRepository bbsCommentLikeRepository;
    private final BbsFileService bbsFileService;
    private final PointService pointService;
    private final LevelPrivilegeService levelPrivilegeService;
    private final ContentModerationService contentModerationService;
    private final BbsModerationLogRepository bbsModerationLogRepository;

    public BbsCommentService(BbsCommentRepository bbsCommentRepository, BbsPostRepository bbsPostRepository,
                          UserRepository userRepository, BbsNotificationRepository bbsNotificationRepository,
                          SensitiveWordFilter sensitiveWordFilter, BbsCommentLikeRepository bbsCommentLikeRepository,
                          BbsFileService bbsFileService, PointService pointService, LevelPrivilegeService levelPrivilegeService,
                          ContentModerationService contentModerationService, BbsModerationLogRepository bbsModerationLogRepository) {
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.bbsCommentLikeRepository = bbsCommentLikeRepository;
        this.bbsFileService = bbsFileService;
        this.pointService = pointService;
        this.levelPrivilegeService = levelPrivilegeService;
        this.contentModerationService = contentModerationService;
        this.bbsModerationLogRepository = bbsModerationLogRepository;
    }
    
    private void createNotification(Long receiverId, Integer type, String title, String content) {
        BbsNotification notification = new BbsNotification();
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);
        bbsNotificationRepository.saveAndFlush(notification);
    }

    private void fillCommentAuthorInfo(BbsComment comment) {
        if (comment.getAuthorId() != null) {
            Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                comment.setAuthorNickname(author.getNickname());
                int authorLevel = author.getLevel() != null ? author.getLevel() : 0;
                comment.setAuthorNicknameStyle(levelPrivilegeService.getNicknameStyle(authorLevel));
                String avatarUrl = author.getAvatarUrl();
                if (avatarUrl == null || avatarUrl.isBlank()) {
                    avatarUrl = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";
                }
                comment.setAuthorAvatarUrl(avatarUrl);
            }
        }
    }

    public DataResponse getCommentsByPost(Long postId) {
        Optional<BbsPost> postOptional = bbsPostRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        if (post.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("帖子已下架");
        }

        Integer currentUserId = CommonMethod.getPersonId();
        List<BbsComment> commentList = bbsCommentRepository.findByPostIdAndParentIdIsNullAndStatusOrderByCreateTimeAsc(postId, 1);

        List<BbsComment> filteredList = new ArrayList<>();
        for (BbsComment comment : commentList) {
            if ("reject".equals(comment.getModerationStatus()) && 
                (currentUserId == null || !currentUserId.equals(comment.getAuthorId().intValue()))) {
                continue;
            }
            fillCommentAuthorInfo(comment);
            List<BbsComment> replyList = bbsCommentRepository.findByParentIdAndStatusOrderByCreateTimeAsc(comment.getId(), 1);
            List<BbsComment> filteredReplies = new ArrayList<>();
            for (BbsComment reply : replyList) {
                if ("reject".equals(reply.getModerationStatus()) && 
                    (currentUserId == null || !currentUserId.equals(reply.getAuthorId().intValue()))) {
                    continue;
                }
                fillCommentAuthorInfo(reply);
                filteredReplies.add(reply);
            }
            comment.setReplyList(filteredReplies);
            filteredList.add(comment);
        }

        return CommonMethod.getReturnData(filteredList);
    }

    @Transactional
    public DataResponse getCommentDetail(Long id) {
        Optional<BbsComment> commentOptional = bbsCommentRepository.findById(id);
        if (commentOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("评论不存在");
        }

        BbsComment comment = commentOptional.get();
        fillCommentAuthorInfo(comment);

        return CommonMethod.getReturnData(comment);
    }

    @Transactional
    public DataResponse createComment(Long postId, DataRequest dataRequest) {
        String content = dataRequest.getString("content");
        String imageUrls = dataRequest.getString("imageUrls");
        String attachmentInfos = dataRequest.getString("attachmentInfos");
        Long parentId = dataRequest.getLong("parentId");

        try {
            attachmentInfos = bbsFileService.normalizeAttachmentInfos(attachmentInfos);
        } catch (IllegalArgumentException e) {
            return CommonMethod.getReturnMessageError(e.getMessage());
        }
        boolean hasAttachments = attachmentInfos != null && !attachmentInfos.isBlank();

        if ((content == null || content.isBlank()) && !hasAttachments) {
            return CommonMethod.getReturnMessageError("参数错误：评论内容不能为空");
        }
        if (content == null || content.isBlank()) {
            content = "分享附件";
        }
        if (content.length() < 2 || content.length() > 500) {
            return CommonMethod.getReturnMessageError("参数错误：评论内容长度2-500");
        }

        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<User> userOptional = userRepository.findById(currentUserId);
        if (userOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("用户不存在");
        }

        User user = userOptional.get();
        if (user.getIsBanned()) {
            return CommonMethod.getReturnMessageError("您已被禁言，无法评论");
        }

        // 评论权限检查
        int level = user.getLevel() != null ? user.getLevel() : 0;
        if (!levelPrivilegeService.canComment(level)) {
            throw new IllegalStateException("等级不足，无法评论");
        }

        Optional<BbsPost> postOptional = bbsPostRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        if (post.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("帖子已下架");
        }

        Long effectiveParentId = parentId;
        Long replyToCommentId = null;
        Long replyToUserId = null;
        String replyToUserNickname = null;

        if (parentId != null) {
            Optional<BbsComment> parentCommentOptional = bbsCommentRepository.findById(parentId);
            if (parentCommentOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("父评论不存在");
            }
            BbsComment parentComment = parentCommentOptional.get();
            if (parentComment.getStatus() != 1) {
                return CommonMethod.getReturnMessageError("父评论已下架");
            }
            
            if (parentComment.getParentId() != null) {
                effectiveParentId = parentComment.getParentId();
            }
            
            replyToCommentId = parentId;
            replyToUserId = parentComment.getAuthorId();
            
            if (parentComment.getAuthorId() != null) {
                Optional<User> replyToUserOptional = userRepository.findById(parentComment.getAuthorId().intValue());
                if (replyToUserOptional.isPresent()) {
                    replyToUserNickname = replyToUserOptional.get().getNickname();
                }
            }
        }

        String filteredContent = sensitiveWordFilter.filterNormalWord(content);
        boolean hasSevereWord = sensitiveWordFilter.checkSevereWord(content);

        BbsComment comment = new BbsComment();
        comment.setPostId(postId);
        comment.setAuthorId(currentUserId.longValue());
        comment.setParentId(effectiveParentId);
        comment.setReplyToCommentId(replyToCommentId);
        comment.setReplyToUserId(replyToUserId);
        comment.setReplyToUserNickname(replyToUserNickname);
        comment.setContent(filteredContent);
        comment.setImageUrls(imageUrls);
        comment.setAttachmentInfos(attachmentInfos);
        comment.setLikeCount(0);

        // 设置审核状态
        boolean skipModeration = levelPrivilegeService.canSkipModeration(level);
        if (hasSevereWord) {
            comment.setStatus(0);
            comment.setModerationStatus("reject");
            comment.setModerationViolationLevel("serious");
            comment.setModerationTime(DateTimeTool.parseDateTime(new Date()));
        } else if (skipModeration) {
            comment.setStatus(1);
            comment.setModerationStatus("pass");
        } else {
            comment.setStatus(0);  // 先隐藏，待AI审核通过后显示
            comment.setModerationStatus("pending");
        }

        bbsCommentRepository.saveAndFlush(comment);

        if (!hasSevereWord && !skipModeration) {
            // 异步AI审核
            final Long commentId = comment.getId();
            final String commentContent = filteredContent;
            moderateCommentAsync(commentId, commentContent, post.getTitle());
        }

        if (!hasSevereWord) {
            userRepository.updateCommentCount(currentUserId, 1);

            post.setCommentCount(post.getCommentCount() + 1);
            bbsPostRepository.saveAndFlush(post);

            // 发送评论/回复通知
            String currentUserNickname = user.getNickname();
            String postTitle = post.getTitle() != null ? post.getTitle() : "评论回复通知";

            if (parentId != null && replyToUserId != null && !replyToUserId.equals(currentUserId.longValue())) {
                // 回复评论：通知被回复的用户
                createNotification(replyToUserId, 4, postTitle,
                    "用户「" + currentUserNickname + "」回复了您的评论，帖子ID：" + postId);
            } else if (post.getAuthorId() != null && !post.getAuthorId().equals(currentUserId.longValue())) {
                // 评论帖子：通知帖子作者
                createNotification(post.getAuthorId(), 4, postTitle,
                    "用户「" + currentUserNickname + "」评论了您的帖子，帖子ID：" + postId);
            }

            // 评论积分奖励
            pointService.addPoints(currentUserId, "PUBLISH_COMMENT", "发布评论", comment.getId(), "COMMENT");
            if (post.getAuthorId() != null && !post.getAuthorId().equals(currentUserId.longValue())) {
                pointService.addPoints(post.getAuthorId().intValue(), "RECEIVED_COMMENT", "收到评论", comment.getId(), "COMMENT");
            }
        }

        fillCommentAuthorInfo(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        result.put("hasSevereWord", hasSevereWord);
        result.put("contentFiltered", !filteredContent.equals(content));

        return CommonMethod.getReturnData(result);
    }

    @Async
    @Transactional
    public void moderateCommentAsync(Long commentId, String content, String postTitle) {
        try {
            ModerationResult result = contentModerationService.moderateContent(postTitle, content);
            Optional<BbsComment> commentOpt = bbsCommentRepository.findById(commentId);
            if (commentOpt.isEmpty()) return;
            BbsComment comment = commentOpt.get();

            comment.setModerationStatus(result.getAuditResult());
            comment.setModerationViolationLevel(result.getViolationLevel());
            comment.setModerationViolationType(result.getViolationType());
            comment.setModerationSuggestion(result.getSuggestion());
            comment.setModerationConfidence(result.getConfidence());
            comment.setModerationTime(DateTimeTool.parseDateTime(new Date()));

            if ("pass".equals(result.getAuditResult())) {
                comment.setStatus(1);
            } else {
                comment.setStatus(1);
            }

            bbsCommentRepository.saveAndFlush(comment);

            BbsModerationLog log = new BbsModerationLog();
            log.setPostId(comment.getPostId());
            log.setNewStatus(comment.getModerationStatus());
            log.setViolationLevel(result.getViolationLevel());
            log.setViolationType(result.getViolationType());
            log.setRemark(result.getSuggestion());
            bbsModerationLogRepository.saveAndFlush(log);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public DataResponse updateComment(Long id, DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsComment> commentOptional = bbsCommentRepository.findById(id);
        if (commentOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("评论不存在");
        }

        BbsComment comment = commentOptional.get();
        Integer oldStatus = comment.getStatus();

        String currentUserRole = CommonMethod.getRoleName();
        boolean isAuthor = comment.getAuthorId().equals(currentUserId.longValue());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);

        if (!isAuthor && !isAdmin) {
            return CommonMethod.getReturnMessageError("您无权修改此评论");
        }

        String newContent = comment.getContent();
        String content = dataRequest.getString("content");
        if (content != null) {
            if (content.length() < 2 || content.length() > 500) {
                return CommonMethod.getReturnMessageError("参数错误：评论内容长度2-500");
            }
            newContent = content;
        }

        String filteredContent = sensitiveWordFilter.filterNormalWord(newContent);
        boolean hasSevereWord = sensitiveWordFilter.checkSevereWord(newContent);

        comment.setContent(filteredContent);
        comment.setStatus(hasSevereWord ? 0 : 1);

        if (oldStatus == 1 && hasSevereWord) {
            userRepository.updateCommentCount(comment.getAuthorId().intValue(), -1);

            Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                bbsPostRepository.saveAndFlush(post);
            }
        }

        if (oldStatus == 0 && !hasSevereWord) {
            userRepository.updateCommentCount(comment.getAuthorId().intValue(), 1);

            Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                post.setCommentCount(post.getCommentCount() + 1);
                bbsPostRepository.saveAndFlush(post);
            }
        }

        bbsCommentRepository.saveAndFlush(comment);
        fillCommentAuthorInfo(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        result.put("hasSevereWord", hasSevereWord);
        result.put("contentFiltered", !filteredContent.equals(newContent));

        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse deleteComment(Long id) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsComment> commentOptional = bbsCommentRepository.findById(id);
        if (commentOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("评论不存在");
        }

        BbsComment comment = commentOptional.get();

        String currentUserRole = CommonMethod.getRoleName();
        boolean isAuthor = comment.getAuthorId().equals(currentUserId.longValue());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);

        if (!isAuthor && !isAdmin) {
            return CommonMethod.getReturnMessageError("您无权删除此评论");
        }

        // 删除他人评论权限检查（非管理员删除他人评论）
        if (!isAuthor && !isAdmin) {
            Optional<User> currentUserOpt = userRepository.findById(currentUserId);
            int level = currentUserOpt.map(u -> u.getLevel() != null ? u.getLevel() : 0).orElse(0);
            if (!levelPrivilegeService.canDeleteOthersComment(level)) {
                throw new IllegalStateException("等级不足，无法删除他人评论");
            }
        }

        deleteCommentWithReplies(comment);

        // 管理员删除违规评论扣除积分
        if (isAdmin && comment.getAuthorId() != null) {
            pointService.deductPoints(comment.getAuthorId().intValue(), "COMMENT_DELETED_VIOLATION", "评论违规删除", comment.getId(), "COMMENT");
        }

        return CommonMethod.getReturnMessageOK("删除成功");
    }

    private void deleteCommentWithReplies(BbsComment comment) {
        List<BbsComment> replies = bbsCommentRepository.findByParentId(comment.getId());
        for (BbsComment reply : replies) {
            deleteCommentWithReplies(reply);
        }

        if (comment.getStatus() == 1 && comment.getAuthorId() != null) {
            // 使用原生SQL更新避免乐观锁冲突
            userRepository.updateCommentCount(comment.getAuthorId().intValue(), -1);
        }

        if (comment.getStatus() == 1 && comment.getPostId() != null) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                bbsPostRepository.saveAndFlush(post);
            }
        }

        bbsCommentLikeRepository.deleteByCommentId(comment.getId());
        bbsCommentRepository.delete(comment);
    }

    @Transactional
    public DataResponse toggleLike(Long commentId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsComment> commentOptional = bbsCommentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("评论不存在");
        }

        BbsComment comment = commentOptional.get();
        if (comment.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("评论已下架");
        }

        boolean alreadyLiked = bbsCommentLikeRepository.existsByCommentIdAndUserId(commentId, currentUserId);

        if (alreadyLiked) {
            bbsCommentLikeRepository.deleteByCommentIdAndUserId(commentId, currentUserId);
            comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
        } else {
            BbsCommentLike like = new BbsCommentLike();
            like.setCommentId(commentId);
            like.setUserId(currentUserId);
            bbsCommentLikeRepository.saveAndFlush(like);
            comment.setLikeCount(comment.getLikeCount() + 1);

            // 被点赞积分奖励
            if (comment.getAuthorId() != null && !comment.getAuthorId().equals(currentUserId.longValue())) {
                pointService.addPoints(comment.getAuthorId().intValue(), "RECEIVED_LIKE", "被点赞", like.getId(), "LIKE");
            }
        }
        
        bbsCommentRepository.saveAndFlush(comment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", !alreadyLiked);
        result.put("likeCount", comment.getLikeCount());
        result.put("comment", comment);
        
        return CommonMethod.getReturnData(result);
    }

    public DataResponse getLikeStatus(Long commentId) {
        Integer currentUserId = CommonMethod.getPersonId();
        boolean liked = false;
        long likeCount = 0;

        Optional<BbsComment> commentOptional = bbsCommentRepository.findById(commentId);
        if (commentOptional.isPresent()) {
            likeCount = commentOptional.get().getLikeCount() != null ? commentOptional.get().getLikeCount() : 0;
        }

        if (currentUserId != null) {
            liked = bbsCommentLikeRepository.existsByCommentIdAndUserId(commentId, currentUserId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);

        return CommonMethod.getReturnData(result);
    }

    public DataResponse getCommentLikers(Long commentId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId != null) {
            Optional<User> userOpt = userRepository.findById(currentUserId);
            int level = userOpt.map(u -> u.getLevel() != null ? u.getLevel() : 0).orElse(0);
            if (!levelPrivilegeService.canViewLikers(level)) {
                return CommonMethod.getReturnMessageError("等级达到LV.8方可查看点赞者列表");
            }
        }

        Optional<BbsComment> commentOptional = bbsCommentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("评论不存在");
        }

        List<Map<String, Object>> likers = bbsCommentLikeRepository.findLikersByCommentId(commentId);
        return CommonMethod.getReturnData(likers);
    }
}
