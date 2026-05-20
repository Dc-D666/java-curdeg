package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.BbsReport;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.BbsReportRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class BbsReportService {
    private static final int TARGET_TYPE_POST = 1;
    private static final int TARGET_TYPE_COMMENT = 2;
    private static final int TARGET_TYPE_PROFILE_CARD = 3;

    private static final int HANDLE_TYPE_DELETE_CONTENT = 1;
    private static final int HANDLE_TYPE_REJECT_REPORT = 2;
    private static final int HANDLE_TYPE_CLEAR_PROFILE_CARD = 3;

    private static final String PROFILE_CARD_TITLE = "个人主页资料卡";

    private final BbsReportRepository bbsReportRepository;
    private final BbsPostRepository bbsPostRepository;
    private final BbsCommentRepository bbsCommentRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public BbsReportService(BbsReportRepository bbsReportRepository, 
                           BbsPostRepository bbsPostRepository,
                           BbsCommentRepository bbsCommentRepository,
                           BbsNotificationRepository bbsNotificationRepository,
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.bbsReportRepository = bbsReportRepository;
        this.bbsPostRepository = bbsPostRepository;
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private void fillReportUserInfo(BbsReport report) {
        if (report.getReporterId() != null) {
            Optional<User> reporterOptional = userRepository.findById(report.getReporterId().intValue());
            if (reporterOptional.isPresent()) {
                report.setReporterNickname(reporterOptional.get().getNickname());
            }
        }
        if (report.getHandlerId() != null) {
            Optional<User> handlerOptional = userRepository.findById(report.getHandlerId().intValue());
            if (handlerOptional.isPresent()) {
                report.setHandlerNickname(handlerOptional.get().getNickname());
            }
        }
    }

    @Transactional
    public DataResponse createReport(DataRequest dataRequest) {
        Integer targetType = dataRequest.getInteger("targetType");
        Long targetId = dataRequest.getLong("targetId");
        String reason = dataRequest.getString("reason");

        if (targetType == null) {
            return CommonMethod.getReturnMessageError("参数错误：举报对象类型不能为空");
        }
        if (targetType != TARGET_TYPE_POST && targetType != TARGET_TYPE_COMMENT && targetType != TARGET_TYPE_PROFILE_CARD) {
            return CommonMethod.getReturnMessageError("参数错误：举报对象类型只能是1、2或3");
        }
        if (targetId == null) {
            return CommonMethod.getReturnMessageError("参数错误：举报对象ID不能为空");
        }
        if (reason == null || reason.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：举报理由不能为空");
        }
        if (reason.length() > 500) {
            return CommonMethod.getReturnMessageError("参数错误：举报理由长度不能超过500");
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
            return CommonMethod.getReturnMessageError("您已被禁言，无法举报");
        }

        String targetSnapshot = null;
        if (targetType == TARGET_TYPE_POST) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(targetId);
            if (postOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("帖子不存在");
            }
            BbsPost post = postOptional.get();
            if (post.getStatus() != 1) {
                return CommonMethod.getReturnMessageError("帖子已下架");
            }
        } else if (targetType == TARGET_TYPE_COMMENT) {
            Optional<BbsComment> commentOptional = bbsCommentRepository.findById(targetId);
            if (commentOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("评论不存在");
            }
            BbsComment comment = commentOptional.get();
            if (comment.getStatus() != 1) {
                return CommonMethod.getReturnMessageError("评论已下架");
            }
        } else {
            if (currentUserId.longValue() == targetId) {
                return CommonMethod.getReturnMessageError("不能举报自己的个人主页资料卡");
            }
            Optional<User> targetUserOptional = userRepository.findById(targetId.intValue());
            if (targetUserOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("用户不存在");
            }
            try {
                targetSnapshot = buildProfileCardSnapshot(targetUserOptional.get());
            } catch (JsonProcessingException e) {
                return CommonMethod.getReturnMessageError("生成举报快照失败，请稍后重试");
            }
        }

        BbsReport report = new BbsReport();
        report.setReporterId(currentUserId.longValue());
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setTargetSnapshot(targetSnapshot);
        report.setReason(reason);
        report.setStatus(0);

        bbsReportRepository.saveAndFlush(report);

        return CommonMethod.getReturnData(report);
    }

    public DataResponse getMyReportList(DataRequest dataRequest) {
        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }

        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsReport> reportPage = bbsReportRepository.findByReporterIdOrderByCreateTimeDesc(
            currentUserId.longValue(), pageable);
        
        reportPage.getContent().forEach(this::fillReportUserInfo);

        return CommonMethod.getReturnData(reportPage);
    }

    public DataResponse getAdminReportList(DataRequest dataRequest) {
        Integer status = dataRequest.getInteger("status");
        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<BbsReport> reportPage;
        
        if (status != null) {
            reportPage = bbsReportRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        } else {
            reportPage = bbsReportRepository.findAllByOrderByCreateTimeDesc(pageable);
        }
        
        reportPage.getContent().forEach(this::fillReportUserInfo);

        return CommonMethod.getReturnData(reportPage);
    }

    @Transactional
    public DataResponse handleReport(Long id, DataRequest dataRequest) {
        Integer handleType = dataRequest.getInteger("handleType");
        String handleRemark = dataRequest.getString("handleRemark");

        if (handleType == null) {
            return CommonMethod.getReturnMessageError("参数错误：处理方式不能为空");
        }
        if (handleType != HANDLE_TYPE_DELETE_CONTENT && handleType != HANDLE_TYPE_REJECT_REPORT
                && handleType != HANDLE_TYPE_CLEAR_PROFILE_CARD) {
            return CommonMethod.getReturnMessageError("参数错误：处理方式只能是1、2或3");
        }
        if (handleRemark != null && handleRemark.length() > 200) {
            return CommonMethod.getReturnMessageError("参数错误：处理备注长度不能超过200");
        }

        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsReport> reportOptional = bbsReportRepository.findById(id);
        if (reportOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("举报不存在");
        }

        BbsReport report = reportOptional.get();
        if (report.getStatus() != 0) {
            return CommonMethod.getReturnMessageError("举报已处理");
        }
        if (report.getTargetType() == TARGET_TYPE_PROFILE_CARD && handleType == HANDLE_TYPE_DELETE_CONTENT) {
            return CommonMethod.getReturnMessageError("资料卡举报不能使用删除内容处理");
        }
        if (report.getTargetType() != TARGET_TYPE_PROFILE_CARD && handleType == HANDLE_TYPE_CLEAR_PROFILE_CARD) {
            return CommonMethod.getReturnMessageError("仅个人主页资料卡举报支持清空违规资料");
        }

        report.setStatus(1);
        report.setHandlerId(currentUserId.longValue());
        report.setHandleType(handleType);
        report.setHandleRemark(handleRemark);
        report.setHandleTime(cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date()));
        bbsReportRepository.saveAndFlush(report);

        String targetTitle = resolveReportTargetTitle(report);

        if (handleType == HANDLE_TYPE_DELETE_CONTENT) {
            handleDeleteContent(report, targetTitle);
        } else if (handleType == HANDLE_TYPE_CLEAR_PROFILE_CARD) {
            handleClearProfileCard(report, targetTitle);
        } else {
            createNotification(report.getReporterId(), 2, targetTitle,
                "您的举报（ID：" + id + "）已处理，处理方式：驳回举报，处理备注：" + 
                safeRemark(handleRemark));
        }

        return CommonMethod.getReturnMessageOK("处理成功");
    }

    private void handleDeleteContent(BbsReport report, String postTitle) {
        if (report.getTargetType() == 1) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(report.getTargetId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                boolean wasVisible = post.getStatus() != null && post.getStatus() == 1;
                post.setStatus(0);
                post.setModerationStatus("reject");
                post.setModerationViolationLevel("一般");
                post.setModerationViolationType("举报处理");
                post.setModerationRemark("举报成立，内容已删除。处理备注：" + safeRemark(report.getHandleRemark()));
                post.setModerationTime(cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date()));
                bbsPostRepository.saveAndFlush(post);

                if (post.getAuthorId() != null) {
                    Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                    if (authorOptional.isPresent()) {
                        User author = authorOptional.get();
                        if (wasVisible && author.getPostCount() > 0) {
                            author.setPostCount(author.getPostCount() - 1);
                            userRepository.saveAndFlush(author);
                        }
                        createNotification(author.getPersonId().longValue(), 2, postTitle, 
                            "您的内容（ID：" + report.getTargetId() + "）因违规已被删除，处理备注：" + 
                            (report.getHandleRemark() != null ? report.getHandleRemark() : "无"));
                    }
                }
            }
        } else {
            Optional<BbsComment> commentOptional = bbsCommentRepository.findById(report.getTargetId());
            if (commentOptional.isPresent()) {
                BbsComment comment = commentOptional.get();
                if (comment.getStatus() == 1) {
                    comment.setStatus(0);
                    bbsCommentRepository.saveAndFlush(comment);

                    if (comment.getAuthorId() != null) {
                        Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
                        if (authorOptional.isPresent()) {
                            User author = authorOptional.get();
                            if (author.getCommentCount() > 0) {
                                author.setCommentCount(author.getCommentCount() - 1);
                                userRepository.saveAndFlush(author);
                            }
                            createNotification(author.getPersonId().longValue(), 2, postTitle, 
                                "您的内容（ID：" + report.getTargetId() + "）因违规已被删除，处理备注：" + 
                                (report.getHandleRemark() != null ? report.getHandleRemark() : "无"));
                        }
                    }

                    if (comment.getPostId() != null) {
                        Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
                        if (postOptional.isPresent()) {
                            BbsPost post = postOptional.get();
                            if (post.getCommentCount() > 0) {
                                post.setCommentCount(post.getCommentCount() - 1);
                                bbsPostRepository.saveAndFlush(post);
                            }
                        }
                    }
                }
            }
        }

        createNotification(report.getReporterId(), 2, postTitle, 
            "您的举报（ID：" + report.getId() + "）已处理，处理方式：删除内容，处理备注：" + 
            safeRemark(report.getHandleRemark()));
    }

    private void handleClearProfileCard(BbsReport report, String targetTitle) {
        Optional<User> userOptional = userRepository.findById(report.getTargetId().intValue());
        userOptional.ifPresent(user -> {
            user.setNickname("用户" + user.getPersonId());
            user.setAvatarUrl(null);
            user.setSignature("");
            userRepository.saveAndFlush(user);

            createNotification(user.getPersonId().longValue(), 2, targetTitle,
                "您的个人主页资料卡因违规已被清理，处理备注：" + safeRemark(report.getHandleRemark()));
        });

        createNotification(report.getReporterId(), 2, targetTitle,
            "您的举报（ID：" + report.getId() + "）已处理，处理方式：清空违规资料，处理备注：" +
                safeRemark(report.getHandleRemark()));
    }

    private String resolveReportTargetTitle(BbsReport report) {
        if (report.getTargetType() == TARGET_TYPE_POST) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(report.getTargetId());
            if (postOptional.isPresent() && postOptional.get().getTitle() != null && !postOptional.get().getTitle().isBlank()) {
                return postOptional.get().getTitle();
            }
            return "举报处理通知";
        }

        if (report.getTargetType() == TARGET_TYPE_COMMENT) {
            Optional<BbsComment> commentOptional = bbsCommentRepository.findById(report.getTargetId());
            if (commentOptional.isPresent()) {
                BbsComment comment = commentOptional.get();
                if (comment.getPostId() != null) {
                    Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
                    if (postOptional.isPresent() && postOptional.get().getTitle() != null && !postOptional.get().getTitle().isBlank()) {
                        return postOptional.get().getTitle();
                    }
                }
            }
            return "举报处理通知";
        }

        return PROFILE_CARD_TITLE;
    }

    private String buildProfileCardSnapshot(User targetUser) throws JsonProcessingException {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("userId", targetUser.getPersonId());
        snapshot.put("nickname", targetUser.getNickname());
        snapshot.put("avatarUrl", targetUser.getAvatarUrl());
        snapshot.put("signature", targetUser.getSignature());
        snapshot.put("capturedAt", cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date()));
        return objectMapper.writeValueAsString(snapshot);
    }

    private String safeRemark(String handleRemark) {
        return handleRemark != null && !handleRemark.isBlank() ? handleRemark : "无";
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
}
