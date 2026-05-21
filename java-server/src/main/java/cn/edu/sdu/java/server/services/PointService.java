package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.repositorys.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRuleRepository pointRuleRepository;
    private final PointRecordRepository pointRecordRepository;
    private final LevelConfigRepository levelConfigRepository;
    private final DailyLimitRepository dailyLimitRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final LevelPrivilegeService levelPrivilegeService;

    private Map<String, PointRule> ruleMap = new HashMap<>();

    @PostConstruct
    public void loadPointRules() {
        List<PointRule> rules = pointRuleRepository.findByEnabledOrderByIdAsc(true);
        for (PointRule rule : rules) {
            ruleMap.put(rule.getRuleCode(), rule);
        }
        log.info("Loaded {} point rules", ruleMap.size());
    }

    public PointRule getRule(String ruleCode) {
        return ruleMap.get(ruleCode);
    }

    @Transactional
    public void addPoints(Integer userId, String ruleCode, String description, Long relatedId, String relatedType) {
        PointRule rule = ruleMap.get(ruleCode);
        if (rule == null || !rule.getEnabled()) {
            log.warn("Rule not found or disabled: {}", ruleCode);
            return;
        }
        doPointChange(userId, ruleCode, rule.getPointsChange(), description, relatedId, relatedType, rule.getDailyLimit());
    }

    @Transactional
    public void deductPoints(Integer userId, String ruleCode, String description, Long relatedId, String relatedType) {
        PointRule rule = ruleMap.get(ruleCode);
        if (rule == null || !rule.getEnabled()) {
            log.warn("Rule not found or disabled: {}", ruleCode);
            return;
        }
        doPointChange(userId, ruleCode, -Math.abs(rule.getPointsChange()), description, relatedId, relatedType, null);
    }

    private void doPointChange(Integer userId, String ruleCode, int pointsChange, String description,
                               Long relatedId, String relatedType, Integer dailyLimit) {
        // Check daily limit
        if (dailyLimit != null && dailyLimit > 0) {
            if (!checkAndIncrementDailyLimit(userId, ruleCode, dailyLimit)) {
                log.debug("Daily limit reached for user {} rule {}", userId, ruleCode);
                return;
            }
        }

        User user = userRepository.findByPersonPersonId(userId).orElse(null);
        if (user == null) return;

        int newPoints = user.getPoints() + pointsChange;
        if (newPoints < 0) newPoints = 0;

        userRepository.updatePointsOnly(userId, newPoints);

        // Record point change
        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setRuleCode(ruleCode);
        record.setPointsChange(pointsChange);
        record.setDescription(description);
        record.setRelatedId(relatedId);
        record.setRelatedType(relatedType);
        record.setBalanceAfter(newPoints);
        pointRecordRepository.save(record);

        // Auto calculate level
        Integer newLevel = calculateLevel(newPoints);
        if (newLevel > user.getLevel()) {
            userRepository.updateLevel(userId, newLevel);
            log.info("User {} leveled up from {} to {}", userId, user.getLevel(), newLevel);
        } else if (newLevel < user.getLevel()) {
            LocalDate now = LocalDate.now();
            if (user.getLevelProtectedUntil() == null || !user.getLevelProtectedUntil().isAfter(now)) {
                userRepository.updateLevel(userId, newLevel);
                userRepository.updateLevelProtectedUntil(userId, now.plusDays(7));
                log.info("User {} leveled down from {} to {} (protected until {})",
                        userId, user.getLevel(), newLevel, now.plusDays(7));
            } else {
                log.info("User {} level downgrade from {} to {} blocked by protection until {}",
                        userId, user.getLevel(), newLevel, user.getLevelProtectedUntil());
            }
        }
    }

    private boolean checkAndIncrementDailyLimit(Integer userId, String ruleCode, int maxCount) {
        LocalDate today = LocalDate.now();
        Optional<DailyLimit> opt = dailyLimitRepository.findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(
                userId, "POINT_RULE", ruleCode, today);
        if (opt.isPresent()) {
            DailyLimit limit = opt.get();
            if (limit.getUsedCount() >= maxCount) {
                return false;
            }
            limit.setUsedCount(limit.getUsedCount() + 1);
            dailyLimitRepository.save(limit);
            return true;
        } else {
            DailyLimit limit = new DailyLimit();
            limit.setUserId(userId);
            limit.setLimitType("POINT_RULE");
            limit.setLimitKey(ruleCode);
            limit.setRecordDate(today);
            limit.setUsedCount(1);
            limit.setMaxCount(maxCount);
            dailyLimitRepository.save(limit);
            return true;
        }
    }

    private Integer calculateLevel(Integer points) {
        return levelConfigRepository.findCurrentLevelByPoints(points, PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst()
                .map(LevelConfig::getId)
                .orElse(0);
    }

    public Integer getUserPoints(Integer userId) {
        return userRepository.findByPersonPersonId(userId)
                .map(User::getPoints)
                .orElse(0);
    }

    public Page<PointRecord> getPointHistory(Integer userId, Pageable pageable) {
        return pointRecordRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    public List<Map<String, Object>> getPointRank(Pageable pageable) {
        List<User> users = userRepository.findAllByOrderByPointsDesc(pageable);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", user.getPerson().getPersonId());
            map.put("nickname", user.getNickname());
            map.put("points", user.getPoints());
            map.put("level", user.getLevel());
            LevelConfig config = levelPrivilegeService.getLevelConfig(user.getLevel());
            if (config != null) {
                map.put("levelName", config.getLevelName());
                map.put("iconPath", config.getIconPath());
            } else {
                map.put("levelName", "Lv." + user.getLevel());
                map.put("iconPath", "");
            }
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getWeeklyPointRank() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        // Get all point records from last 7 days, group by user
        List<PointRecord> records = pointRecordRepository.findByCreateTimeAfter(weekAgo);
        Map<Integer, Integer> userGrowth = new HashMap<>();
        for (PointRecord record : records) {
            if (record.getPointsChange() > 0) {
                userGrowth.merge(record.getUserId(), record.getPointsChange(), Integer::sum);
            }
        }
        // Sort by growth desc
        List<Map<String, Object>> result = new ArrayList<>();
        userGrowth.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(50)
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    userRepository.findByPersonPersonId(entry.getKey()).ifPresent(user -> {
                        item.put("userId", user.getPersonId());
                        item.put("nickname", user.getNickname());
                        item.put("avatarUrl", user.getAvatarUrl());
                        item.put("level", user.getLevel());
                        item.put("points", user.getPoints());
                        item.put("weeklyGrowth", entry.getValue());
                        LevelConfig config = levelPrivilegeService.getLevelConfig(user.getLevel());
                        if (config != null) {
                            item.put("levelName", config.getLevelName());
                            item.put("iconPath", config.getIconPath());
                        } else {
                            item.put("levelName", "Lv." + user.getLevel());
                            item.put("iconPath", "");
                        }
                    });
                    if (!item.isEmpty()) result.add(item);
                });
        return result;
    }

    public Integer getWeeklyGrowth(Integer userId) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<PointRecord> records = pointRecordRepository.findByUserIdAndCreateTimeAfter(userId, weekAgo);
        int growth = 0;
        for (PointRecord record : records) {
            if (record.getPointsChange() > 0) {
                growth += record.getPointsChange();
            }
        }
        return growth;
    }

    public Integer getUserRank(Integer userId) {
        User user = userRepository.findByPersonPersonId(userId).orElse(null);
        if (user == null) return 0;
        return userRepository.findUserRankByPoints(user.getPoints());
    }

    public List<PointRule> getAllPointRules() {
        return pointRuleRepository.findByEnabledOrderByIdAsc(true);
    }

    public boolean checkDailyLimit(Integer userId, String ruleCode) {
        PointRule rule = ruleMap.get(ruleCode);
        if (rule == null || rule.getDailyLimit() == null) return true;
        LocalDate today = LocalDate.now();
        Optional<DailyLimit> opt = dailyLimitRepository.findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(
                userId, "POINT_RULE", ruleCode, today);
        return opt.map(limit -> limit.getUsedCount() < rule.getDailyLimit()).orElse(true);
    }
}
