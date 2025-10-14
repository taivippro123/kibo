package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class OrderDetail {
    @SerializedName("orderDetailId")
    private int orderDetailId;
    @SerializedName("orderId")
    private int orderId;
    @SerializedName("productId")
    private int productId;
    @SerializedName("quantity")
    private int quantity;
    @SerializedName("unitPrice")
    private double unitPrice;
    @SerializedName("totalPrice")
    private double totalPrice;

    public int getOrderDetailId() { return orderDetailId; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
}


