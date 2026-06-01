package com.teach.javafx.util;

import com.teach.javafx.models.AppSettings;

import java.util.prefs.Preferences;

public class SettingsManager {
    private static final String PREFS_NODE = "com.teach.javafx.settings";
    private static final String KEY_FONT_SIZE = "fontSize";
    private static final String KEY_POST_NOTIFICATION = "postNotification";
    private static final String KEY_COMMENT_NOTIFICATION = "commentNotification";
    private static final String KEY_LIKE_NOTIFICATION = "likeNotification";
    private static final String KEY_FOLLOW_NOTIFICATION = "followNotification";
    private static final String KEY_DEFAULT_BOARD = "defaultBoard";
    private static final String KEY_POST_SORT = "postSort";

    private static Preferences prefs;
    private static AppSettings currentSettings;

    static {
        prefs = Preferences.userRoot().node(PREFS_NODE);
        loadSettings();
    }

    public static AppSettings loadSettings() {
        if (currentSettings == null) {
            currentSettings = new AppSettings();
        }

        currentSettings.setFontSize(prefs.get(KEY_FONT_SIZE, currentSettings.getFontSize()));
        currentSettings.setPostNotification(prefs.getBoolean(KEY_POST_NOTIFICATION, currentSettings.isPostNotification()));
        currentSettings.setCommentNotification(prefs.getBoolean(KEY_COMMENT_NOTIFICATION, currentSettings.isCommentNotification()));
        currentSettings.setLikeNotification(prefs.getBoolean(KEY_LIKE_NOTIFICATION, currentSettings.isLikeNotification()));
        currentSettings.setFollowNotification(prefs.getBoolean(KEY_FOLLOW_NOTIFICATION, currentSettings.isFollowNotification()));
        currentSettings.setDefaultBoard(prefs.get(KEY_DEFAULT_BOARD, currentSettings.getDefaultBoard()));
        currentSettings.setPostSort(prefs.get(KEY_POST_SORT, currentSettings.getPostSort()));

        return currentSettings;
    }

    public static void saveSettings(AppSettings settings) {
        currentSettings = settings;

        prefs.put(KEY_FONT_SIZE, settings.getFontSize());
        prefs.putBoolean(KEY_POST_NOTIFICATION, settings.isPostNotification());
        prefs.putBoolean(KEY_COMMENT_NOTIFICATION, settings.isCommentNotification());
        prefs.putBoolean(KEY_LIKE_NOTIFICATION, settings.isLikeNotification());
        prefs.putBoolean(KEY_FOLLOW_NOTIFICATION, settings.isFollowNotification());
        prefs.put(KEY_DEFAULT_BOARD, settings.getDefaultBoard());
        prefs.put(KEY_POST_SORT, settings.getPostSort());
    }

    public static AppSettings getCurrentSettings() {
        if (currentSettings == null) {
            loadSettings();
        }
        return currentSettings;
    }

    public static void clearCache() {
        prefs.remove(KEY_FONT_SIZE);
        prefs.remove(KEY_POST_NOTIFICATION);
        prefs.remove(KEY_COMMENT_NOTIFICATION);
        prefs.remove(KEY_LIKE_NOTIFICATION);
        prefs.remove(KEY_FOLLOW_NOTIFICATION);
        prefs.remove(KEY_DEFAULT_BOARD);
        prefs.remove(KEY_POST_SORT);
        currentSettings = new AppSettings();
    }
}
