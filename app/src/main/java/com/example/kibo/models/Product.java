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
    private String switchDetailType;

    @SerializedName("switch_type")
    private String switchType;

    @SerializedName("battery")
    private String battery;

    @SerializedName("os")
    private String os;

    @SerializedName("led")
    private String led;

    @SerializedName("screen")
    private String screen;

    @SerializedName("width")
    private int width;

    @SerializedName("length")
    private int length;

    @SerializedName("height")
    private int height;

    @SerializedName("weight")
    private int weight;

    @SerializedName("quantity")
    private int quantity;

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

    public String getSwitchDetailType() {
        return switchDetailType;
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

    // Setters - thêm cho admin CRUD operations
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setKeycap(String keycap) {
        this.keycap = keycap;
    }

    public void setSwitchDetailType(String switchDetailType) {
        this.switchDetailType = switchDetailType;
    }

    public void setSwitchType(String switchType) {
        this.switchType = switchType;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setLed(String led) {
        this.led = led;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Helper methods cho admin
    public String getStockText() {
        return "Tồn kho: " + quantity;
    }

    public String getCategoryDisplayText() {
        return categoryName != null ? categoryName : "Chưa phân loại";
    }
}
