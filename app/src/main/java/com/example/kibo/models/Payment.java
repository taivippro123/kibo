package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("paymentid")
    private int paymentId;
    
    @SerializedName("orderid")
    private int orderId;
    
    @SerializedName("amount")
    private double amount;
    
    @SerializedName("paymentmethod")
    private int paymentMethod;
    
    @SerializedName("paymentdate")
    private String paymentDate;
    
    @SerializedName("paymentstatus")
    private int paymentStatus;
    
    @SerializedName("order")
    private Object order;
    
    public int getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }
    
    public int getOrderId() {
        return orderId;
    }
    
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public int getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(int paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public int getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(int paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public Object getOrder() {
        return order;
    }
    
    public void setOrder(Object order) {
        this.order = order;
    }
}

