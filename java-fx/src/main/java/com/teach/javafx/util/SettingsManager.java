package com.teach.javafx.util;

import com.teach.javafx.models.AppSettings;

import java.util.prefs.Preferences;

public class SettingsManager {
    private static final String PREFS_NODE = "com.teach.javafx.settings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_FONT_SIZE = "fontSize";
    private static final String KEY_NOTIFICATION_SOUND = "notificationSound";
    private static final String KEY_POST_NOTIFICATION = "postNotification";
    private static final String KEY_COMMENT_NOTIFICATION = "commentNotification";
    private static final String KEY_LIKE_NOTIFICATION = "likeNotification";
    private static final String KEY_FOLLOW_NOTIFICATION = "followNotification";
    private static final String KEY_MESSAGE_NOTIFICATION = "messageNotification";
    private static final String KEY_DEFAULT_BOARD = "defaultBoard";
    private static final String KEY_POST_SORT = "postSort";
    private static final String KEY_AUTO_SAVE_DRAFT = "autoSaveDraft";

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

        currentSettings.setTheme(prefs.get(KEY_THEME, currentSettings.getTheme()));
        currentSettings.setFontSize(prefs.get(KEY_FONT_SIZE, currentSettings.getFontSize()));
        currentSettings.setNotificationSound(prefs.getBoolean(KEY_NOTIFICATION_SOUND, currentSettings.isNotificationSound()));
        currentSettings.setPostNotification(prefs.getBoolean(KEY_POST_NOTIFICATION, currentSettings.isPostNotification()));
        currentSettings.setCommentNotification(prefs.getBoolean(KEY_COMMENT_NOTIFICATION, currentSettings.isCommentNotification()));
        currentSettings.setLikeNotification(prefs.getBoolean(KEY_LIKE_NOTIFICATION, currentSettings.isLikeNotification()));
        currentSettings.setFollowNotification(prefs.getBoolean(KEY_FOLLOW_NOTIFICATION, currentSettings.isFollowNotification()));
        currentSettings.setMessageNotification(prefs.getBoolean(KEY_MESSAGE_NOTIFICATION, currentSettings.isMessageNotification()));
        currentSettings.setDefaultBoard(prefs.get(KEY_DEFAULT_BOARD, currentSettings.getDefaultBoard()));
        currentSettings.setPostSort(prefs.get(KEY_POST_SORT, currentSettings.getPostSort()));
        currentSettings.setAutoSaveDraft(prefs.getBoolean(KEY_AUTO_SAVE_DRAFT, currentSettings.isAutoSaveDraft()));

        return currentSettings;
    }

    public static void saveSettings(AppSettings settings) {
        currentSettings = settings;

        prefs.put(KEY_THEME, settings.getTheme());
        prefs.put(KEY_FONT_SIZE, settings.getFontSize());
        prefs.putBoolean(KEY_NOTIFICATION_SOUND, settings.isNotificationSound());
        prefs.putBoolean(KEY_POST_NOTIFICATION, settings.isPostNotification());
        prefs.putBoolean(KEY_COMMENT_NOTIFICATION, settings.isCommentNotification());
        prefs.putBoolean(KEY_LIKE_NOTIFICATION, settings.isLikeNotification());
        prefs.putBoolean(KEY_FOLLOW_NOTIFICATION, settings.isFollowNotification());
        prefs.putBoolean(KEY_MESSAGE_NOTIFICATION, settings.isMessageNotification());
        prefs.put(KEY_DEFAULT_BOARD, settings.getDefaultBoard());
        prefs.put(KEY_POST_SORT, settings.getPostSort());
        prefs.putBoolean(KEY_AUTO_SAVE_DRAFT, settings.isAutoSaveDraft());
    }

    public static AppSettings getCurrentSettings() {
        if (currentSettings == null) {
            loadSettings();
        }
        return currentSettings;
    }

    public static void clearCache() {
        prefs.remove(KEY_THEME);
        prefs.remove(KEY_FONT_SIZE);
        prefs.remove(KEY_NOTIFICATION_SOUND);
        prefs.remove(KEY_POST_NOTIFICATION);
        prefs.remove(KEY_COMMENT_NOTIFICATION);
        prefs.remove(KEY_LIKE_NOTIFICATION);
        prefs.remove(KEY_FOLLOW_NOTIFICATION);
        prefs.remove(KEY_MESSAGE_NOTIFICATION);
        prefs.remove(KEY_DEFAULT_BOARD);
        prefs.remove(KEY_POST_SORT);
        prefs.remove(KEY_AUTO_SAVE_DRAFT);
        currentSettings = new AppSettings();
    }
}
