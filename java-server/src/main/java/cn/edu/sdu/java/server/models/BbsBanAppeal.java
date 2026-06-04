package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bbs_ban_appeal")
public class BbsBanAppeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Size(max = 500)
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0:待处理, 1:已处理, 2:已驳回

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handle_result", length = 500)
    private String handleResult;

    @Column(name = "handle_time")
    private String handleTime;

    @Column(name = "create_time", updatable = false)
    private String createTime;

    @PrePersist
    protected void onCreate() {
        createTime = cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date());
    }
}
