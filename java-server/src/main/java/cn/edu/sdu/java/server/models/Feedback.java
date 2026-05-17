package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bbs_feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 100)
    private String contact;

    @Column(name = "status", length = 20)
    private String status = "PENDING";

    @Column(name = "create_time", updatable = false)
    private String createTime;

    @Column(name = "handle_result", length = 500)
    private String handleResult;

    @Transient
    private String userNickname;

    @PrePersist
    protected void onCreate() {
        createTime = cn.edu.sdu.java.server.util.DateTimeTool.parseDateTime(new java.util.Date());
    }
}
