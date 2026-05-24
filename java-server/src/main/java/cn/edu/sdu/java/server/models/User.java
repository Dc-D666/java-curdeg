package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Entity
@Table(	name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "userName"),
                @UniqueConstraint(columnNames = "student_id"),
        })
public class User {
    @Id
    private Integer personId;

    @ManyToOne()
    @JoinColumn(name = "userTypeId")
    private UserType userType;

    @OneToOne
    @JoinColumn(name="personId")
    @JsonIgnore
    private Person person;

    @NotBlank
    @Size(max = 20)
    private String userName;


    @NotBlank
    @Size(max = 60)
    @JsonIgnore
    private String password;

    private Integer loginCount;
    @Size(max = 20)
    private String lastLoginTime;
    @Size(max = 20)
    private String  createTime;
    private Integer creatorId;

    // ==================== 社区业务扩展字段 ====================

    @NotBlank
    @Size(max = 20)
    @Column(name = "student_id")
    private String studentId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "nickname")
    private String nickname;

    @Size(max = 255)
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Size(max = 200)
    @Column(name = "signature")
    private String signature;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "violation_count", nullable = false)
    private Integer violationCount = 0;

    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned = false;

    @Column(name = "follower_count")
    private Integer followerCount = 0;

    @Column(name = "following_count")
    private Integer followingCount = 0;

    @Column(name = "points", nullable = false)
    private Integer points = 0;

    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Column(name = "consecutive_login_days", nullable = false)
    private Integer consecutiveLoginDays = 0;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Column(name = "profile_completed_reward", nullable = false)
    private Boolean profileCompletedReward = false;

    @Column(name = "level_protected_until")
    private LocalDate levelProtectedUntil;

    @Column(name = "version", nullable = false)
    private Integer version = 0;

    // ==================== 临时字段（不映射到数据库） ====================

    @Transient
    private String authority;

    // ==================== Person 表相关字段（临时字段） ====================

    @Transient
    private String personName;

    @Transient
    private String personDept;

    @Transient
    private String personGender;

    @Transient
    private String personBirthday;

    @Transient
    private String personEmail;

    @Transient
    private String personPhone;

    @Transient
    private String personAddress;

    @Transient
    private String personIntroduce;

    // ==================== 隐私设置字段（临时字段） ====================

    @Transient
    private String namePrivacy;

    @Transient
    private String deptPrivacy;

    @Transient
    private String genderPrivacy;

    @Transient
    private String birthdayPrivacy;

    @Transient
    private String emailPrivacy;

    @Transient
    private String phonePrivacy;

    @Transient
    private String addressPrivacy;

    @Transient
    private String introducePrivacy;
}
