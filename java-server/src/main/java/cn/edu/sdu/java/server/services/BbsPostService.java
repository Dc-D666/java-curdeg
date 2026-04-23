package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.BbsBoard;
import cn.edu.sdu.java.server.models.BbsFollow;
import cn.edu.sdu.java.server.models.BbsFavorite;
import cn.edu.sdu.java.server.models.BbsLike;
import cn.edu.sdu.java.server.models.BbsNotification;
import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsBoardRepository;
import cn.edu.sdu.java.server.repositorys.BbsCommentRepository;
import cn.edu.sdu.java.server.repositorys.BbsFollowRepository;
import cn.edu.sdu.java.server.repositorys.BbsFavoriteRepository;
import cn.edu.sdu.java.server.repositorys.BbsLikeRepository;
import cn.edu.sdu.java.server.repositorys.BbsNotificationRepository;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.SensitiveWordFilter;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BbsPostService {

    private final BbsPostRepository bbsPostRepository;
    private final UserRepository userRepository;
    private final BbsBoardRepository bbsBoardRepository;
    private final BbsCommentRepository bbsCommentRepository;
    private final BbsLikeRepository bbsLikeRepository;
    private final BbsFavoriteRepository bbsFavoriteRepository;
    private final SensitiveWordFilter sensitiveWordFilter;
    private final BbsFollowRepository bbsFollowRepository;
    private final BbsNotificationRepository bbsNotificationRepository;

    public BbsPostService(BbsPostRepository bbsPostRepository, UserRepository userRepository,
                         BbsBoardRepository bbsBoardRepository, BbsCommentRepository bbsCommentRepository,
                         BbsLikeRepository bbsLikeRepository, BbsFavoriteRepository bbsFavoriteRepository,
                         SensitiveWordFilter sensitiveWordFilter, BbsFollowRepository bbsFollowRepository,
                         BbsNotificationRepository bbsNotificationRepository) {
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
        this.bbsBoardRepository = bbsBoardRepository;
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsLikeRepository = bbsLikeRepository;
        this.bbsFavoriteRepository = bbsFavoriteRepository;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.bbsFollowRepository = bbsFollowRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
    }

    private void fillPostAuthorInfo(BbsPost post) {
        if (post.getAuthorId() != null) {
            Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                post.setAuthorNickname(author.getNickname());
                String avatarUrl = author.getAvatarUrl();
                if (avatarUrl == null || avatarUrl.isBlank()) {
                    avatarUrl = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";
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
    }

    public DataResponse getPostList(DataRequest dataRequest) {
        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");
        Long boardId = dataRequest.getLong("boardId");
        String keyword = dataRequest.getString("keyword");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 20;
        }
        if (keyword != null && keyword.length() > 50) {
            return CommonMethod.getReturnMessageError("参数错误：搜索关键词长度不能超过50");
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsPost> postPage = bbsPostRepository.findPostsByCondition(boardId, keyword, pageable);

        postPage.getContent().forEach(this::fillPostAuthorInfo);

        return CommonMethod.getReturnData(postPage);
    }

    @Transactional
    public DataResponse getPostDetail(Long id) {
        Optional<BbsPost> postOptional = bbsPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        fillPostAuthorInfo(post);

        return CommonMethod.getReturnData(post);
    }

    @Transactional
    public DataResponse createPost(DataRequest dataRequest) {
        String title = dataRequest.getString("title");
        String content = dataRequest.getString("content");
        String imageUrls = dataRequest.getString("imageUrls");
        Long boardId = dataRequest.getLong("boardId");

        if (title == null || title.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：帖子标题不能为空");
        }
        if (title.length() < 5 || title.length() > 100) {
            return CommonMethod.getReturnMessageError("参数错误：帖子标题长度5-100");
        }

        if (content == null || content.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：帖子内容不能为空");
        }
        if (content.length() < 10) {
            return CommonMethod.getReturnMessageError("参数错误：帖子内容长度至少10");
        }

        if (imageUrls != null && imageUrls.length() > 1000) {
            return CommonMethod.getReturnMessageError("参数错误：图片URL列表长度不能超过1000");
        }

        if (boardId == null) {
            return CommonMethod.getReturnMessageError("参数错误：所属板块不能为空");
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
            return CommonMethod.getReturnMessageError("您已被禁言，无法发帖");
        }

        Optional<BbsBoard> boardOptional = bbsBoardRepository.findById(boardId);
        if (boardOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("板块不存在");
        }

        String filteredTitle = sensitiveWordFilter.filterNormalWord(title);
        String filteredContent = sensitiveWordFilter.filterNormalWord(content);
        boolean hasSevereWord = sensitiveWordFilter.checkSevereWord(title) || sensitiveWordFilter.checkSevereWord(content);

        BbsPost post = new BbsPost();
        post.setTitle(filteredTitle);
        post.setContent(filteredContent);
        post.setImageUrls(imageUrls);
        post.setBoardId(boardId);
        post.setAuthorId(currentUserId.longValue());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);
        post.setIsTop(false);
        post.setIsFeatured(false);
        post.setStatus(hasSevereWord ? 0 : 1);

        bbsPostRepository.saveAndFlush(post);

        if (!hasSevereWord) {
            user.setPostCount(user.getPostCount() + 1);
            userRepository.saveAndFlush(user);
            
            try {
                List<BbsFollow> followers = bbsFollowRepository.findByFollowingId(currentUserId);
                if (!followers.isEmpty()) {
                    List<BbsNotification> notifications = new ArrayList<>();
                    String authorNickname = user.getNickname();
                    String notificationContent = String.format("用户【%s】发布帖子", authorNickname);
                    
                    for (BbsFollow follow : followers) {
                        BbsNotification notification = new BbsNotification();
                        notification.setReceiverId(follow.getFollowerId().longValue());
                        notification.setType(6);
                        notification.setTitle("关注用户发帖通知");
                        notification.setContent(notificationContent);
                        notifications.add(notification);
                    }
                    
                    bbsNotificationRepository.saveAll(notifications);
                }
            } catch (Exception e) {
            }
        }

        fillPostAuthorInfo(post);

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("hasSevereWord", hasSevereWord);
        result.put("contentFiltered", !filteredTitle.equals(title) || !filteredContent.equals(content));

        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse updatePost(Long id, DataRequest dataRequest) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsPost> postOptional = bbsPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        Integer oldStatus = post.getStatus();

        String currentUserRole = CommonMethod.getRoleName();
        boolean isAuthor = post.getAuthorId().equals(currentUserId.longValue());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);

        if (!isAuthor && !isAdmin) {
            return CommonMethod.getReturnMessageError("您无权修改此帖子");
        }

        String newTitle = post.getTitle();
        String newContent = post.getContent();

        String title = dataRequest.getString("title");
        if (title != null && !title.isBlank()) {
            if (title.length() < 5 || title.length() > 100) {
                return CommonMethod.getReturnMessageError("参数错误：帖子标题长度5-100");
            }
            newTitle = title;
        }

        String content = dataRequest.getString("content");
        if (content != null) {
            if (content.length() < 10) {
                return CommonMethod.getReturnMessageError("参数错误：帖子内容长度至少10");
            }
            newContent = content;
        }

        String filteredTitle = sensitiveWordFilter.filterNormalWord(newTitle);
        String filteredContent = sensitiveWordFilter.filterNormalWord(newContent);
        boolean hasSevereWord = sensitiveWordFilter.checkSevereWord(newTitle) || sensitiveWordFilter.checkSevereWord(newContent);

        post.setTitle(filteredTitle);
        post.setContent(filteredContent);

        String imageUrls = dataRequest.getString("imageUrls");
        if (imageUrls != null) {
            if (imageUrls.length() > 1000) {
                return CommonMethod.getReturnMessageError("参数错误：图片URL列表长度不能超过1000");
            }
            post.setImageUrls(imageUrls);
        }

        post.setStatus(hasSevereWord ? 0 : 1);

        if (oldStatus == 1 && hasSevereWord) {
            Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                author.setPostCount(Math.max(0, author.getPostCount() - 1));
                userRepository.saveAndFlush(author);
            }
        }

        if (oldStatus == 0 && !hasSevereWord) {
            Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                author.setPostCount(author.getPostCount() + 1);
                userRepository.saveAndFlush(author);
            }
        }

        bbsPostRepository.saveAndFlush(post);
        fillPostAuthorInfo(post);

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("hasSevereWord", hasSevereWord);
        result.put("contentFiltered", !filteredTitle.equals(newTitle) || !filteredContent.equals(newContent));

        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse deletePost(Long id) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsPost> postOptional = bbsPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();

        String currentUserRole = CommonMethod.getRoleName();
        boolean isAuthor = post.getAuthorId().equals(currentUserId.longValue());
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);

        if (!isAuthor && !isAdmin) {
            return CommonMethod.getReturnMessageError("您无权删除此帖子");
        }

        if (post.getStatus() == 1 && post.getAuthorId() != null) {
            Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
            if (authorOptional.isPresent()) {
                User author = authorOptional.get();
                author.setPostCount(Math.max(0, author.getPostCount() - 1));
                userRepository.saveAndFlush(author);
            }
        }

        bbsCommentRepository.deleteByPostId(id);
        bbsLikeRepository.deleteByPostId(id);
        bbsFavoriteRepository.deleteByPostId(id);
        bbsPostRepository.delete(post);

        return CommonMethod.getReturnMessageOK("删除成功");
    }

    @Transactional
    public DataResponse toggleTop(Long id) {
        Optional<BbsPost> postOptional = bbsPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        if (post.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("帖子已下架");
        }

        post.setIsTop(!post.getIsTop());
        bbsPostRepository.saveAndFlush(post);

        return CommonMethod.getReturnData(post);
    }

    @Transactional
    public DataResponse toggleFeature(Long id) {
        Optional<BbsPost> postOptional = bbsPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        if (post.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("帖子已下架");
        }

        post.setIsFeatured(!post.getIsFeatured());
        bbsPostRepository.saveAndFlush(post);

        return CommonMethod.getReturnData(post);
    }

    @Transactional
    public DataResponse toggleLike(Long postId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsPost> postOptional = bbsPostRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        if (post.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("帖子已下架");
        }

        boolean alreadyLiked = bbsLikeRepository.existsByPostIdAndUserId(postId, currentUserId);
        
        if (alreadyLiked) {
            bbsLikeRepository.deleteByPostIdAndUserId(postId, currentUserId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        } else {
            BbsLike like = new BbsLike();
            like.setPostId(postId);
            like.setUserId(currentUserId);
            bbsLikeRepository.saveAndFlush(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        
        bbsPostRepository.saveAndFlush(post);
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", !alreadyLiked);
        result.put("likeCount", post.getLikeCount());
        result.put("post", post);
        
        return CommonMethod.getReturnData(result);
    }

    public DataResponse getLikeStatus(Long postId) {
        Integer currentUserId = CommonMethod.getPersonId();
        boolean liked = false;
        long likeCount = 0;
        
        Optional<BbsPost> postOptional = bbsPostRepository.findById(postId);
        if (postOptional.isPresent()) {
            likeCount = postOptional.get().getLikeCount() != null ? postOptional.get().getLikeCount() : 0;
        }
        
        if (currentUserId != null) {
            liked = bbsLikeRepository.existsByPostIdAndUserId(postId, currentUserId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likeCount);
        
        return CommonMethod.getReturnData(result);
    }

    @Transactional
    public DataResponse toggleFavorite(Long postId) {
        Integer currentUserId = CommonMethod.getPersonId();
        if (currentUserId == null) {
            return CommonMethod.getReturnMessageError("用户未登录");
        }

        Optional<BbsPost> postOptional = bbsPostRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return CommonMethod.getReturnMessageError("帖子不存在");
        }

        BbsPost post = postOptional.get();
        if (post.getStatus() != 1) {
            return CommonMethod.getReturnMessageError("帖子已下架");
        }

        boolean alreadyFavorited = bbsFavoriteRepository.existsByPostIdAndUserId(postId, currentUserId);
        
        if (alreadyFavorited) {
            bbsFavoriteRepository.deleteByPostIdAndUserId(postId, currentUserId);
            post.setFavoriteCount(Math.max(0, post.getFavoriteCount() - 1));
        } else {
            BbsFavorite favorite = new BbsFavorite();
            favorite.setPostId(postId);
            favorite.setUserId(currentUserId);
            bbsFavoriteRepository.saveAndFlush(favorite);
            post.setFavoriteCount(post.getFavoriteCount() + 1);
        }
        
        bbsPostRepository.saveAndFlush(post);
        
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", !alreadyFavorited);
        result.put("favoriteCount", post.getFavoriteCount());
        result.put("post", post);
        
        return CommonMethod.getReturnData(result);
    }

    public DataResponse getFavoriteStatus(Long postId) {
        Integer currentUserId = CommonMethod.getPersonId();
        boolean favorited = false;
        long favoriteCount = 0;
        
        Optional<BbsPost> postOptional = bbsPostRepository.findById(postId);
        if (postOptional.isPresent()) {
            favoriteCount = postOptional.get().getFavoriteCount() != null ? postOptional.get().getFavoriteCount() : 0;
        }
        
        if (currentUserId != null) {
            favorited = bbsFavoriteRepository.existsByPostIdAndUserId(postId, currentUserId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        result.put("favoriteCount", favoriteCount);
        
        return CommonMethod.getReturnData(result);
    }
}
