package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "bbs_comment")
public class BbsComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "reply_to_comment_id")
    private Long replyToCommentId;

    @Column(name = "reply_to_user_id")
    private Long replyToUserId;

    @Column(name = "reply_to_user_nickname")
    private String replyToUserNickname;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    @Column(name = "attachment_infos", columnDefinition = "TEXT")
    private String attachmentInfos;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer status = 1;

    // ==================== 审核相关字段 ====================

    @Column(name = "moderation_status", nullable = false)
    private String moderationStatus = "pass";

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
    private List<BbsComment> replyList;

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
