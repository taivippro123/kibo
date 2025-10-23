package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class AddToWishlistRequest {
    @SerializedName("userid")
    private int userId;

    @SerializedName("productids")
    private int[] productIds;

    public AddToWishlistRequest(int userId, int[] productIds) {
        this.userId = userId;
        this.productIds = productIds;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int[] getProductIds() {
        return productIds;
    }

    public void setProductIds(int[] productIds) {
        this.productIds = productIds;
    }
}
