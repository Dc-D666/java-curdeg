package com.teach.javafx.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    private Long id;
    @SerializedName(value = "receiverId", alternate = "receiver_id")
    private Long receiverId;
    private Integer type;
    private String title;
    private String content;
    @SerializedName(value = "isRead", alternate = "is_read")
    private Integer isRead;
    @SerializedName(value = "createTime", alternate = "create_time")
    private String createTime;

    public Notification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
