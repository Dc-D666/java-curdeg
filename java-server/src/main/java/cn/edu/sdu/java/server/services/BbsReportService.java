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
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BbsReportService {

    private final BbsReportRepository bbsReportRepository;
    private final BbsPostRepository bbsPostRepository;
    private final BbsCommentRepository bbsCommentRepository;
    private final BbsNotificationRepository bbsNotificationRepository;
    private final UserRepository userRepository;

    public BbsReportService(BbsReportRepository bbsReportRepository, 
                           BbsPostRepository bbsPostRepository,
                           BbsCommentRepository bbsCommentRepository,
                           BbsNotificationRepository bbsNotificationRepository,
                           UserRepository userRepository) {
        this.bbsReportRepository = bbsReportRepository;
        this.bbsPostRepository = bbsPostRepository;
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.userRepository = userRepository;
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
        if (targetType != 1 && targetType != 2) {
            return CommonMethod.getReturnMessageError("参数错误：举报对象类型只能是1或2");
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

        if (targetType == 1) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(targetId);
            if (postOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("帖子不存在");
            }
            BbsPost post = postOptional.get();
            if (post.getStatus() != 1) {
                return CommonMethod.getReturnMessageError("帖子已下架");
            }
        } else {
            Optional<BbsComment> commentOptional = bbsCommentRepository.findById(targetId);
            if (commentOptional.isEmpty()) {
                return CommonMethod.getReturnMessageError("评论不存在");
            }
            BbsComment comment = commentOptional.get();
            if (comment.getStatus() != 1) {
                return CommonMethod.getReturnMessageError("评论已下架");
            }
        }

        BbsReport report = new BbsReport();
        report.setReporterId(currentUserId.longValue());
        report.setTargetType(targetType);
        report.setTargetId(targetId);
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

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
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

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsReport> reportPage;
        
        if (status != null) {
            reportPage = bbsReportRepository.findByStatusOrderByCreateTimeDesc(status, pageable);
        } else {
            reportPage = bbsReportRepository.findAll(pageable);
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
        if (handleType != 1 && handleType != 2) {
            return CommonMethod.getReturnMessageError("参数错误：处理方式只能是1或2");
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

        report.setStatus(1);
        report.setHandlerId(currentUserId.longValue());
        report.setHandleType(handleType);
        report.setHandleRemark(handleRemark);
        report.setHandleTime(cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date()));
        bbsReportRepository.saveAndFlush(report);

        if (handleType == 1) {
            handleDeleteContent(report);
        } else {
            createNotification(report.getReporterId(), 
                "您的举报（ID：" + id + "）已处理，处理方式：驳回举报，处理备注：" + 
                (handleRemark != null ? handleRemark : "无"));
        }

        return CommonMethod.getReturnMessageOK("处理成功");
    }

    private void handleDeleteContent(BbsReport report) {
        if (report.getTargetType() == 1) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(report.getTargetId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                if (post.getStatus() == 1) {
                    post.setStatus(0);
                    bbsPostRepository.saveAndFlush(post);

                    if (post.getAuthorId() != null) {
                        Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                        if (authorOptional.isPresent()) {
                            User author = authorOptional.get();
                            if (author.getPostCount() > 0) {
                                author.setPostCount(author.getPostCount() - 1);
                                userRepository.saveAndFlush(author);
                            }
                            createNotification(author.getPersonId().longValue(), 
                                "您的内容（ID：" + report.getTargetId() + "）因违规已被删除，处理备注：" + 
                                (report.getHandleRemark() != null ? report.getHandleRemark() : "无"));
                        }
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
                            createNotification(author.getPersonId().longValue(), 
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

        createNotification(report.getReporterId(), 
            "您的举报（ID：" + report.getId() + "）已处理，处理方式：删除内容，处理备注：" + 
            (report.getHandleRemark() != null ? report.getHandleRemark() : "无"));
    }

    private void createNotification(Long receiverId, String content) {
        BbsNotification notification = new BbsNotification();
        notification.setReceiverId(receiverId);
        notification.setContent(content);
        notification.setIsRead(0);
        bbsNotificationRepository.saveAndFlush(notification);
    }
}
