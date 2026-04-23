package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.SensitiveWordFilter;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BbsCommentService {

    private final BbsCommentRepository bbsCommentRepository;
    private final BbsPostRepository bbsPostRepository;
    private final UserRepository userRepository;
    private final SensitiveWordFilter sensitiveWordFilter;

    public BbsCommentService(BbsCommentRepository bbsCommentRepository, BbsPostRepository bbsPostRepository,
                           UserRepository userRepository, SensitiveWordFilter sensitiveWordFilter) {
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    private void fillCommentAuthorInfo(BbsComment comment) {
        if (comment.getAuthorId() != null) {
            Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                comment.setAuthorNickname(author.getNickname());
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

        List<BbsComment> commentList = bbsCommentRepository.findByPostIdAndParentIdIsNullAndStatusOrderByCreateTimeAsc(postId, 1);
        
        for (BbsComment comment : commentList) {
            fillCommentAuthorInfo(comment);
            List<BbsComment> replyList = bbsCommentRepository.findByParentIdAndStatusOrderByCreateTimeAsc(comment.getId(), 1);
            replyList.forEach(this::fillCommentAuthorInfo);
            comment.setReplyList(replyList);
        }

        return CommonMethod.getReturnData(commentList);
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
        Long parentId = dataRequest.getLong("parentId");

        if (content == null || content.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：评论内容不能为空");
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
        comment.setLikeCount(0);
        comment.setStatus(hasSevereWord ? 0 : 1);

        bbsCommentRepository.saveAndFlush(comment);

        if (!hasSevereWord) {
            user.setCommentCount(user.getCommentCount() + 1);
            userRepository.saveAndFlush(user);

            post.setCommentCount(post.getCommentCount() + 1);
            bbsPostRepository.saveAndFlush(post);
        }

        fillCommentAuthorInfo(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("comment", comment);
        result.put("hasSevereWord", hasSevereWord);
        result.put("contentFiltered", !filteredContent.equals(content));

        return CommonMethod.getReturnData(result);
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
            Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                author.setCommentCount(Math.max(0, author.getCommentCount() - 1));
                userRepository.saveAndFlush(author);
            }

            Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                bbsPostRepository.saveAndFlush(post);
            }
        }

        if (oldStatus == 0 && !hasSevereWord) {
            Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                author.setCommentCount(author.getCommentCount() + 1);
                userRepository.saveAndFlush(author);
            }

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

        deleteCommentWithReplies(comment);

        return CommonMethod.getReturnMessageOK("删除成功");
    }

    private void deleteCommentWithReplies(BbsComment comment) {
        List<BbsComment> replies = bbsCommentRepository.findByParentId(comment.getId());
        for (BbsComment reply : replies) {
            deleteCommentWithReplies(reply);
        }

        if (comment.getStatus() == 1 && comment.getAuthorId() != null) {
            Optional<User> authorOptional = userRepository.findById(comment.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                author.setCommentCount(Math.max(0, author.getCommentCount() - 1));
                userRepository.saveAndFlush(author);
            }
        }

        if (comment.getStatus() == 1 && comment.getPostId() != null) {
            Optional<BbsPost> postOptional = bbsPostRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                BbsPost post = postOptional.get();
                post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
                bbsPostRepository.saveAndFlush(post);
            }
        }

        bbsCommentRepository.delete(comment);
    }
}
