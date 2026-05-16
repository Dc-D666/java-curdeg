package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.BbsComment;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.services.StatisticsService;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bbs/statistics")
public class StatisticsController {

    private final BbsPostRepository bbsPostRepository;
    private final UserRepository userRepository;
    private final StatisticsService statisticsService;

    public StatisticsController(BbsPostRepository bbsPostRepository, UserRepository userRepository, StatisticsService statisticsService) {
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
        this.statisticsService = statisticsService;
    }

    @GetMapping("/daily-post")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getDailyPostStatistics() {
        List<Object[]> rawData = bbsPostRepository.countDailyPostLast7Days();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            result.add(item);
        }
        
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/hot-post")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getHotPostStatistics() {
        List<BbsPost> posts = bbsPostRepository.findTop10ByStatusOrderByLikeCountDescCommentCountDesc(1);
        
        for (BbsPost post : posts) {
            if (post.getAuthorId() != null) {
                Optional<User> authorOptional = userRepository.findById(post.getAuthorId().intValue());
                if (authorOptional.isPresent()) {
                    post.setAuthorNickname(authorOptional.get().getNickname());
                }
            }
        }
        
        return CommonMethod.getReturnData(posts);
    }

    @GetMapping("/active-user")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getActiveUserStatistics() {
        List<User> users = userRepository.findTop10ByIsBannedOrderByPostCountDescCommentCountDesc(false);
        
        for (User user : users) {
            user.setPassword(null);
        }
        
        return CommonMethod.getReturnData(users);
    }

    @GetMapping("/overview")
    public DataResponse getOverview() {
        Map<String, Object> result = statisticsService.getOverview();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/user-growth")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getUserGrowth(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getUserGrowthTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/user-type")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getUserTypeDistribution() {
        List<Map<String, Object>> result = statisticsService.getUserTypeDistribution();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/active-users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getActiveUsers(@RequestParam(defaultValue = "composite") String sortBy) {
        List<User> result = statisticsService.getActiveUsers(sortBy);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/user-activity")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getUserActivityDistribution() {
        List<Map<String, Object>> result = statisticsService.getUserActivityDistribution();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/banned-users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getBannedUsers() {
        Map<String, Object> result = statisticsService.getBannedUsers();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/post-trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getPostTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getPostTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/board-distribution")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getBoardDistribution() {
        List<Map<String, Object>> result = statisticsService.getBoardDistribution();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/hot-posts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getHotPosts(@RequestParam(defaultValue = "composite") String sortBy) {
        List<BbsPost> result = statisticsService.getHotPosts(sortBy);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/post-status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getPostStatusDistribution() {
        List<Map<String, Object>> result = statisticsService.getPostStatusDistribution();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/image-post-ratio")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getImagePostRatio() {
        List<Map<String, Object>> result = statisticsService.getImagePostRatio();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/comment-trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getCommentTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getCommentTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/like-trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getLikeTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getLikeTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/favorite-trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getFavoriteTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getFavoriteTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/follow-trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getFollowTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getFollowTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/hot-comments")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getHotComments() {
        List<Map<String, Object>> result = statisticsService.getHotComments();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/moderation-overview")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getModerationOverview() {
        Map<String, Object> result = statisticsService.getModerationOverview();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/moderation-trend")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getModerationTrend(@RequestParam(defaultValue = "7") Integer days) {
        List<Map<String, Object>> result = statisticsService.getModerationTrend(days);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/violation-types")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getViolationTypeDistribution() {
        List<Map<String, Object>> result = statisticsService.getViolationTypeDistribution();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/report-statistics")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER')")
    public DataResponse getReportStatistics() {
        Map<String, Object> result = statisticsService.getReportStatistics();
        return CommonMethod.getReturnData(result);
    }
}
