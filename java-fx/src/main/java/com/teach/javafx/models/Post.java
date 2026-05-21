package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Post {
    private Long id;
    private Long boardId;
    @SerializedName(value = "authorId", alternate = "userId")
    private Long userId;
    private String title;
    private String content;
    @SerializedName(value = "images", alternate = "imageUrls")
    private String images;
    private String attachmentInfos;
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
    private String authorNicknameStyle;
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

    public String getAttachmentInfos() {
        return attachmentInfos;
    }

    public void setAttachmentInfos(String attachmentInfos) {
        this.attachmentInfos = attachmentInfos;
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

    public String getAuthorNicknameStyle() {
        return authorNicknameStyle;
    }

    public void setAuthorNicknameStyle(String authorNicknameStyle) {
        this.authorNicknameStyle = authorNicknameStyle;
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

    public String getModerationStatusStyle() {
        if (moderationStatus == null || moderationStatus.isBlank()) {
            return "";
        }
        String baseStyle = "-fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 12px;";
        switch (moderationStatus) {
            case "pending":
                return baseStyle + " -fx-background-color: #3b82f6; -fx-text-fill: white;";
            case "manual":
                return baseStyle + " -fx-background-color: #f59e0b; -fx-text-fill: white;";
            case "pass":
                return baseStyle + " -fx-background-color: #22c55e; -fx-text-fill: white;";
            case "reject":
                return baseStyle + " -fx-background-color: #ef4444; -fx-text-fill: white;";
            default:
                return baseStyle + " -fx-background-color: #94a3b8; -fx-text-fill: white;";
        }
    }

    public boolean hasManualModerationTrace() {
        return moderatorNickname != null && !moderatorNickname.isBlank();
    }

    public ModerationFlowView getModerationFlowView() {
        ModerationFlowStep publishStep = new ModerationFlowStep("帖子已发布", "已提交", ModerationFlowVisualState.COMPLETED);
        ModerationFlowStep aiStep;
        ModerationFlowStep manualStep;
        ModerationFlowStep resultStep;
        List<Boolean> connectors;
        String summary;
        boolean manualReviewed = hasManualModerationTrace();
        String status = moderationStatus == null ? "" : moderationStatus.trim();

        switch (status) {
            case "manual":
                aiStep = new ModerationFlowStep("AI审核", "需人工复核", ModerationFlowVisualState.COMPLETED);
                manualStep = new ModerationFlowStep("人工审核", "进行中", ModerationFlowVisualState.WARNING);
                resultStep = new ModerationFlowStep("审核结果", "待定", ModerationFlowVisualState.INACTIVE);
                connectors = Arrays.asList(true, true, false);
                summary = "AI判定需人工复核，等待管理员审核";
                break;
            case "pass":
                if (manualReviewed) {
                    aiStep = new ModerationFlowStep("AI审核", "已转人工", ModerationFlowVisualState.COMPLETED);
                    manualStep = new ModerationFlowStep("人工审核", "已完成", ModerationFlowVisualState.COMPLETED);
                    resultStep = new ModerationFlowStep("审核结果", "审核通过", ModerationFlowVisualState.COMPLETED);
                    connectors = Arrays.asList(true, true, true);
                    summary = "人工审核已完成，最终结果：审核通过";
                } else {
                    aiStep = new ModerationFlowStep("AI审核", "审核通过", ModerationFlowVisualState.COMPLETED);
                    manualStep = new ModerationFlowStep("人工审核", "已跳过", ModerationFlowVisualState.INACTIVE);
                    resultStep = new ModerationFlowStep("审核结果", "审核通过", ModerationFlowVisualState.COMPLETED);
                    connectors = Arrays.asList(true, false, false);
                    summary = "AI审核通过，人工审核已跳过";
                }
                break;
            case "reject":
                if (manualReviewed) {
                    aiStep = new ModerationFlowStep("AI审核", "已转人工", ModerationFlowVisualState.COMPLETED);
                    manualStep = new ModerationFlowStep("人工审核", "已完成", ModerationFlowVisualState.COMPLETED);
                    resultStep = new ModerationFlowStep("审核结果", "内容违规", ModerationFlowVisualState.DANGER);
                    connectors = Arrays.asList(true, true, true);
                    summary = "人工审核已完成，最终结果：内容违规";
                } else {
                    aiStep = new ModerationFlowStep("AI审核", "判定违规", ModerationFlowVisualState.DANGER);
                    manualStep = new ModerationFlowStep("人工审核", "已跳过", ModerationFlowVisualState.INACTIVE);
                    resultStep = new ModerationFlowStep("审核结果", "内容违规", ModerationFlowVisualState.DANGER);
                    connectors = Arrays.asList(true, false, false);
                    summary = "AI审核判定违规，人工审核已跳过";
                }
                break;
            case "pending":
            default:
                aiStep = new ModerationFlowStep("AI审核", "审核中", ModerationFlowVisualState.ACTIVE);
                manualStep = new ModerationFlowStep("人工审核", "未开始", ModerationFlowVisualState.INACTIVE);
                resultStep = new ModerationFlowStep("审核结果", "待定", ModerationFlowVisualState.INACTIVE);
                connectors = Arrays.asList(true, false, false);
                summary = "AI正在审核，审核完成前帖子暂不公开";
                break;
        }

        return new ModerationFlowView(
                summary,
                Arrays.asList(publishStep, aiStep, manualStep, resultStep),
                connectors
        );
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

    public enum ModerationFlowVisualState {
        COMPLETED,
        ACTIVE,
        WARNING,
        DANGER,
        INACTIVE
    }

    public static class ModerationFlowStep {
        private final String title;
        private final String stateText;
        private final ModerationFlowVisualState visualState;

        public ModerationFlowStep(String title, String stateText, ModerationFlowVisualState visualState) {
            this.title = title;
            this.stateText = stateText;
            this.visualState = visualState;
        }

        public String getTitle() {
            return title;
        }

        public String getStateText() {
            return stateText;
        }

        public ModerationFlowVisualState getVisualState() {
            return visualState;
        }
    }

    public static class ModerationFlowView {
        private final String summary;
        private final List<ModerationFlowStep> steps;
        private final List<Boolean> connectors;

        public ModerationFlowView(String summary, List<ModerationFlowStep> steps, List<Boolean> connectors) {
            this.summary = summary;
            this.steps = steps;
            this.connectors = connectors;
        }

        public String getSummary() {
            return summary;
        }

        public List<ModerationFlowStep> getSteps() {
            return steps;
        }

        public boolean isConnectorReached(int index) {
            return index >= 0 && index < connectors.size() && Boolean.TRUE.equals(connectors.get(index));
        }
    }
}
