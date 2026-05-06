
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
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
    private final PostModerationService postModerationService;

    public BbsPostService(BbsPostRepository bbsPostRepository, UserRepository userRepository,
                          BbsBoardRepository bbsBoardRepository, BbsCommentRepository bbsCommentRepository,
                          BbsLikeRepository bbsLikeRepository, BbsFavoriteRepository bbsFavoriteRepository,
                          SensitiveWordFilter sensitiveWordFilter, BbsFollowRepository bbsFollowRepository,
                          BbsNotificationRepository bbsNotificationRepository, PostModerationService postModerationService) {
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
        this.bbsBoardRepository = bbsBoardRepository;
        this.bbsCommentRepository = bbsCommentRepository;
        this.bbsLikeRepository = bbsLikeRepository;
        this.bbsFavoriteRepository = bbsFavoriteRepository;
        this.sensitiveWordFilter = sensitiveWordFilter;
        this.bbsFollowRepository = bbsFollowRepository;
        this.bbsNotificationRepository = bbsNotificationRepository;
        this.postModerationService = postModerationService;
    }

    private void fillPostAuthorInfo(BbsPost post) {
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

        Integer currentUserId = CommonMethod.getPersonId();
        String currentUserRole = CommonMethod.getRoleName();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);
        Long currentUserIdLong = currentUserId != null ? currentUserId.longValue() : -1L;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsPost> postPage = bbsPostRepository.findPostsByConditionWithModeration(
                boardId, keyword, currentUserIdLong, isAdmin, pageable);

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
        
        // 增加浏览量
        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        bbsPostRepository.saveAndFlush(post);
        
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
        if (title.length() > 256) {
            return CommonMethod.getReturnMessageError("参数错误：帖子标题长度不能超过256");
        }

        if (content == null || content.isBlank()) {
            return CommonMethod.getReturnMessageError("参数错误：帖子内容不能为空");
        }
        if (content.length() > 20000) {
            return CommonMethod.getReturnMessageError("参数错误：帖子内容长度不能超过20000");
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

        // 注释掉敏感词库审核功能，只使用AI审核
        // String filteredTitle = sensitiveWordFilter.filterNormalWord(title);
        // String filteredContent = sensitiveWordFilter.filterNormalWord(content);
        // boolean hasSevereWord = sensitiveWordFilter.checkSevereWord(title) || sensitiveWordFilter.checkSevereWord(content);
        
        String filteredTitle = title;  // 不经过敏感词库过滤
        String filteredContent = content;
        boolean hasSevereWord = false;  // 禁用敏感词库检测

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
        post.setStatus(0);  // 初始设为不可见，等待AI审核结果
        post.setModerationStatus("pending");

        log.info("正在保存帖子...");
        bbsPostRepository.saveAndFlush(post);
        
        log.info("帖子保存成功，ID：{}", post.getId());

        // ===== 关键修改：注册事务同步，确保事务提交后再调用异步审核 =====
        final Long postId = post.getId();
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("事务已提交，准备调用AI审核，帖子ID：{}", postId);
                // 确保事务完全提交后再调用
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                postModerationService.moderatePostAsync(postId);
            }
        });

        // 暂时先不更新用户发帖数，等AI审核通过后再更新（或者由AI审核通过后再处理）
        // if (!hasSevereWord) {
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
        // }  // 注释掉敏感词库判断结束

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
            if (title.length() > 256) {
                return CommonMethod.getReturnMessageError("参数错误：帖子标题长度不能超过256");
            }
            newTitle = title;
        }

        String content = dataRequest.getString("content");
        if (content != null) {
            if (content.length() > 20000) {
                return CommonMethod.getReturnMessageError("参数错误：帖子内容长度不能超过20000");
            }
            newContent = content;
        }

        // 注释掉敏感词库审核功能，只使用AI审核
        // String filteredTitle = sensitiveWordFilter.filterNormalWord(newTitle);
        // String filteredContent = sensitiveWordFilter.filterNormalWord(newContent);
        // boolean hasSevereWord = sensitiveWordFilter.checkSevereWord(newTitle) || sensitiveWordFilter.checkSevereWord(newContent);
        
        String filteredTitle = newTitle;  // 不经过敏感词库过滤
        String filteredContent = newContent;
        boolean hasSevereWord = false;  // 禁用敏感词库检测

        post.setTitle(filteredTitle);
        post.setContent(filteredContent);

        String imageUrls = dataRequest.getString("imageUrls");
        if (imageUrls != null) {
            if (imageUrls.length() > 1000) {
                return CommonMethod.getReturnMessageError("参数错误：图片URL列表长度不能超过1000");
            }
            post.setImageUrls(imageUrls);
        }

        // 重置审核状态为 pending，并设置为不可见
        post.setModerationStatus("pending");
        post.setStatus(0);  // 审核通过前不显示

        log.info("正在保存帖子...");
        bbsPostRepository.saveAndFlush(post);
        log.info("帖子保存成功，ID：{}", post.getId());

        // ===== 关键修改：注册事务同步，确保事务提交后再调用异步审核 =====
        final Long postId = post.getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("事务已提交，准备调用AI审核，帖子ID：{}", postId);
                // 确保事务完全提交后再调用
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                postModerationService.moderatePostAsync(postId);
            }
        });

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

    public DataResponse searchPosts(String keyword, String searchType, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 20;
        }

        Integer currentUserId = CommonMethod.getPersonId();
        String currentUserRole = CommonMethod.getRoleName();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);
        Long currentUserIdLong = currentUserId != null ? currentUserId.longValue() : -1L;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsPost> postPage = bbsPostRepository.searchPostsWithModerationByType(
                keyword, searchType, currentUserIdLong, isAdmin, pageable);

        // 填充信息并处理高亮
        postPage.getContent().forEach(post -> {
            fillPostAuthorInfo(post);
            applyHighlight(post, keyword, searchType);
        });

        return CommonMethod.getReturnData(postPage);
    }

    private void applyHighlight(BbsPost post, String keyword, String searchType) {
        if (keyword == null || keyword.trim().isEmpty()) {
            post.setHighlightTitle(post.getTitle());
            post.setHighlightSnippet(extractSnippet(post.getContent()));
            return;
        }

        // 对标题进行高亮处理
        String highlightedTitle = highlightText(post.getTitle(), keyword);
        post.setHighlightTitle(highlightedTitle);

        // 对内容进行高亮并提取片段
        String snippet = extractAndHighlightSnippet(post.getContent(), keyword);
        post.setHighlightSnippet(snippet);
    }

    private String highlightText(String text, String keyword) {
        if (text == null || keyword == null || keyword.trim().isEmpty()) {
            return text;
        }

        String highlightStart = "<span style=\"color:red;font-weight:bold\">";
        String highlightEnd = "</span>";

        // 简单的不区分大小写替换
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        int lastIndex = 0;
        StringBuilder sb = new StringBuilder();
        int index;

        while ((index = lowerText.indexOf(lowerKeyword, lastIndex)) != -1) {
            // 添加匹配前的部分
            sb.append(text, lastIndex, index);
            // 添加高亮标签和匹配内容
            sb.append(highlightStart);
            sb.append(text, index, index + keyword.length());
            sb.append(highlightEnd);
            // 更新位置
            lastIndex = index + keyword.length();
        }
        // 添加剩余部分
        sb.append(text, lastIndex, text.length());

        return sb.toString();
    }

    private String extractAndHighlightSnippet(String content, String keyword) {
        if (content == null) {
            return "";
        }

        String cleanContent = content.replaceAll("\\s+", " ").trim();

        // 如果内容很短，直接高亮返回
        if (cleanContent.length() <= 200) {
            return highlightText(cleanContent, keyword);
        }

        // 尝试找到关键词位置
        int keywordIndex = cleanContent.toLowerCase().indexOf(keyword.toLowerCase());

        String snippet;
        if (keywordIndex >= 0) {
            // 从关键词前后各截取一部分，构成片段
            int start = Math.max(0, keywordIndex - 50);
            int end = Math.min(cleanContent.length(), keywordIndex + keyword.length() + 150);
            if (start > 0) {
                snippet = "..." + cleanContent.substring(start, end) + "...";
            } else {
                snippet = cleanContent.substring(start, end) + "...";
            }
        } else {
            // 如果没找到关键词，截取开头
            snippet = cleanContent.substring(0, Math.min(200, cleanContent.length())) + "...";
        }

        return highlightText(snippet, keyword);
    }

    private String extractSnippet(String content) {
        if (content == null) {
            return "";
        }

        String cleanContent = content.replaceAll("\\s+", " ").trim();
        if (cleanContent.length() <= 200) {
            return cleanContent;
        }
        return cleanContent.substring(0, 200) + "...";
    }

    public DataResponse getUserPosts(Long userId, DataRequest dataRequest) {
        Integer pageNum = dataRequest.getInteger("pageNum");
        Integer pageSize = dataRequest.getInteger("pageSize");

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 50) {
            pageSize = 10;
        }

        Integer currentUserId = CommonMethod.getPersonId();
        String currentUserRole = CommonMethod.getRoleName();
        boolean isAdmin = "ROLE_ADMIN".equals(currentUserRole) || "ROLE_SUPER".equals(currentUserRole);
        Long currentUserIdLong = currentUserId != null ? currentUserId.longValue() : -1L;

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<BbsPost> postPage = bbsPostRepository.findUserPostsWithModeration(
                userId, currentUserIdLong, isAdmin, pageable);

        postPage.getContent().forEach(this::fillPostAuthorInfo);

        return CommonMethod.getReturnData(postPage);
    }
}
