package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class UnreadCountResponse {
    @SerializedName("conversationId")
    private int conversationId;
    
    @SerializedName("unreadCount")
    private int unreadCount;
    
    @SerializedName("totalMessages")
    private int totalMessages;
    
    @SerializedName("adminId")
    private int adminId;
    
    @SerializedName("customerId")
    private int customerId;

    public UnreadCountResponse() {}

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "UnreadCountResponse{" +
                "conversationId=" + conversationId +
                ", unreadCount=" + unreadCount +
                ", totalMessages=" + totalMessages +
                ", adminId=" + adminId +
                ", customerId=" + customerId +
                '}';
    }
}
