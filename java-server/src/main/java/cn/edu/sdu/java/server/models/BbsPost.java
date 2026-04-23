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

    @Column(name = "is_top", nullable = false)
    private Boolean isTop = false;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "create_time", updatable = false)
    private String createTime;

    @Column(name = "update_time")
    private String updateTime;

    // ==================== 临时字段（不映射到数据库） ====================

    @Transient
    private String authorNickname;

    @Transient
    private String authorAvatarUrl;

    @Transient
    private String boardName;

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
