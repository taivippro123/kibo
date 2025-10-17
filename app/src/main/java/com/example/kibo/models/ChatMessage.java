package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("chatmessageid")
    private int chatMessageId;
    
    @SerializedName("conversationid")
    private int conversationId;
    
    @SerializedName("senderid")
    private int senderId;
    
    @SerializedName("senderName")
    private String senderName;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("sentat")
    private String sentAt;
    
    @SerializedName("hasImage")
    private boolean hasImage;
    
    @SerializedName("isFromShop")
    private boolean isFromShop;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(String message, String imageUrl) {
        this.message = message;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getChatMessageId() {
        return chatMessageId;
    }

    public void setChatMessageId(int chatMessageId) {
        this.chatMessageId = chatMessageId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public boolean isFromShop() {
        return isFromShop;
    }

    public void setFromShop(boolean fromShop) {
        isFromShop = fromShop;
    }
}
