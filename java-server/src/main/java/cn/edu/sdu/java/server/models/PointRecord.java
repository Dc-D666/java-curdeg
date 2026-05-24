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
@Table(name = "bbs_point_record")
public class PointRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Column(name = "related_id")
    private Long relatedId;

    @Size(max = 50)
    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
