package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

public class OverviewData {
    @SerializedName(value = "userCount", alternate = "totalUsers")
    private Integer userCount;
    
    @SerializedName(value = "todayNewUsers", alternate = "usersToday")
    private Integer todayNewUsers;
    
    @SerializedName(value = "monthlyActiveUsers", alternate = "activeUsersThisMonth")
    private Integer monthlyActiveUsers;
    
    @SerializedName(value = "postCount", alternate = "totalPosts")
    private Integer postCount;
    
    @SerializedName(value = "todayNewPosts", alternate = "postsToday")
    private Integer todayNewPosts;
    
    @SerializedName(value = "commentCount", alternate = "totalComments")
    private Integer commentCount;
    
    @SerializedName(value = "todayNewComments", alternate = "commentsToday")
    private Integer todayNewComments;
    
    @SerializedName(value = "pendingModerationCount", alternate = "pendingContentCount")
    private Integer pendingModerationCount;
    
    @SerializedName(value = "likeCount", alternate = "totalLikes")
    private Integer likeCount;
    
    @SerializedName(value = "favoriteCount", alternate = "totalFavorites")
    private Integer favoriteCount;
    
    @SerializedName(value = "followCount", alternate = "totalFollows")
    private Integer followCount;

    public OverviewData() {
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getTodayNewUsers() {
        return todayNewUsers;
    }

    public void setTodayNewUsers(Integer todayNewUsers) {
        this.todayNewUsers = todayNewUsers;
    }

    public Integer getMonthlyActiveUsers() {
        return monthlyActiveUsers;
    }

    public void setMonthlyActiveUsers(Integer monthlyActiveUsers) {
        this.monthlyActiveUsers = monthlyActiveUsers;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getTodayNewPosts() {
        return todayNewPosts;
    }

    public void setTodayNewPosts(Integer todayNewPosts) {
        this.todayNewPosts = todayNewPosts;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getTodayNewComments() {
        return todayNewComments;
    }

    public void setTodayNewComments(Integer todayNewComments) {
        this.todayNewComments = todayNewComments;
    }

    public Integer getPendingModerationCount() {
        return pendingModerationCount;
    }

    public void setPendingModerationCount(Integer pendingModerationCount) {
        this.pendingModerationCount = pendingModerationCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getFollowCount() {
        return followCount;
    }

    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }
}
