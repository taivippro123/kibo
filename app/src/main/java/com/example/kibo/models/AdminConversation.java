package com.example.kibo.models;

import java.util.Date;

public class AdminConversation {
    private int conversationId;
    private int customerId;
    private String customerName;
    private String customerEmail;
    private Date createdAt;
    private Date lastMessageAt;
    private String lastMessage;
    private boolean hasUnreadMessages;
    private int unreadCount;

    public AdminConversation() {}

    public AdminConversation(int conversationId, int customerId, String customerName, 
                           String customerEmail, Date createdAt, Date lastMessageAt, 
                           String lastMessage, boolean hasUnreadMessages, int unreadCount) {
        this.conversationId = conversationId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
        this.lastMessage = lastMessage;
        this.hasUnreadMessages = hasUnreadMessages;
        this.unreadCount = unreadCount;
    }

    // Getters
    public int getConversationId() { return conversationId; }
    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public Date getCreatedAt() { return createdAt; }
    public Date getLastMessageAt() { return lastMessageAt; }
    public String getLastMessage() { return lastMessage; }
    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public int getUnreadCount() { return unreadCount; }

    // Setters
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setLastMessageAt(Date lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
