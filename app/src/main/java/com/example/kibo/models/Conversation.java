package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Conversation {
    @SerializedName("conversationid")
    private int conversationId;
    
    @SerializedName("customerid")
    private Integer customerId;
    
    @SerializedName("customerName")
    private String customerName;
    
    @SerializedName("createdat")
    private String createdAt;
    
    @SerializedName("lastMessages")
    private List<ChatMessage> lastMessages;
    
    @SerializedName("totalMessages")
    private int totalMessages;
    
    @SerializedName("lastMessageTime")
    private String lastMessageTime;
    
    @SerializedName("hasUnreadMessages")
    private boolean hasUnreadMessages;

    // Constructors
    public Conversation() {}

    // Getters and Setters
    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<ChatMessage> getLastMessages() {
        return lastMessages;
    }

    public void setLastMessages(List<ChatMessage> lastMessages) {
        this.lastMessages = lastMessages;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isHasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
