package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ShippingOrderRequest {
    @SerializedName("toName")
    private String toName;
    
    @SerializedName("toPhone")
    private String toPhone;
    
    @SerializedName("toAddress")
    private String toAddress;
    
    @SerializedName("toWardCode")
    private String toWardCode;
    
    @SerializedName("toDistrictId")
    private int toDistrictId;
    
    @SerializedName("weight")
    private int weight;
    
    @SerializedName("length")
    private int length;
    
    @SerializedName("width")
    private int width;
    
    @SerializedName("height")
    private int height;
    
    @SerializedName("serviceTypeId")
    private int serviceTypeId;
    
    @SerializedName("paymentTypeId")
    private int paymentTypeId;
    
    @SerializedName("codAmount")
    private int codAmount;
    
    @SerializedName("insuranceValue")
    private int insuranceValue;
    
    @SerializedName("requiredNote")
    private String requiredNote;
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("cartId")
    private int cartId;
    
    public ShippingOrderRequest(String toName, String toPhone, String toAddress, 
                                String toWardCode, int toDistrictId,
                                int weight, int length, int width, int height,
                                int userId, int cartId) {
        this.toName = toName;
        this.toPhone = toPhone;
        this.toAddress = toAddress;
        this.toWardCode = toWardCode;
        this.toDistrictId = toDistrictId;
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
        this.serviceTypeId = 2;
        this.paymentTypeId = 2;
        this.codAmount = 0;
        this.insuranceValue = 0;
        this.requiredNote = "KHONGCHOXEMHANG";
        this.userId = userId;
        this.cartId = cartId;
    }
    
    // Getters and setters
    public String getToName() {
        return toName;
    }
    
    public void setToName(String toName) {
        this.toName = toName;
    }
    
    public String getToPhone() {
        return toPhone;
    }
    
    public void setToPhone(String toPhone) {
        this.toPhone = toPhone;
    }
    
    public String getToAddress() {
        return toAddress;
    }
    
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
    
    public String getToWardCode() {
        return toWardCode;
    }
    
    public void setToWardCode(String toWardCode) {
        this.toWardCode = toWardCode;
    }
    
    public int getToDistrictId() {
        return toDistrictId;
    }
    
    public void setToDistrictId(int toDistrictId) {
        this.toDistrictId = toDistrictId;
    }
    
    public int getWeight() {
        return weight;
    }
    
    public void setWeight(int weight) {
        this.weight = weight;
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
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getServiceTypeId() {
        return serviceTypeId;
    }
    
    public void setServiceTypeId(int serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }
    
    public int getPaymentTypeId() {
        return paymentTypeId;
    }
    
    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }
    
    public int getCodAmount() {
        return codAmount;
    }
    
    public void setCodAmount(int codAmount) {
        this.codAmount = codAmount;
    }
    
    public int getInsuranceValue() {
        return insuranceValue;
    }
    
    public void setInsuranceValue(int insuranceValue) {
        this.insuranceValue = insuranceValue;
    }
    
    public String getRequiredNote() {
        return requiredNote;
    }
    
    public void setRequiredNote(String requiredNote) {
        this.requiredNote = requiredNote;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getCartId() {
        return cartId;
    }
    
    public void setCartId(int cartId) {
        this.cartId = cartId;
    }
}

