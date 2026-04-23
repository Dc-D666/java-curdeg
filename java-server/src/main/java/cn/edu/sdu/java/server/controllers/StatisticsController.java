package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.BbsPost;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.BbsPostRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
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

    public StatisticsController(BbsPostRepository bbsPostRepository, UserRepository userRepository) {
        this.bbsPostRepository = bbsPostRepository;
        this.userRepository = userRepository;
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
}
