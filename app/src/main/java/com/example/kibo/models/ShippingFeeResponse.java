package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ShippingFeeResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private ShippingFeeData data;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ShippingFeeData getData() {
        return data;
    }
    
    public void setData(ShippingFeeData data) {
        this.data = data;
    }
}

