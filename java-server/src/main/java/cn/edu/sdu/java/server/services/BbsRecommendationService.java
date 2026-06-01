package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BbsRecommendationService {

    private final BbsPostRepository bbsPostRepository;
    private final BbsUserBrowseHistoryRepository browseHistoryRepository;
    private final BbsFollowRepository bbsFollowRepository;
    private final BbsBoardRepository bbsBoardRepository;
    private final UserRepository userRepository;
    private final LevelPrivilegeService levelPrivilegeService;

    private static final int RECOMMEND_LIMIT = 20;

    public Map<String, Object> getRecommendations() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();

        // 1. 根据浏览历史推荐
        List<Map<String, Object>> browseRecommendations = getBrowseHistoryRecommendations(userId);
        result.put("browseRecommendations", browseRecommendations);
        result.put("browseRecommendationsTitle", "根据你的浏览历史推荐");

        // 2. 关注用户最新发帖
        List<Map<String, Object>> followingPosts = getFollowingPosts(userId);
        result.put("followingPosts", followingPosts);
        result.put("followingPostsTitle", "你关注的人最新发帖");

        // 3. 相似帖子推荐
        List<Map<String, Object>> similarPosts = getSimilarPosts(userId);
        result.put("similarPosts", similarPosts);
        result.put("similarPostsTitle", "相似帖子推荐");

        return result;
    }

    private List<Map<String, Object>> getBrowseHistoryRecommendations(Integer userId) {
        List<Long> recentPostIds = browseHistoryRepository.findRecentPostIdsByUserIdWithLimit(userId, 5);
        if (recentPostIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<BbsPost> recentPosts = new ArrayList<>();
        for (Long postId : recentPostIds) {
            Optional<BbsPost> postOpt = bbsPostRepository.findById(postId);
            if (postOpt.isPresent() && isPostVisible(postOpt.get(), userId)) {
                recentPosts.add(postOpt.get());
            }
        }

        if (recentPosts.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取用户浏览过的板块
        Set<Long> browsedBoardIds = recentPosts.stream()
                .map(BbsPost::getBoardId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> browsedAuthorIds = recentPosts.stream()
                .map(BbsPost::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 获取用户最近浏览的作者，关注更多相似作者
        Set<Integer> similarAuthors = new HashSet<>();
        for (Long postId : recentPostIds) {
            Optional<BbsPost> postOpt = bbsPostRepository.findById(postId);
            if (postOpt.isPresent()) {
                Long authorId = postOpt.get().getAuthorId();
                if (authorId != null) {
                    List<BbsFollow> followers = bbsFollowRepository.findByFollowingId(authorId.intValue());
                    for (BbsFollow follow : followers) {
                        if (!follow.getFollowerId().equals(userId)) {
                            similarAuthors.add(follow.getFollowerId());
                        }
                    }
                }
            }
        }

        List<BbsPost> recommendations = new ArrayList<>();
        Set<Long> excludePostIds = new HashSet<>(recentPostIds);

        // 优先推荐同一板块的高热度帖子
        for (Long boardId : browsedBoardIds) {
            if (recommendations.size() >= RECOMMEND_LIMIT) break;
            List<BbsPost> boardPosts = bbsPostRepository.findTop10ByStatusOrderByLikeCountDescCommentCountDesc(1);
            for (BbsPost post : boardPosts) {
                if (recommendations.size() >= RECOMMEND_LIMIT) break;
                if (!excludePostIds.contains(post.getId()) && 
                    post.getBoardId() != null && post.getBoardId().equals(boardId) &&
                    isPostVisible(post, userId)) {
                    recommendations.add(post);
                    excludePostIds.add(post.getId());
                }
            }
        }

        // 如果还不够，添加热门帖子
        if (recommendations.size() < RECOMMEND_LIMIT) {
            List<BbsPost> hotPosts = bbsPostRepository.findTop20ByStatusOrderByLikeCountDesc(1);
            for (BbsPost post : hotPosts) {
                if (recommendations.size() >= RECOMMEND_LIMIT) break;
                if (!excludePostIds.contains(post.getId()) && isPostVisible(post, userId)) {
                    recommendations.add(post);
                    excludePostIds.add(post.getId());
                }
            }
        }

        return convertPostsToMaps(recommendations);
    }

    private List<Map<String, Object>> getFollowingPosts(Integer userId) {
        List<BbsFollow> following = bbsFollowRepository.findByFollowerId(userId);
        if (following.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> followingIds = following.stream()
                .map(BbsFollow::getFollowingId)
                .collect(Collectors.toList());

        List<BbsPost> posts = new ArrayList<>();
        Set<Long> addedPostIds = new HashSet<>();

        // 获取关注用户的最新帖子
        for (Integer authorId : followingIds) {
            if (posts.size() >= RECOMMEND_LIMIT) break;
            List<BbsPost> authorPosts = bbsPostRepository.findUserVisiblePosts(authorId.longValue(), PageRequest.of(0, 3)).getContent();
            for (BbsPost post : authorPosts) {
                if (posts.size() >= RECOMMEND_LIMIT) break;
                if (!addedPostIds.contains(post.getId())) {
                    posts.add(post);
                    addedPostIds.add(post.getId());
                }
            }
        }

        // 按时间排序
        posts.sort((a, b) -> {
            String timeA = a.getCreateTime() != null ? a.getCreateTime() : "";
            String timeB = b.getCreateTime() != null ? b.getCreateTime() : "";
            return timeB.compareTo(timeA);
        });

        return convertPostsToMaps(posts);
    }

    private List<Map<String, Object>> getSimilarPosts(Integer userId) {
        List<Long> recentPostIds = browseHistoryRepository.findRecentPostIdsByUserIdWithLimit(userId, 3);
        if (recentPostIds.isEmpty()) {
            return getPopularPostsAsFallback();
        }

        Set<Long> excludePostIds = new HashSet<>(recentPostIds);
        List<BbsPost> recommendations = new ArrayList<>();

        // 获取最近浏览的帖子作为相似度基准
        for (Long postId : recentPostIds) {
            Optional<BbsPost> basePostOpt = bbsPostRepository.findById(postId);
            if (basePostOpt.isEmpty()) continue;

            BbsPost basePost = basePostOpt.get();
            
            // 查找同板块的帖子
            if (basePost.getBoardId() != null) {
                Page<BbsPost> boardPosts = bbsPostRepository.findPostsByCondition(basePost.getBoardId(), null, PageRequest.of(0, 20));
                for (BbsPost post : boardPosts) {
                    if (recommendations.size() >= RECOMMEND_LIMIT) break;
                    if (!excludePostIds.contains(post.getId()) && isPostVisible(post, userId)) {
                        recommendations.add(post);
                        excludePostIds.add(post.getId());
                    }
                }
            }

            // 查找同作者的帖子
            if (basePost.getAuthorId() != null && recommendations.size() < RECOMMEND_LIMIT) {
                Page<BbsPost> authorPosts = bbsPostRepository.findUserVisiblePosts(basePost.getAuthorId(), PageRequest.of(0, 10));
                for (BbsPost post : authorPosts) {
                    if (recommendations.size() >= RECOMMEND_LIMIT) break;
                    if (!excludePostIds.contains(post.getId()) && isPostVisible(post, userId)) {
                        recommendations.add(post);
                        excludePostIds.add(post.getId());
                    }
                }
            }
        }

        if (recommendations.isEmpty()) {
            return getPopularPostsAsFallback();
        }

        return convertPostsToMaps(recommendations);
    }

    private List<Map<String, Object>> getPopularPostsAsFallback() {
        List<BbsPost> hotPosts = bbsPostRepository.findTop20ByStatusOrderByLikeCountDesc(1);
        List<BbsPost> recommendations = hotPosts.subList(0, Math.min(RECOMMEND_LIMIT, hotPosts.size()));
        return convertPostsToMaps(recommendations);
    }

    private boolean isPostVisible(BbsPost post, Integer userId) {
        if (post.getStatus() != 1) {
            return false;
        }
        if ("pass".equals(post.getModerationStatus())) {
            return true;
        }
        if (post.getAuthorId() != null && post.getAuthorId().equals(userId.longValue())) {
            return true;
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getUserType() != null) {
                String typeName = user.getUserType().getName();
                if ("ROLE_ADMIN".equals(typeName) || "ROLE_SUPER".equals(typeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Map<String, Object>> convertPostsToMaps(List<BbsPost> posts) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (BbsPost post : posts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("authorId", post.getAuthorId());
            map.put("boardId", post.getBoardId());
            map.put("boardName", post.getBoardName());
            map.put("createTime", post.getCreateTime());
            map.put("viewCount", post.getViewCount());
            map.put("likeCount", post.getLikeCount());
            map.put("commentCount", post.getCommentCount());
            map.put("isTop", post.getIsTop());
            map.put("isFeatured", post.getIsFeatured());
            map.put("images", post.getImageUrls());
            map.put("hasImages", post.getImageUrls() != null && !post.getImageUrls().isBlank());

            // 填充作者信息
            if (post.getAuthorId() != null) {
                Optional<User> authorOpt = userRepository.findById(post.getAuthorId().intValue());
                if (authorOpt.isPresent()) {
                    User author = authorOpt.get();
                    map.put("authorNickname", author.getNickname());
                    int level = author.getLevel() != null ? author.getLevel() : 0;
                    map.put("authorNicknameStyle", levelPrivilegeService.getNicknameStyle(level));
                    String avatar = author.getAvatarUrl();
                    if (avatar == null || avatar.isBlank()) {
                        avatar = "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg";
                    }
                    map.put("authorAvatarUrl", avatar);
                } else {
                    // 即使找不到作者，也设置默认头像和昵称
                    map.put("authorNickname", "未知用户");
                    map.put("authorAvatarUrl", "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg");
                }
            } else {
                // 如果没有作者ID，也设置默认头像
                map.put("authorNickname", "未知用户");
                map.put("authorAvatarUrl", "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg");
            }

            // 填充板块名称
            if (post.getBoardId() != null && post.getBoardName() == null) {
                Optional<BbsBoard> boardOpt = bbsBoardRepository.findById(post.getBoardId());
                if (boardOpt.isPresent()) {
                    map.put("boardName", boardOpt.get().getName());
                }
            }

            result.add(map);
        }
        return result;
    }

    public void recordBrowseHistory(Long postId) {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null || postId == null) {
            return;
        }

        try {
            Optional<BbsUserBrowseHistory> existing = browseHistoryRepository.findByUserIdAndPostId(userId, postId);
            BbsUserBrowseHistory history;
            if (existing.isPresent()) {
                history = existing.get();
                history.setBrowseTime(cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date()));
            } else {
                history = new BbsUserBrowseHistory();
                history.setUserId(userId);
                history.setPostId(postId);
            }
            browseHistoryRepository.save(history);
            log.info("记录浏览历史: userId={}, postId={}", userId, postId);
        } catch (Exception e) {
            log.error("记录浏览历史失败: userId={}, postId={}", userId, postId, e);
        }
    }

    public void clearBrowseHistory() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) {
            return;
        }
        List<BbsUserBrowseHistory> histories = browseHistoryRepository.findByUserIdOrderByBrowseTimeDesc(userId);
        browseHistoryRepository.deleteAll(histories);
        log.info("清除浏览历史: userId={}", userId);
    }

    public Map<String, Object> getBrowseHistoryList(int page, int size) {
        Integer userId = CommonMethod.getPersonId();
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null) {
            result.put("list", Collections.emptyList());
            result.put("total", 0L);
            result.put("totalPages", 0);
            result.put("currentPage", page);
            return result;
        }

        List<BbsUserBrowseHistory> allHistories = browseHistoryRepository.findByUserIdOrderByBrowseTimeDesc(userId);
        
        List<BbsUserBrowseHistory> visibleHistories = new ArrayList<>();
        for (BbsUserBrowseHistory history : allHistories) {
            Optional<BbsPost> postOpt = bbsPostRepository.findById(history.getPostId());
            if (postOpt.isPresent()) {
                BbsPost post = postOpt.get();
                if (isPostVisible(post, userId)) {
                    visibleHistories.add(history);
                }
            }
        }

        int total = visibleHistories.size();
        int totalPages = (int) Math.ceil((double) total / size);
        
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);
        
        List<BbsUserBrowseHistory> pageHistories = (fromIndex < total) 
            ? visibleHistories.subList(fromIndex, toIndex) 
            : Collections.emptyList();
        
        List<Map<String, Object>> list = new ArrayList<>();

        for (BbsUserBrowseHistory history : pageHistories) {
            Optional<BbsPost> postOpt = bbsPostRepository.findById(history.getPostId());
            if (postOpt.isPresent()) {
                BbsPost post = postOpt.get();
                Map<String, Object> map = new HashMap<>();
                map.put("id", post.getId());
                map.put("title", post.getTitle());
                map.put("content", post.getContent());
                map.put("authorId", post.getAuthorId());
                map.put("boardId", post.getBoardId());
                map.put("boardName", post.getBoardName());
                map.put("createTime", post.getCreateTime());
                map.put("viewCount", post.getViewCount());
                map.put("likeCount", post.getLikeCount());
                map.put("commentCount", post.getCommentCount());
                map.put("isTop", post.getIsTop());
                map.put("isFeatured", post.getIsFeatured());
                map.put("images", post.getImageUrls());
                map.put("hasImages", post.getImageUrls() != null && !post.getImageUrls().isBlank());
                map.put("browseTime", history.getBrowseTime());

                if (post.getAuthorId() != null) {
                    Optional<User> authorOpt = userRepository.findById(post.getAuthorId().intValue());
                    if (authorOpt.isPresent()) {
                        User author = authorOpt.get();
                        map.put("authorNickname", author.getNickname());
                        int level = author.getLevel() != null ? author.getLevel() : 0;
                        map.put("authorNicknameStyle", levelPrivilegeService.getNicknameStyle(level));
                        String avatar = author.getAvatarUrl();
                        if (avatar == null || avatar.isBlank()) {
                            avatar = "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg";
                        }
                        map.put("authorAvatarUrl", avatar);
                    } else {
                        // 即使找不到作者，也设置默认头像和昵称
                        map.put("authorNickname", "未知用户");
                        map.put("authorAvatarUrl", "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg");
                    }
                } else {
                    // 如果没有作者ID，也设置默认头像
                    map.put("authorNickname", "未知用户");
                    map.put("authorAvatarUrl", "https://img.phb123.com/uploads/allimg/22060G55A40-L.jpeg");
                }

                if (post.getBoardId() != null && post.getBoardName() == null) {
                    Optional<BbsBoard> boardOpt = bbsBoardRepository.findById(post.getBoardId());
                    if (boardOpt.isPresent()) {
                        map.put("boardName", boardOpt.get().getName());
                    }
                }

                list.add(map);
            }
        }

        result.put("list", list);
        result.put("total", total);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        return result;
    }
}
