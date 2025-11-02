package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Order implements Serializable {
    @SerializedName("orderid")
    private int orderId;
    @SerializedName("ordercode")
    private String orderCode;
    @SerializedName("cartid")
    private int cartId;
    @SerializedName("userid")
    private int userId;
    @SerializedName("orderstatus")
    private int orderStatus;
    @SerializedName("orderdate")
    private String orderDate;
    @SerializedName("paymentid")
    private Integer paymentId;

    public int getOrderId() { return orderId; }
    public String getOrderCode() { return orderCode; }
    public int getCartId() { return cartId; }
    public int getUserId() { return userId; }
    public int getOrderStatus() { return orderStatus; }
    public String getOrderDate() { return orderDate; }
    public Integer getPaymentId() { return paymentId; }
}


