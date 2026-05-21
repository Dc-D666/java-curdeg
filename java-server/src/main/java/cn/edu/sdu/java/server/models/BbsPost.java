package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bbs_post")
public class BbsPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "attachment_infos", columnDefinition = "TEXT")
    private String attachmentInfos;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    @Column(name = "is_top", nullable = false)
    private Boolean isTop = false;

    @Column(name = "top_expire_time")
    private String topExpireTime;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(nullable = false)
    private Integer status = 1;

    // ==================== 审核相关字段 ====================

    @Column(name = "moderation_status", nullable = false)
    private String moderationStatus = "pending";

    @Column(name = "moderation_violation_level")
    private String moderationViolationLevel;

    @Column(name = "moderation_violation_type")
    private String moderationViolationType;

    @Column(name = "moderation_violation_fragments", columnDefinition = "TEXT")
    private String moderationViolationFragments;

    @Column(name = "moderation_suggestion", columnDefinition = "TEXT")
    private String moderationSuggestion;

    @Column(name = "moderation_confidence")
    private Integer moderationConfidence;

    @Column(name = "moderation_remark", columnDefinition = "TEXT")
    private String moderationRemark;

    @Column(name = "moderation_time")
    private String moderationTime;

    @Column(name = "moderator_id")
    private Integer moderatorId;

    @Column(name = "create_time", updatable = false)
    private String createTime;

    @Column(name = "update_time")
    private String updateTime;

    // ==================== 临时字段（不映射到数据库） ====================

    @Transient
    private String authorNickname;

    @Transient
    private String authorNicknameStyle;

    @Transient
    private String authorAvatarUrl;

    @Transient
    private String boardName;

    @Transient
    private String moderatorNickname;

    @Transient
    private Double matchScore;

    @Transient
    private String highlightTitle;

    @Transient
    private String highlightSnippet;

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
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

    @PrePersist
    protected void onCreate() {
        createTime = cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date());
        updateTime = createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date());
    }
}
