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
@Table(name = "bbs_level_config")
public class LevelConfig {

    @Id
    private Integer id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;

    @Column(name = "min_points", nullable = false)
    private Integer minPoints;

    @Size(max = 255)
    @Column(name = "icon_path", length = 255)
    private String iconPath;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String privileges;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
