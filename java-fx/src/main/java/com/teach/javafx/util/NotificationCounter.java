package com.teach.javafx.util;

import com.teach.javafx.request.HttpRequestUtil;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.Map;

public class NotificationCounter {

    private static long notificationCount = 0;
    private static long messageCount = 0;
    private static ScheduledService<Void> refreshService;
    private static Runnable onCountChangedCallback;

    public static long getNotificationCount() {
        return notificationCount;
    }

    public static long getMessageCount() {
        return messageCount;
    }

    public static void setOnCountChangedCallback(Runnable callback) {
        onCountChangedCallback = callback;
    }

    public static void refreshCounts() {
        try {
            Long notification = HttpRequestUtil.getUnreadNotificationCount();
            if (notification != null) {
                notificationCount = notification;
            }

            Map<String, Object> data = HttpRequestUtil.getUnreadCount();
            if (data != null && data.containsKey("totalUnread")) {
                Object count = data.get("totalUnread");
                if (count instanceof Number) {
                    messageCount = ((Number) count).longValue();
                }
            }
            
            System.out.println("Notification count: " + notificationCount + ", Message count: " + messageCount);
            
            if (onCountChangedCallback != null) {
                Platform.runLater(onCountChangedCallback);
            }
        } catch (Exception e) {
            System.err.println("Error refreshing notification counts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void startAutoRefresh() {
        if (refreshService != null && refreshService.isRunning()) {
            refreshService.cancel();
        }

        refreshService = new ScheduledService<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() {
                        refreshCounts();
                        return null;
                    }
                };
            }
        };

        refreshService.setPeriod(Duration.seconds(30));
        refreshService.setDelay(Duration.seconds(2));
        refreshService.start();
    }

    public static void stopAutoRefresh() {
        if (refreshService != null) {
            refreshService.cancel();
            refreshService = null;
        }
    }
}
