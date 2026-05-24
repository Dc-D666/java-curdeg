package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatisticsData {
    @SerializedName(value = "overview", alternate = "overviewData")
    private OverviewData overview;
    
    @SerializedName(value = "userGrowthTrend", alternate = "userTrend")
    private List<TrendData> userGrowthTrend;
    
    @SerializedName(value = "postTrend", alternate = "postGrowthTrend")
    private List<TrendData> postTrend;
    
    @SerializedName(value = "commentTrend", alternate = "commentGrowthTrend")
    private List<TrendData> commentTrend;
    
    @SerializedName(value = "likeTrend", alternate = "likeGrowthTrend")
    private List<TrendData> likeTrend;
    
    @SerializedName(value = "favoriteTrend", alternate = "favoriteGrowthTrend")
    private List<TrendData> favoriteTrend;
    
    @SerializedName(value = "followTrend", alternate = "followGrowthTrend")
    private List<TrendData> followTrend;
    
    @SerializedName(value = "moderationTrend", alternate = "moderationTrendData")
    private List<TrendData> moderationTrend;
    
    @SerializedName(value = "userTypeDistribution", alternate = "userType")
    private List<DistributionData> userTypeDistribution;
    
    @SerializedName(value = "boardDistribution", alternate = "boardPostDistribution")
    private List<DistributionData> boardDistribution;
    
    @SerializedName(value = "postStatusDistribution", alternate = "postStatus")
    private List<DistributionData> postStatusDistribution;
    
    @SerializedName(value = "imagePostDistribution", alternate = "imagePostRate")
    private List<DistributionData> imagePostDistribution;
    
    @SerializedName(value = "violationTypeDistribution", alternate = "violationType")
    private List<DistributionData> violationTypeDistribution;
    
    @SerializedName(value = "reportTypeDistribution", alternate = "reportType")
    private List<DistributionData> reportTypeDistribution;
    
    @SerializedName(value = "userActivityDistribution", alternate = "userActivity")
    private List<DistributionData> userActivityDistribution;
    
    @SerializedName(value = "activeUsers", alternate = "activeUserList")
    private List<User> activeUsers;
    
    @SerializedName(value = "hotPosts", alternate = "hotPostList")
    private List<Post> hotPosts;
    
    @SerializedName(value = "hotComments", alternate = "hotCommentList")
    private List<Comment> hotComments;
    
    @SerializedName(value = "bannedUsers", alternate = "bannedUserList")
    private List<User> bannedUsers;
    
    @SerializedName(value = "moderationOverview", alternate = "moderationStats")
    private ModerationOverview moderationOverview;
    
    @SerializedName(value = "reportOverview", alternate = "reportStats")
    private ReportOverview reportOverview;

    public StatisticsData() {
    }

    public OverviewData getOverview() {
        return overview;
    }

    public void setOverview(OverviewData overview) {
        this.overview = overview;
    }

    public List<TrendData> getUserGrowthTrend() {
        return userGrowthTrend;
    }

    public void setUserGrowthTrend(List<TrendData> userGrowthTrend) {
        this.userGrowthTrend = userGrowthTrend;
    }

    public List<TrendData> getPostTrend() {
        return postTrend;
    }

    public void setPostTrend(List<TrendData> postTrend) {
        this.postTrend = postTrend;
    }

    public List<TrendData> getCommentTrend() {
        return commentTrend;
    }

    public void setCommentTrend(List<TrendData> commentTrend) {
        this.commentTrend = commentTrend;
    }

    public List<TrendData> getLikeTrend() {
        return likeTrend;
    }

    public void setLikeTrend(List<TrendData> likeTrend) {
        this.likeTrend = likeTrend;
    }

    public List<TrendData> getFavoriteTrend() {
        return favoriteTrend;
    }

    public void setFavoriteTrend(List<TrendData> favoriteTrend) {
        this.favoriteTrend = favoriteTrend;
    }

    public List<TrendData> getFollowTrend() {
        return followTrend;
    }

    public void setFollowTrend(List<TrendData> followTrend) {
        this.followTrend = followTrend;
    }

    public List<TrendData> getModerationTrend() {
        return moderationTrend;
    }

    public void setModerationTrend(List<TrendData> moderationTrend) {
        this.moderationTrend = moderationTrend;
    }

    public List<DistributionData> getUserTypeDistribution() {
        return userTypeDistribution;
    }

    public void setUserTypeDistribution(List<DistributionData> userTypeDistribution) {
        this.userTypeDistribution = userTypeDistribution;
    }

    public List<DistributionData> getBoardDistribution() {
        return boardDistribution;
    }

    public void setBoardDistribution(List<DistributionData> boardDistribution) {
        this.boardDistribution = boardDistribution;
    }

    public List<DistributionData> getPostStatusDistribution() {
        return postStatusDistribution;
    }

    public void setPostStatusDistribution(List<DistributionData> postStatusDistribution) {
        this.postStatusDistribution = postStatusDistribution;
    }

    public List<DistributionData> getImagePostDistribution() {
        return imagePostDistribution;
    }

    public void setImagePostDistribution(List<DistributionData> imagePostDistribution) {
        this.imagePostDistribution = imagePostDistribution;
    }

    public List<DistributionData> getViolationTypeDistribution() {
        return violationTypeDistribution;
    }

    public void setViolationTypeDistribution(List<DistributionData> violationTypeDistribution) {
        this.violationTypeDistribution = violationTypeDistribution;
    }

    public List<DistributionData> getReportTypeDistribution() {
        return reportTypeDistribution;
    }

    public void setReportTypeDistribution(List<DistributionData> reportTypeDistribution) {
        this.reportTypeDistribution = reportTypeDistribution;
    }

    public List<DistributionData> getUserActivityDistribution() {
        return userActivityDistribution;
    }

    public void setUserActivityDistribution(List<DistributionData> userActivityDistribution) {
        this.userActivityDistribution = userActivityDistribution;
    }

    public List<User> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(List<User> activeUsers) {
        this.activeUsers = activeUsers;
    }

    public List<Post> getHotPosts() {
        return hotPosts;
    }

    public void setHotPosts(List<Post> hotPosts) {
        this.hotPosts = hotPosts;
    }

    public List<Comment> getHotComments() {
        return hotComments;
    }

    public void setHotComments(List<Comment> hotComments) {
        this.hotComments = hotComments;
    }

    public List<User> getBannedUsers() {
        return bannedUsers;
    }

    public void setBannedUsers(List<User> bannedUsers) {
        this.bannedUsers = bannedUsers;
    }

    public ModerationOverview getModerationOverview() {
        return moderationOverview;
    }

    public void setModerationOverview(ModerationOverview moderationOverview) {
        this.moderationOverview = moderationOverview;
    }

    public ReportOverview getReportOverview() {
        return reportOverview;
    }

    public void setReportOverview(ReportOverview reportOverview) {
        this.reportOverview = reportOverview;
    }
}
