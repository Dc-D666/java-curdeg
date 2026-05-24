package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bbs_user_browse_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class BbsUserBrowseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "browse_time", nullable = false)
    private String browseTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds = 0;

    @PrePersist
    protected void onCreate() {
        if (browseTime == null) {
            browseTime = cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        browseTime = cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date());
    }
}
