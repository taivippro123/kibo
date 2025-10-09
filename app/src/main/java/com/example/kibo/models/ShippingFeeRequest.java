package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ShippingFeeRequest {
    @SerializedName("serviceTypeId")
    private int serviceTypeId;
    
    @SerializedName("toDistrictId")
    private int toDistrictId;
    
    @SerializedName("toWardCode")
    private String toWardCode;
    
    @SerializedName("height")
    private int height;
    
    @SerializedName("length")
    private int length;
    
    @SerializedName("width")
    private int width;
    
    @SerializedName("weight")
    private int weight;
    
    public ShippingFeeRequest(int serviceTypeId, int toDistrictId, String toWardCode, 
                              int height, int length, int width, int weight) {
        this.serviceTypeId = serviceTypeId;
        this.toDistrictId = toDistrictId;
        this.toWardCode = toWardCode;
        this.height = height;
        this.length = length;
        this.width = width;
        this.weight = weight;
    }
    
    // Getters and setters
    public int getServiceTypeId() {
        return serviceTypeId;
    }
    
    public void setServiceTypeId(int serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }
    
    public int getToDistrictId() {
        return toDistrictId;
    }
    
    public void setToDistrictId(int toDistrictId) {
        this.toDistrictId = toDistrictId;
    }
    
    public String getToWardCode() {
        return toWardCode;
    }
    
    public void setToWardCode(String toWardCode) {
        this.toWardCode = toWardCode;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getLength() {
        return length;
    }
    
    public void setLength(int length) {
        this.length = length;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
    }
}

