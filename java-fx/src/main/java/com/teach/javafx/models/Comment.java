package com.teach.javafx.models;

import java.util.Date;
import java.util.List;

public class Comment {
    private Long id;
    private Long postId;
    private Long authorId;
    private Long parentId;
    private Long replyToCommentId;
    private Long replyToUserId;
    private String replyToUserNickname;
    private String content;
    private String contentHtml;
    private java.util.List<java.util.Map<String, Object>> mentionedUsers;
    private Date createTime;
    private Date updateTime;
    
    private String authorNickname;
    private String authorNicknameStyle;
    private String authorAvatarUrl;
    
    private String imageUrls;
    private String attachmentInfos;
    private Integer likeCount;
    private Integer status;
    private String moderationStatus;
    
    private List<Comment> replyList;

    public Comment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getReplyToCommentId() {
        return replyToCommentId;
    }

    public void setReplyToCommentId(Long replyToCommentId) {
        this.replyToCommentId = replyToCommentId;
    }

    public Long getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Long replyToUserId) {
        this.replyToUserId = replyToUserId;
    }

    public String getReplyToUserNickname() {
        return replyToUserNickname;
    }

    public void setReplyToUserNickname(String replyToUserNickname) {
        this.replyToUserNickname = replyToUserNickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public java.util.List<java.util.Map<String, Object>> getMentionedUsers() {
        return mentionedUsers;
    }

    public void setMentionedUsers(java.util.List<java.util.Map<String, Object>> mentionedUsers) {
        this.mentionedUsers = mentionedUsers;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorNicknameStyle() {
        return authorNicknameStyle;
    }

    public void setAuthorNicknameStyle(String authorNicknameStyle) {
        this.authorNicknameStyle = authorNicknameStyle;
    }

    public String getAuthorAvatarUrl() {
        // 使用原来的默认头像
        String defaultUrl = "https://img.phb123.com/uploads/allimg/220607/810-22060G55A40-L.jpeg";
        
        if (authorAvatarUrl == null || authorAvatarUrl.isBlank()) {
            return defaultUrl;
        }
        
        // 清理所有特殊字符
        String clean = authorAvatarUrl.trim()
            .replace("`", "")
            .replace("\"", "")
            .replace("'", "");
        
        if (clean.isBlank()) {
            return defaultUrl;
        }
        
        return clean;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getAttachmentInfos() {
        return attachmentInfos;
    }

    public void setAttachmentInfos(String attachmentInfos) {
        this.attachmentInfos = attachmentInfos;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public List<Comment> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<Comment> replyList) {
        this.replyList = replyList;
    }
}
