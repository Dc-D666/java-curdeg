package com.teach.javafx.util;

import com.teach.javafx.request.HttpRequestUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户等级特权缓存工具类
 * 缓存当前用户的等级特权信息，避免频繁请求后端
 */
public class PrivilegeCache {
    private static final PrivilegeCache INSTANCE = new PrivilegeCache();
    private Map<String, Object> privileges;
    private long lastUpdateTime = 0;
    private Map<String, Object> aiUsage;
    private long lastAiUsageTime = 0;
    private static final long CACHE_DURATION_MS = 5 * 60 * 1000; // 5分钟缓存
    private static final long AI_USAGE_CACHE_DURATION_MS = 30 * 1000; // 30秒缓存

    private PrivilegeCache() {}

    public static PrivilegeCache getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<String, Object> getPrivileges() {
        long now = System.currentTimeMillis();
        if (privileges == null || (now - lastUpdateTime) > CACHE_DURATION_MS) {
            refreshPrivileges();
        }
        return privileges;
    }

    @SuppressWarnings("unchecked")
    public synchronized void refreshPrivileges() {
        Map<String, Object> data = HttpRequestUtil.getMyPrivileges();
        if (data != null && data.containsKey("privileges")) {
            privileges = (Map<String, Object>) data.get("privileges");
        } else {
            privileges = new ConcurrentHashMap<>();
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    public synchronized void clear() {
        privileges = null;
        lastUpdateTime = 0;
        aiUsage = null;
        lastAiUsageTime = 0;
    }

    @SuppressWarnings("unchecked")
    private synchronized Map<String, Object> getAiUsage() {
        long now = System.currentTimeMillis();
        if (aiUsage == null || (now - lastAiUsageTime) > AI_USAGE_CACHE_DURATION_MS) {
            Map<String, Object> data = HttpRequestUtil.getAiUsage();
            if (data != null) {
                aiUsage = data;
            } else {
                aiUsage = new ConcurrentHashMap<>();
            }
            lastAiUsageTime = now;
        }
        return aiUsage;
    }

    public boolean canPost() {
        Map<String, Object> privs = getPrivileges();
        if (privs == null) return true;
        Object canPost = privs.get("canPost");
        return !(canPost instanceof Boolean && Boolean.FALSE.equals(canPost));
    }

    public int getDailyPostLimit() {
        Map<String, Object> privs = getPrivileges();
        if (privs == null) return 0;
        Object limit = privs.get("canPostDaily");
        return limit instanceof Number ? ((Number) limit).intValue() : 0;
    }

    public int getAiSearchLimit() {
        Map<String, Object> privs = getPrivileges();
        if (privs == null) return 0;
        Object limit = privs.get("aiSearchLimit");
        return limit instanceof Number ? ((Number) limit).intValue() : 0;
    }

    public int getAiImageLimit() {
        Map<String, Object> privs = getPrivileges();
        if (privs == null) return 0;
        Object limit = privs.get("aiImageLimit");
        return limit instanceof Number ? ((Number) limit).intValue() : 0;
    }

    public String getNicknameStyle() {
        Map<String, Object> privs = getPrivileges();
        if (privs == null) return null;
        Object style = privs.get("nicknameStyle");
        return style instanceof String ? (String) style : null;
    }

    public boolean canUseAiSummary() {
        Map<String, Object> privs = getPrivileges();
        if (privs == null) return false;
        Object can = privs.get("aiSummary");
        return can instanceof Boolean && Boolean.TRUE.equals(can);
    }

    public int getAiSearchRemaining() {
        Map<String, Object> usage = getAiUsage();
        if (usage != null && !usage.isEmpty()) {
            Object remaining = usage.get("aiSearchRemaining");
            if (remaining instanceof Number) {
                return ((Number) remaining).intValue();
            }
        }
        return getAiSearchLimit();
    }

    public int getAiImageRemaining() {
        Map<String, Object> usage = getAiUsage();
        if (usage != null && !usage.isEmpty()) {
            Object remaining = usage.get("aiImageRemaining");
            if (remaining instanceof Number) {
                return ((Number) remaining).intValue();
            }
        }
        return getAiImageLimit();
    }

    public String getPostRestrictionMessage() {
        if (!canPost()) {
            return "当前等级禁止发帖，请提升等级后再试";
        }
        return null;
    }
}
