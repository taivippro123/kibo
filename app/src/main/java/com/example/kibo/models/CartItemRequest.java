package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class CartItemRequest {
    @SerializedName("cartId")
    private int cartId;
    
    @SerializedName("productId")
    private int productId;
    
    @SerializedName("quantity")
    private int quantity;

    public CartItemRequest() {}

    public CartItemRequest(int cartId, int productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

