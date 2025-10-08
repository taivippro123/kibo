package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class RemoveCartItemRequest {
    @SerializedName("cartId")
    private int cartId;

    @SerializedName("productId")
    private int productId;

    public RemoveCartItemRequest(int cartId, int productId) {
        this.cartId = cartId;
        this.productId = productId;
    }

    public int getCartId() {
        return cartId;
    }

    public int getProductId() {
        return productId;
    }
}


