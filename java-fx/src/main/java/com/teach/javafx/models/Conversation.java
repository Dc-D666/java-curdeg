package com.teach.javafx.models;

public class Conversation {
    private Long conversationId;
    private Integer otherUserId;
    private User otherUser;
    private Integer unreadCount;
    private Message lastMessage;
    private String lastMessageTime;
    private Boolean isMutualFollow;

    public Conversation() {}

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Integer otherUserId) {
        this.otherUserId = otherUserId;
    }

    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Boolean getIsMutualFollow() {
        return isMutualFollow;
    }

    public void setIsMutualFollow(Boolean isMutualFollow) {
        this.isMutualFollow = isMutualFollow;
    }
}
