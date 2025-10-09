package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class VoucherUseResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private VoucherUseData data;
    
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
    
    public VoucherUseData getData() {
        return data;
    }
    
    public void setData(VoucherUseData data) {
        this.data = data;
    }
    
    public static class VoucherUseData {
        @SerializedName("discountAmount")
        private double discountAmount;
        
        @SerializedName("finalTotal")
        private double finalTotal;
        
        public double getDiscountAmount() {
            return discountAmount;
        }
        
        public void setDiscountAmount(double discountAmount) {
            this.discountAmount = discountAmount;
        }
        
        public double getFinalTotal() {
            return finalTotal;
        }
        
        public void setFinalTotal(double finalTotal) {
            this.finalTotal = finalTotal;
        }
    }
}
