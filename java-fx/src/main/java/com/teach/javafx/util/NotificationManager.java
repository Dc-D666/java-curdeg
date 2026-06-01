package com.teach.javafx.util;

import com.teach.javafx.models.AppSettings;

public class NotificationManager {

    public static boolean shouldNotify(String type) {
        AppSettings settings = SettingsManager.getCurrentSettings();
        
        switch (type) {
            case "post":
                return settings.isPostNotification();
            case "comment":
                return settings.isCommentNotification();
            case "follow":
                return settings.isFollowNotification();
            case "like":
                return settings.isLikeNotification();
            default:
                return true;
        }
    }

    public static String getDefaultBoard() {
        return SettingsManager.getCurrentSettings().getDefaultBoard();
    }

    public static String getDefaultSort() {
        return SettingsManager.getCurrentSettings().getPostSort();
    }
}
