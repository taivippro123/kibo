package com.example.kibo.models;

import java.util.List;
import java.util.Date;

public class ConversationResponse {
    private int conversationid;
    private Integer customerid;
    private String customerName;
    private Date createdat;
    private List<ChatMessage> lastMessages;
    private int totalMessages;
    private Date lastMessageTime;
    private boolean hasUnreadMessages;

    // Constructors
    public ConversationResponse() {}

    public ConversationResponse(int conversationid, Integer customerid, String customerName, 
                               Date createdat, List<ChatMessage> lastMessages, int totalMessages, 
                               Date lastMessageTime, boolean hasUnreadMessages) {
        this.conversationid = conversationid;
        this.customerid = customerid;
        this.customerName = customerName;
        this.createdat = createdat;
        this.lastMessages = lastMessages;
        this.totalMessages = totalMessages;
        this.lastMessageTime = lastMessageTime;
        this.hasUnreadMessages = hasUnreadMessages;
    }

    // Getters and Setters
    public int getConversationid() {
        return conversationid;
    }

    public void setConversationid(int conversationid) {
        this.conversationid = conversationid;
    }

    public Integer getCustomerid() {
        return customerid;
    }

    public void setCustomerid(Integer customerid) {
        this.customerid = customerid;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
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

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isHasUnreadMessages() {
        return hasUnreadMessages;
    }

    public void setHasUnreadMessages(boolean hasUnreadMessages) {
        this.hasUnreadMessages = hasUnreadMessages;
    }
}
