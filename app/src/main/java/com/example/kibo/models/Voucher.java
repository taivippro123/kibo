package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Voucher implements Serializable {
    @SerializedName("voucherid")
    private int voucherId;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("discounttype")
    private int discountType; // 1 = percentage, 2 = fixed amount
    
    @SerializedName("discountvalue")
    private double discountValue;
    
    @SerializedName("minordervalue")
    private double minOrderValue;
    
    @SerializedName("maxdiscount")
    private double maxDiscount;
    
    @SerializedName("startdate")
    private String startDate;
    
    @SerializedName("enddate")
    private String endDate;
    
    @SerializedName("isactive")
    private boolean isActive;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("ordertables")
    private List<Object> orderTables;
    
    public Voucher() {}
    
    // Getters
    public int getVoucherId() {
        return voucherId;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDiscountType() {
        return discountType;
    }
    
    public double getDiscountValue() {
        return discountValue;
    }
    
    public double getMinOrderValue() {
        return minOrderValue;
    }
    
    public double getMaxDiscount() {
        return maxDiscount;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public String getEndDate() {
        return endDate;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public List<Object> getOrderTables() {
        return orderTables;
    }
    
    // Setters
    public void setVoucherId(int voucherId) {
        this.voucherId = voucherId;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setDiscountType(int discountType) {
        this.discountType = discountType;
    }
    
    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }
    
    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }
    
    public void setMaxDiscount(double maxDiscount) {
        this.maxDiscount = maxDiscount;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setOrderTables(List<Object> orderTables) {
        this.orderTables = orderTables;
    }
    
    // Helper methods
    public String getDiscountTypeText() {
        return discountType == 1 ? "Giảm %" : "Giảm tiền";
    }
    
    public String getDiscountDisplayText() {
        if (discountType == 1) {
            return String.format("Giảm %.0f%%", discountValue);
        } else {
            return String.format("Giảm %,.0fđ", discountValue);
        }
    }
    
    public String getMinOrderDisplayText() {
        if (minOrderValue <= 0) {
            return "Không giới hạn";
        }
        return String.format("Đơn tối thiểu %,.0fđ", minOrderValue);
    }
    
    public boolean isValidForOrder(double orderValue) {
        return isActive && orderValue >= minOrderValue && quantity > 0;
    }
    
    public double calculateDiscount(double orderValue) {
        if (!isValidForOrder(orderValue)) {
            return 0;
        }
        
        double discount = 0;
        if (discountType == 1) { // Percentage
            discount = orderValue * (discountValue / 100);
        } else { // Fixed amount
            discount = discountValue;
        }
        
        // Apply max discount limit
        if (discount > maxDiscount) {
            discount = maxDiscount;
        }
        
        return discount;
    }
}
