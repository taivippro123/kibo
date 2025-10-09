package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Cart {
    @SerializedName("cartid")
    private int cartId;
    
    @SerializedName("userid")
    private int userId;
    
    @SerializedName("status")
    private int status;
    
    @SerializedName("totalprice")
    private double totalPrice;
    
    @SerializedName("statusName")
    private String statusName;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("cartitems")
    private List<Object> cartItems;

    public Cart() {}

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Object> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<Object> cartItems) {
        this.cartItems = cartItems;
    }
}

