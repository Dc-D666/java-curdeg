package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/*
 * User用户表实体类 保存每个允许登录的信息人员的账号信息，
 * Integer personId 用户表 user 主键 person_id
 * UserType userType 关联到用户类型对象
 * Person person 关联到该用户所用的Person对象，账户所对应的人员信息 person_id 关联 person 表主键 person_id
 * String userName 登录账号 和Person 中的num属性相同
 * String password 用户密码 非对称加密，这能加密，无法解码
 *
 * 【新增】社区业务扩展字段：
 * String studentId 学号（社区用户唯一标识）
 * String nickname 昵称（社区展示名）
 * String avatarUrl 头像URL
 * String signature 个性签名
 * Integer postCount 发帖数
 * Integer commentCount 回帖数
 * Integer violationCount 违规记录数
 * Boolean isBanned 是否被禁言
 */
import lombok.Getter;
import lombok.Setter;
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

    // ==================== 临时字段（不映射到数据库） ====================

    @Transient
    private String authority;
}
