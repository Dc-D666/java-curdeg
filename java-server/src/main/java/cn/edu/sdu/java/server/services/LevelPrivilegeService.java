package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.AiUsageRecord;
import cn.edu.sdu.java.server.models.DailyLimit;
import cn.edu.sdu.java.server.models.LevelConfig;
import cn.edu.sdu.java.server.repositorys.AiUsageRecordRepository;
import cn.edu.sdu.java.server.repositorys.DailyLimitRepository;
import cn.edu.sdu.java.server.repositorys.LevelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelPrivilegeService {

    private final LevelConfigRepository levelConfigRepository;
    private final DailyLimitRepository dailyLimitRepository;
    private final AiUsageRecordRepository aiUsageRecordRepository;
    private final ObjectMapper objectMapper;

    private final Map<Integer, JsonNode> privilegeCache = new ConcurrentHashMap<>();

    private JsonNode getPrivileges(Integer level) {
        return privilegeCache.computeIfAbsent(level, k -> {
            Optional<LevelConfig> config = levelConfigRepository.findById(k);
            if (config.isPresent() && config.get().getPrivileges() != null) {
                try {
                    return objectMapper.readTree(config.get().getPrivileges());
                } catch (Exception e) {
                    log.error("Failed to parse privileges for level {}", k, e);
                }
            }
            return objectMapper.createObjectNode();
        });
    }

    public LevelConfig getLevelConfig(Integer level) {
        return levelConfigRepository.findById(level).orElse(null);
    }

    public List<LevelConfig> getAllLevelConfigs() {
        return levelConfigRepository.findAllOrdered();
    }

    public LevelConfig getNextLevelConfig(Integer currentLevel) {
        return levelConfigRepository.findFirstByIdGreaterThanOrderByIdAsc(currentLevel).orElse(null);
    }

    private boolean getBoolPrivilege(Integer level, String key) {
        JsonNode node = getPrivileges(level);
        return node.has(key) && node.get(key).asBoolean();
    }

    private int getIntPrivilege(Integer level, String key, int defaultValue) {
        JsonNode node = getPrivileges(level);
        return node.has(key) ? node.get(key).asInt(defaultValue) : defaultValue;
    }

    private long getLongPrivilege(Integer level, String key, long defaultValue) {
        JsonNode node = getPrivileges(level);
        return node.has(key) ? node.get(key).asLong(defaultValue) : defaultValue;
    }

    private String getStringPrivilege(Integer level, String key, String defaultValue) {
        JsonNode node = getPrivileges(level);
        return node.has(key) ? node.get(key).asText(defaultValue) : defaultValue;
    }

    public boolean canPost(Integer level) {
        return getBoolPrivilege(level, "canPost");
    }

    public boolean canComment(Integer level) {
        return getBoolPrivilege(level, "canComment");
    }

    public int getDailyPostLimit(Integer level) {
        return getIntPrivilege(level, "dailyPostLimit", 0);
    }

    public int getAiSearchLimit(Integer level) {
        return getIntPrivilege(level, "aiSearch", 0);
    }

    public int getAiImageLimit(Integer level) {
        return getIntPrivilege(level, "aiImage", 0);
    }

    public boolean canUseAiSummary(Integer level) {
        return getBoolPrivilege(level, "aiSummary");
    }

    public boolean canAttach(Integer level) {
        return getBoolPrivilege(level, "canAttach");
    }

    public long getAttachSizeLimit(Integer level) {
        return getLongPrivilege(level, "attachSize", 0);
    }

    public boolean canUseGifAvatar(Integer level) {
        return getBoolPrivilege(level, "canUseGifAvatar");
    }

    public boolean canDeleteOthersComment(Integer level) {
        return getBoolPrivilege(level, "canDeleteOthersComment");
    }

    public boolean canViewLikers(Integer level) {
        return getBoolPrivilege(level, "canViewLikers");
    }

    public int getPrivateMessageLimit(Integer level) {
        return getIntPrivilege(level, "privateMessageLimit", 0);
    }

    public boolean canSkipModeration(Integer level) {
        return getBoolPrivilege(level, "canSkipModeration");
    }

    public String getNicknameStyle(Integer level) {
        return getStringPrivilege(level, "nicknameStyle", "normal");
    }

    public boolean hasContentPriority(Integer level) {
        return getBoolPrivilege(level, "contentPriority");
    }

    public BigDecimal getStoreDiscount(Integer level) {
        JsonNode node = getPrivileges(level);
        if (node.has("storeDiscount")) {
            return BigDecimal.valueOf(node.get("storeDiscount").asDouble());
        }
        return BigDecimal.ONE;
    }

    public boolean canApplyAdmin(Integer level) {
        return getBoolPrivilege(level, "canApplyAdmin");
    }

    public String getLevelName(Integer level) {
        return levelConfigRepository.findById(level)
                .map(LevelConfig::getLevelName)
                .orElse("Lv." + level);
    }

    public Integer getPointsToNextLevel(Integer points) {
        return levelConfigRepository.findNextLevelByPoints(points, PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst()
                .map(next -> next.getMinPoints() - points)
                .orElse(0);
    }

    public boolean checkAiUsageLimit(Integer userId, String aiType, Integer levelLimit) {
        if (levelLimit <= 0) return false;
        LocalDate today = LocalDate.now();

        Optional<AiUsageRecord> aiOpt = aiUsageRecordRepository.findByUserIdAndUsageTypeAndUsedDate(userId, aiType, today);
        if (aiOpt.isPresent()) {
            AiUsageRecord record = aiOpt.get();
            record.setUsedCount(record.getUsedCount() + 1);
            aiUsageRecordRepository.save(record);
        } else {
            AiUsageRecord record = new AiUsageRecord();
            record.setUserId(userId);
            record.setUsageType(aiType);
            record.setUsedDate(today);
            record.setUsedCount(1);
            aiUsageRecordRepository.save(record);
        }

        Optional<DailyLimit> opt = dailyLimitRepository.findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(
                userId, "AI_" + aiType.toUpperCase(), aiType, today);
        if (opt.isPresent()) {
            DailyLimit limit = opt.get();
            if (limit.getUsedCount() >= levelLimit) {
                return false;
            }
            limit.setUsedCount(limit.getUsedCount() + 1);
            dailyLimitRepository.save(limit);
            return true;
        } else {
            DailyLimit limit = new DailyLimit();
            limit.setUserId(userId);
            limit.setLimitType("AI_" + aiType.toUpperCase());
            limit.setLimitKey(aiType);
            limit.setRecordDate(today);
            limit.setUsedCount(1);
            limit.setMaxCount(levelLimit);
            dailyLimitRepository.save(limit);
            return true;
        }
    }

    public boolean checkPostLimit(Integer userId, Integer level) {
        int limit = getDailyPostLimit(level);
        if (limit <= 0) return false;
        if (limit >= 999) return true;
        LocalDate today = LocalDate.now();
        Optional<DailyLimit> opt = dailyLimitRepository.findByUserIdAndLimitTypeAndLimitKeyAndRecordDate(
                userId, "POST_PUBLISH", "POST", today);
        if (opt.isPresent()) {
            DailyLimit daily = opt.get();
            if (daily.getUsedCount() >= limit) {
                return false;
            }
            daily.setUsedCount(daily.getUsedCount() + 1);
            dailyLimitRepository.save(daily);
            return true;
        } else {
            DailyLimit daily = new DailyLimit();
            daily.setUserId(userId);
            daily.setLimitType("POST_PUBLISH");
            daily.setLimitKey("POST");
            daily.setRecordDate(today);
            daily.setUsedCount(1);
            daily.setMaxCount(limit);
            dailyLimitRepository.save(daily);
            return true;
        }
    }
}
