package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Post {
    private Long id;
    private Long boardId;
    @SerializedName(value = "authorId", alternate = "userId")
    private Long userId;
    private String title;
    private String content;
    @SerializedName(value = "images", alternate = "imageUrls")
    private String images;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer favoriteCount;
    @SerializedName(value = "isTop", alternate = "top")
    private Boolean isTop;
    @SerializedName(value = "isFeatured", alternate = "featured")
    private Boolean isFeatured;
    private Integer status;
    private String moderationStatus;
    private Date createTime;
    private Date updateTime;
    
    private String authorNickname;
    private String authorAvatarUrl;
    private String boardName;
    private String moderationTime;
    private String moderatorNickname;
    private String moderationViolationLevel;
    private String moderationViolationType;
    private String moderationViolationFragments;
    private String moderationSuggestion;
    private String moderationRemark;
    
    private String highlightTitle;
    private String highlightSnippet;

    public Post() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Boolean getIsTop() {
        return isTop;
    }

    public void setIsTop(Boolean isTop) {
        this.isTop = isTop;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }

    public String getHighlightTitle() {
        return highlightTitle;
    }

    public void setHighlightTitle(String highlightTitle) {
        this.highlightTitle = highlightTitle;
    }

    public String getHighlightSnippet() {
        return highlightSnippet;
    }

    public void setHighlightSnippet(String highlightSnippet) {
        this.highlightSnippet = highlightSnippet;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public String getModerationStatusText() {
        if (moderationStatus == null) {
            return "";
        }
        switch (moderationStatus) {
            case "pending":
                return "审核中";
            case "pass":
                return "审核通过";
            case "reject":
                return "内容违规";
            case "manual":
                return "待人工审核";
            default:
                return "";
        }
    }

    public String getStatusText() {
        StringBuilder sb = new StringBuilder();
        if (isTop != null && isTop) {
            sb.append("置顶 ");
        }
        if (isFeatured != null && isFeatured) {
            sb.append("加精");
        }
        return sb.toString().trim();
    }

    public String getModerationTime() {
        return moderationTime;
    }

    public void setModerationTime(String moderationTime) {
        this.moderationTime = moderationTime;
    }

    public String getModeratorNickname() {
        return moderatorNickname;
    }

    public void setModeratorNickname(String moderatorNickname) {
        this.moderatorNickname = moderatorNickname;
    }

    public String getModerationViolationLevel() {
        return moderationViolationLevel;
    }

    public void setModerationViolationLevel(String moderationViolationLevel) {
        this.moderationViolationLevel = moderationViolationLevel;
    }

    public String getModerationViolationType() {
        return moderationViolationType;
    }

    public void setModerationViolationType(String moderationViolationType) {
        this.moderationViolationType = moderationViolationType;
    }

    public String getModerationViolationFragments() {
        return moderationViolationFragments;
    }

    public void setModerationViolationFragments(String moderationViolationFragments) {
        this.moderationViolationFragments = moderationViolationFragments;
    }

    public String getModerationSuggestion() {
        return moderationSuggestion;
    }

    public void setModerationSuggestion(String moderationSuggestion) {
        this.moderationSuggestion = moderationSuggestion;
    }

    public String getModerationRemark() {
        return moderationRemark;
    }

    public void setModerationRemark(String moderationRemark) {
        this.moderationRemark = moderationRemark;
    }
}
