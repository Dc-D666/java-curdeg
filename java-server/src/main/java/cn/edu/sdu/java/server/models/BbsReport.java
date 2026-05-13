package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bbs_report")
public class BbsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "target_type", nullable = false)
    private Integer targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Lob
    @Column(name = "target_snapshot")
    private String targetSnapshot;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handle_type")
    private Integer handleType;

    @Size(max = 200)
    @Column(name = "handle_remark")
    private String handleRemark;

    @Column(name = "handle_time")
    private String handleTime;

    @Column(name = "create_time", updatable = false)
    private String createTime;

    @Column(name = "update_time")
    private String updateTime;

    @Transient
    private String reporterNickname;

    @Transient
    private String handlerNickname;

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
