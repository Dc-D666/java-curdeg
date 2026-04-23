package com.teach.javafx.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class FollowStateManager {
    private static FollowStateManager instance;
    
    private final Map<Long, Boolean> followStates;
    private final Map<Long, Set<Consumer<Boolean>>> listeners;

    private FollowStateManager() {
        this.followStates = new HashMap<>();
        this.listeners = new HashMap<>();
    }

    public static synchronized FollowStateManager getInstance() {
        if (instance == null) {
            instance = new FollowStateManager();
        }
        return instance;
    }

    public void setFollowState(Long userId, Boolean isFollowed) {
        Boolean oldState = followStates.get(userId);
        followStates.put(userId, isFollowed);
        
        if (listeners.containsKey(userId)) {
            for (Consumer<Boolean> listener : listeners.get(userId)) {
                listener.accept(isFollowed);
            }
        }
    }

    public Boolean getFollowState(Long userId) {
        return followStates.get(userId);
    }

    public void registerListener(Long userId, Consumer<Boolean> listener) {
        if (!listeners.containsKey(userId)) {
            listeners.put(userId, new HashSet<>());
        }
        listeners.get(userId).add(listener);
    }

    public void unregisterListener(Long userId, Consumer<Boolean> listener) {
        if (listeners.containsKey(userId)) {
            listeners.get(userId).remove(listener);
        }
    }

    public void clear() {
        followStates.clear();
        listeners.clear();
    }
}
