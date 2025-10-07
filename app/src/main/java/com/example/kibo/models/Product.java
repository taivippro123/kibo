package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("productid")
    private int productId;
    
    @SerializedName("productname")
    private String productName;
    
    @SerializedName("briefdescription")
    private String briefDescription;
    
    @SerializedName("fulldescription")
    private String fullDescription;
    
    @SerializedName("price")
    private double price;
    
    @SerializedName("imageurl")
    private String imageUrl;
    
    @SerializedName("categoryid")
    private int categoryId;
    
    @SerializedName("categoryname")
    private String categoryName;
    
    @SerializedName("connection")
    private String connection;
    
    @SerializedName("layout")
    private String layout;
    
    @SerializedName("keycap")
    private String keycap;
    
    @SerializedName("switch")
    private String switchType;
    
    @SerializedName("battery")
    private String battery;
    
    @SerializedName("os")
    private String os;
    
    @SerializedName("led")
    private String led;
    
    @SerializedName("screen")
    private String screen;

    // Getters
    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getConnection() {
        return connection;
    }

    public String getLayout() {
        return layout;
    }

    public String getKeycap() {
        return keycap;
    }

    public String getSwitchType() {
        return switchType;
    }

    public String getBattery() {
        return battery;
    }

    public String getOs() {
        return os;
    }

    public String getLed() {
        return led;
    }

    public String getScreen() {
        return screen;
    }

    // Format price to Vietnamese currency
    public String getFormattedPrice() {
        return String.format("%,.0fđ", price);
    }
}

