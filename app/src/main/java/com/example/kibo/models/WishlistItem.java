package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class WishlistItem {
    @SerializedName("id")
    private int id;

    @SerializedName("userid")
    private int userId;

    @SerializedName("productid")
    private int productId;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}
