package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ShippingOrderResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private ShippingOrderData data;
    
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
    
    public ShippingOrderData getData() {
        return data;
    }
    
    public void setData(ShippingOrderData data) {
        this.data = data;
    }
    
    public static class ShippingOrderData {
        @SerializedName("orderId")
        private String orderId;
        
        @SerializedName("orderCode")
        private String orderCode;
        
        @SerializedName("trackingNumber")
        private String trackingNumber;
        
        public String getOrderId() {
            return orderId;
        }
        
        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
        
        public String getOrderCode() {
            return orderCode;
        }
        
        public void setOrderCode(String orderCode) {
            this.orderCode = orderCode;
        }
        
        public String getTrackingNumber() {
            return trackingNumber;
        }
        
        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }
    }
}

