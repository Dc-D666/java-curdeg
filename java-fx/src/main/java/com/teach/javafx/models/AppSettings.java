package com.teach.javafx.models;

public class AppSettings {
    private String fontSize;
    private boolean postNotification;
    private boolean commentNotification;
    private boolean likeNotification;
    private boolean commentLikeNotification;
    private boolean followNotification;
    private String defaultBoard;
    private String postSort;

    public AppSettings() {
        this.fontSize = "中（默认）";
        this.postNotification = true;
        this.commentNotification = true;
        this.likeNotification = true;
        this.commentLikeNotification = true;
        this.followNotification = true;
        this.defaultBoard = "全部";
        this.postSort = "最新发布";
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
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

    public boolean isCommentLikeNotification() {
        return commentLikeNotification;
    }

    public void setCommentLikeNotification(boolean commentLikeNotification) {
        this.commentLikeNotification = commentLikeNotification;
    }

    public boolean isFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(boolean followNotification) {
        this.followNotification = followNotification;
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
}
