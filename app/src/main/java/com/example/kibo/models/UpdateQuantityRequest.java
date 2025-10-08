package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class UpdateQuantityRequest {
    @SerializedName("cartId")
    private int cartId;

    @SerializedName("productId")
    private int productId;

    @SerializedName("quantity")
    private int quantity;

    public UpdateQuantityRequest(int cartId, int productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getCartId() {
        return cartId;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}


