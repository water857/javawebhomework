package com.smartcommunity.entity;

import java.sql.Timestamp;

public class PrivateConversation {
    private int otherUserId;
    private String otherUsername;
    private String otherRealName;
    private String lastMessage;
    private Timestamp lastTime;
    private int unreadCount;

    public int getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(int otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUsername() {
        return otherUsername;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }

    public String getOtherRealName() {
        return otherRealName;
    }

    public void setOtherRealName(String otherRealName) {
        this.otherRealName = otherRealName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastTime() {
        return lastTime;
    }

    public void setLastTime(Timestamp lastTime) {
        this.lastTime = lastTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
