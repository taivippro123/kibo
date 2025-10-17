package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class SendChatMessageRequest {
    @SerializedName("conversationId")
    private int conversationId;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("senderId")
    private int senderId;

    // Constructors
    public SendChatMessageRequest() {}

    public SendChatMessageRequest(int conversationId, String message, String imageUrl) {
        this.conversationId = conversationId;
        this.message = message;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
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

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}
