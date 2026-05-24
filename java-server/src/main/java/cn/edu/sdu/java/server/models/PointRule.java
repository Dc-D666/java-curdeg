package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bbs_point_rule",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "rule_code")
        })
public class PointRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
