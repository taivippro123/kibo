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
    
    @SerializedName("toWardName")
    private String toWardName;
    
    @SerializedName("toDistrictId")
    private int toDistrictId;
    
    @SerializedName("toDistrictName")
    private String toDistrictName;
    
    @SerializedName("toProvinceName")
    private String toProvinceName;
    
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
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("note")
    private String note;
    
    @SerializedName("requiredNote")
    private String requiredNote;
    
    @SerializedName("items")
    private java.util.List<OrderItem> items;
    
    public ShippingOrderRequest(String toName, String toPhone, String toAddress, 
                                String toWardCode, String toWardName, 
                                int toDistrictId, String toDistrictName,
                                String toProvinceName,
                                int weight, int length, int width, int height) {
        this.toName = toName;
        this.toPhone = toPhone;
        this.toAddress = toAddress;
        this.toWardCode = toWardCode;
        this.toWardName = toWardName;
        this.toDistrictId = toDistrictId;
        this.toDistrictName = toDistrictName;
        this.toProvinceName = toProvinceName;
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
        this.serviceTypeId = 2;
        this.paymentTypeId = 2;
        this.codAmount = 0;
        this.insuranceValue = 0;
        this.content = "";
        this.note = "";
        this.requiredNote = "KHONGCHOXEMHANG";
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
    
    public String getToWardName() {
        return toWardName;
    }
    
    public void setToWardName(String toWardName) {
        this.toWardName = toWardName;
    }
    
    public int getToDistrictId() {
        return toDistrictId;
    }
    
    public void setToDistrictId(int toDistrictId) {
        this.toDistrictId = toDistrictId;
    }
    
    public String getToDistrictName() {
        return toDistrictName;
    }
    
    public void setToDistrictName(String toDistrictName) {
        this.toDistrictName = toDistrictName;
    }
    
    public String getToProvinceName() {
        return toProvinceName;
    }
    
    public void setToProvinceName(String toProvinceName) {
        this.toProvinceName = toProvinceName;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getRequiredNote() {
        return requiredNote;
    }
    
    public void setRequiredNote(String requiredNote) {
        this.requiredNote = requiredNote;
    }
    
    public java.util.List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(java.util.List<OrderItem> items) {
        this.items = items;
    }
    
    // Inner class for order items
    public static class OrderItem {
        @SerializedName("name")
        private String name;
        
        @SerializedName("code")
        private String code;
        
        @SerializedName("quantity")
        private int quantity;
        
        @SerializedName("price")
        private int price;
        
        @SerializedName("length")
        private int length;
        
        @SerializedName("width")
        private int width;
        
        @SerializedName("height")
        private int height;
        
        @SerializedName("weight")
        private int weight;
        
        public OrderItem(String name, String code, int quantity, int price, 
                        int length, int width, int height, int weight) {
            this.name = name;
            this.code = code;
            this.quantity = quantity;
            this.price = price;
            this.length = length;
            this.width = width;
            this.height = height;
            this.weight = weight;
        }
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public int getPrice() {
            return price;
        }
        
        public void setPrice(int price) {
            this.price = price;
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
        
        public int getWeight() {
            return weight;
        }
        
        public void setWeight(int weight) {
            this.weight = weight;
        }
    }
}

