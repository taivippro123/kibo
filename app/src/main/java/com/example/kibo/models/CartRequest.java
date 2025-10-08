package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class CartRequest {
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("status")
    private int status;

    public CartRequest() {}

    public CartRequest(int userId, int status) {
        this.userId = userId;
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}

