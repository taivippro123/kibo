package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ProductImage implements Serializable {
    @SerializedName("imageid")
    private int imageId;
    
    @SerializedName("productid")
    private int productId;
    
    @SerializedName("imageurl")
    private String imageUrl;
    
    @SerializedName("isprimary")
    private boolean isPrimary;
    
    @SerializedName("sortorder")
    private int sortOrder;
    
    public ProductImage() {
    }
    
    public ProductImage(int imageId, int productId, String imageUrl, boolean isPrimary, int sortOrder) {
        this.imageId = imageId;
        this.productId = productId;
        this.imageUrl = imageUrl;
        this.isPrimary = isPrimary;
        this.sortOrder = sortOrder;
    }
    
    // Getters and Setters
    public int getImageId() {
        return imageId;
    }
    
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

