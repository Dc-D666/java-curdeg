package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class User {
    private Integer personId;
    private String userName;
    private String studentId;
    private String nickname;
    private String avatarUrl;
    private String signature;
    private Integer postCount;
    private Integer commentCount;
    private Integer violationCount;
    private Boolean isBanned;
    private String authority;
    private Integer followerCount;
    private Integer followingCount;
    private Integer points = 0;
    private Integer level = 0;
    private String levelName;
    private String nicknameStyle = "normal";
    private BigDecimal storeDiscount = BigDecimal.ONE;

    private String personName;
    private String personDept;
    private String personGender;
    private String personBirthday;
    private String personEmail;
    private String personPhone;
    private String personAddress;
    private String personIntroduce;
    
    private String namePrivacy;
    private String deptPrivacy;
    private String genderPrivacy;
    private String birthdayPrivacy;
    private String emailPrivacy;
    private String phonePrivacy;
    private String addressPrivacy;
    private String introducePrivacy;

    public User() {
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Integer getPostCount() {
        return postCount;
    }

    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }

    public Boolean getIsBanned() {
        return isBanned;
    }

    public void setIsBanned(Boolean isBanned) {
        this.isBanned = isBanned;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonDept() {
        return personDept;
    }

    public void setPersonDept(String personDept) {
        this.personDept = personDept;
    }

    public String getPersonGender() {
        return personGender;
    }

    public void setPersonGender(String personGender) {
        this.personGender = personGender;
    }

    public String getPersonBirthday() {
        return personBirthday;
    }

    public void setPersonBirthday(String personBirthday) {
        this.personBirthday = personBirthday;
    }

    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    public String getPersonPhone() {
        return personPhone;
    }

    public void setPersonPhone(String personPhone) {
        this.personPhone = personPhone;
    }

    public String getPersonAddress() {
        return personAddress;
    }

    public void setPersonAddress(String personAddress) {
        this.personAddress = personAddress;
    }

    public String getPersonIntroduce() {
        return personIntroduce;
    }

    public void setPersonIntroduce(String personIntroduce) {
        this.personIntroduce = personIntroduce;
    }
    
    public String getNamePrivacy() {
        return namePrivacy;
    }
    
    public void setNamePrivacy(String namePrivacy) {
        this.namePrivacy = namePrivacy;
    }
    
    public String getDeptPrivacy() {
        return deptPrivacy;
    }
    
    public void setDeptPrivacy(String deptPrivacy) {
        this.deptPrivacy = deptPrivacy;
    }
    
    public String getGenderPrivacy() {
        return genderPrivacy;
    }
    
    public void setGenderPrivacy(String genderPrivacy) {
        this.genderPrivacy = genderPrivacy;
    }
    
    public String getBirthdayPrivacy() {
        return birthdayPrivacy;
    }
    
    public void setBirthdayPrivacy(String birthdayPrivacy) {
        this.birthdayPrivacy = birthdayPrivacy;
    }
    
    public String getEmailPrivacy() {
        return emailPrivacy;
    }
    
    public void setEmailPrivacy(String emailPrivacy) {
        this.emailPrivacy = emailPrivacy;
    }
    
    public String getPhonePrivacy() {
        return phonePrivacy;
    }
    
    public void setPhonePrivacy(String phonePrivacy) {
        this.phonePrivacy = phonePrivacy;
    }
    
    public String getAddressPrivacy() {
        return addressPrivacy;
    }
    
    public void setAddressPrivacy(String addressPrivacy) {
        this.addressPrivacy = addressPrivacy;
    }
    
    public String getIntroducePrivacy() {
        return introducePrivacy;
    }

    public void setIntroducePrivacy(String introducePrivacy) {
        this.introducePrivacy = introducePrivacy;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getNicknameStyle() {
        return nicknameStyle;
    }

    public void setNicknameStyle(String nicknameStyle) {
        this.nicknameStyle = nicknameStyle;
    }

    public BigDecimal getStoreDiscount() {
        return storeDiscount;
    }

    public void setStoreDiscount(BigDecimal storeDiscount) {
        this.storeDiscount = storeDiscount;
    }
}
