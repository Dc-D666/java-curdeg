package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.models.LevelConfig;
import cn.edu.sdu.java.server.models.PointRecord;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.models.DailyLimit;
import cn.edu.sdu.java.server.repositorys.DailyLimitRepository;
import cn.edu.sdu.java.server.services.BbsUserService;
import cn.edu.sdu.java.server.services.LevelPrivilegeService;
import cn.edu.sdu.java.server.services.PointService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bbs/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final LevelPrivilegeService levelPrivilegeService;
    private final BbsUserService bbsUserService;
    private final DailyLimitRepository dailyLimitRepository;

    @GetMapping("/me")
    public DataResponse getMyPoints() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) return CommonMethod.getReturnMessageError("未登录");

        User user = bbsUserService.getUserById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("points", user.getPoints());
        result.put("level", user.getLevel());

        LevelConfig config = levelPrivilegeService.getLevelConfig(user.getLevel());
        if (config != null) {
            result.put("levelName", config.getLevelName());
            result.put("iconPath", config.getIconPath());
            result.put("minPoints", config.getMinPoints());
        }

        Integer rank = pointService.getUserRank(userId);
        result.put("rank", rank);

        // 计算下一等级所需积分
        LevelConfig nextConfig = levelPrivilegeService.getNextLevelConfig(user.getLevel());
        if (nextConfig != null) {
            result.put("nextLevelPoints", nextConfig.getMinPoints());
            result.put("currentLevelPoints", config != null ? config.getMinPoints() : 0);
            result.put("pointsToNext", nextConfig.getMinPoints() - user.getPoints());
        } else {
            result.put("nextLevelPoints", 0);
            result.put("currentLevelPoints", config != null ? config.getMinPoints() : 0);
            result.put("pointsToNext", 0);
        }

        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/me/history")
    public DataResponse getMyPointHistory(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "20") int pageSize) {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) return CommonMethod.getReturnMessageError("未登录");

        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Page<PointRecord> page = pointService.getPointHistory(userId, pageable);
        return CommonMethod.getReturnData(page);
    }

    @GetMapping("/rank")
    public DataResponse getPointRank(@RequestParam(defaultValue = "1") int pageNum,
                                     @RequestParam(defaultValue = "50") int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        List<Map<String, Object>> users = pointService.getPointRank(pageable);
        Map<String, Object> result = new HashMap<>();
        result.put("content", users);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/rank/weekly")
    public DataResponse getWeeklyPointRank() {
        List<Map<String, Object>> result = pointService.getWeeklyPointRank();
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/level/config")
    public DataResponse getLevelConfig() {
        // Return all level configs
        List<LevelConfig> configs = levelPrivilegeService.getAllLevelConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("levels", configs);
        return CommonMethod.getReturnData(result);
    }

    @GetMapping("/level/my-privileges")
    public DataResponse getMyPrivileges() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) return CommonMethod.getReturnMessageError("未登录");

        User user = bbsUserService.getUserById(userId);
        int level = user.getLevel();

        Map<String, Object> data = new HashMap<>();
        data.put("level", level);
        data.put("points", user.getPoints());

        LevelConfig config = levelPrivilegeService.getLevelConfig(level);
        if (config != null) {
            data.put("levelName", config.getLevelName());
            data.put("iconPath", config.getIconPath());
        }

        LevelConfig nextConfig = levelPrivilegeService.getNextLevelConfig(level);
        if (nextConfig != null) {
            data.put("nextLevelPoints", nextConfig.getMinPoints());
            data.put("pointsToNext", nextConfig.getMinPoints() - user.getPoints());
        } else {
            data.put("nextLevelPoints", 0);
            data.put("pointsToNext", 0);
        }

        Map<String, Object> privileges = new HashMap<>();
        privileges.put("canPost", levelPrivilegeService.canPost(level));
        privileges.put("canPostDaily", levelPrivilegeService.getDailyPostLimit(level));
        privileges.put("aiSearchLimit", levelPrivilegeService.getAiSearchLimit(level));
        privileges.put("aiSummary", levelPrivilegeService.canUseAiSummary(level));
        privileges.put("aiImageLimit", levelPrivilegeService.getAiImageLimit(level));
        privileges.put("attachmentSize", levelPrivilegeService.getAttachSizeLimit(level));
        privileges.put("animatedAvatar", levelPrivilegeService.canUseGifAvatar(level));
        privileges.put("canDeleteComment", levelPrivilegeService.canDeleteOthersComment(level));
        privileges.put("canViewLikers", levelPrivilegeService.canViewLikers(level));
        privileges.put("privateMessageLimit", levelPrivilegeService.getPrivateMessageLimit(level));
        privileges.put("nicknameStyle", levelPrivilegeService.getNicknameStyle(level));
        privileges.put("priorityRecommend", levelPrivilegeService.hasContentPriority(level));
        privileges.put("postAudit", levelPrivilegeService.canSkipModeration(level) ? "none" : "ai_post");
        privileges.put("storeDiscount", levelPrivilegeService.getStoreDiscount(level).multiply(BigDecimal.valueOf(10)).intValue());
        privileges.put("canApplyAdmin", levelPrivilegeService.canApplyAdmin(level));

        data.put("privileges", privileges);

        return CommonMethod.getReturnData(data);
    }

    @GetMapping("/rules")
    public DataResponse getPointRules() {
        return CommonMethod.getReturnData(pointService.getAllPointRules());
    }

    @GetMapping("/ai-usage")
    public DataResponse getAiUsage() {
        Integer userId = CommonMethod.getPersonId();
        if (userId == null) return CommonMethod.getReturnMessageError("未登录");

        User user = bbsUserService.getUserById(userId);
        int level = user.getLevel();
        LocalDate today = LocalDate.now();

        Map<String, Object> result = new HashMap<>();

        // AI搜索剩余次数
        int aiSearchLimit = levelPrivilegeService.getAiSearchLimit(level);
        int aiSearchUsed = 0;
        if (aiSearchLimit > 0) {
            Optional<DailyLimit> opt = dailyLimitRepository.findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(
                    userId, "AI_SEARCH", "search", today);
            aiSearchUsed = opt.map(DailyLimit::getUsedCount).orElse(0);
        }
        result.put("aiSearchLimit", aiSearchLimit);
        result.put("aiSearchUsed", aiSearchUsed);
        result.put("aiSearchRemaining", Math.max(0, aiSearchLimit - aiSearchUsed));

        // AI配图剩余次数
        int aiImageLimit = levelPrivilegeService.getAiImageLimit(level);
        int aiImageUsed = 0;
        if (aiImageLimit > 0) {
            Optional<DailyLimit> opt = dailyLimitRepository.findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(
                    userId, "AI_IMAGE", "image", today);
            aiImageUsed = opt.map(DailyLimit::getUsedCount).orElse(0);
        }
        result.put("aiImageLimit", aiImageLimit);
        result.put("aiImageUsed", aiImageUsed);
        result.put("aiImageRemaining", Math.max(0, aiImageLimit - aiImageUsed));

        return CommonMethod.getReturnData(result);
    }
}
