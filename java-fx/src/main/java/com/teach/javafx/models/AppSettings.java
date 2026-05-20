package com.teach.javafx.models;

public class AppSettings {
    private String theme;
    private String fontSize;
    private boolean notificationSound;
    private boolean postNotification;
    private boolean commentNotification;
    private boolean likeNotification;
    private boolean followNotification;
    private boolean messageNotification;
    private String defaultBoard;
    private String postSort;
    private boolean autoSaveDraft;

    public AppSettings() {
        this.theme = "默认主题（浅色）";
        this.fontSize = "中（默认）";
        this.notificationSound = true;
        this.postNotification = true;
        this.commentNotification = true;
        this.likeNotification = true;
        this.followNotification = true;
        this.messageNotification = true;
        this.defaultBoard = "学习交流";
        this.postSort = "最新发布";
        this.autoSaveDraft = true;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isNotificationSound() {
        return notificationSound;
    }

    public void setNotificationSound(boolean notificationSound) {
        this.notificationSound = notificationSound;
    }

    public boolean isPostNotification() {
        return postNotification;
    }

    public void setPostNotification(boolean postNotification) {
        this.postNotification = postNotification;
    }

    public boolean isCommentNotification() {
        return commentNotification;
    }

    public void setCommentNotification(boolean commentNotification) {
        this.commentNotification = commentNotification;
    }

    public boolean isLikeNotification() {
        return likeNotification;
    }

    public void setLikeNotification(boolean likeNotification) {
        this.likeNotification = likeNotification;
    }

    public boolean isFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(boolean followNotification) {
        this.followNotification = followNotification;
    }

    public boolean isMessageNotification() {
        return messageNotification;
    }

    public void setMessageNotification(boolean messageNotification) {
        this.messageNotification = messageNotification;
    }

    public String getDefaultBoard() {
        return defaultBoard;
    }

    public void setDefaultBoard(String defaultBoard) {
        this.defaultBoard = defaultBoard;
    }

    public String getPostSort() {
        return postSort;
    }

    public void setPostSort(String postSort) {
        this.postSort = postSort;
    }

    public boolean isAutoSaveDraft() {
        return autoSaveDraft;
    }

    public void setAutoSaveDraft(boolean autoSaveDraft) {
        this.autoSaveDraft = autoSaveDraft;
    }
}
