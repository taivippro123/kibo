package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ShippingOrderResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private ShippingOrderData data;
    
    @SerializedName("order")
    private OrderData order;
    
    @SerializedName("payment")
    private PaymentData payment;
    
    @SerializedName("zaloPay")
    private ZaloPayData zaloPay;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ShippingOrderData getData() {
        return data;
    }
    
    public void setData(ShippingOrderData data) {
        this.data = data;
    }
    
    public OrderData getOrder() {
        return order;
    }
    
    public void setOrder(OrderData order) {
        this.order = order;
    }
    
    public PaymentData getPayment() {
        return payment;
    }
    
    public void setPayment(PaymentData payment) {
        this.payment = payment;
    }
    
    public ZaloPayData getZaloPay() {
        return zaloPay;
    }
    
    public void setZaloPay(ZaloPayData zaloPay) {
        this.zaloPay = zaloPay;
    }
    
    public static class ShippingOrderData {
        @SerializedName("orderId")
        private String orderId;
        
        @SerializedName("orderCode")
        private String orderCode;
        
        @SerializedName("trackingNumber")
        private String trackingNumber;
        
        public String getOrderId() {
            return orderId;
        }
        
        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
        
        public String getOrderCode() {
            return orderCode;
        }
        
        public void setOrderCode(String orderCode) {
            this.orderCode = orderCode;
        }
        
        public String getTrackingNumber() {
            return trackingNumber;
        }
        
        public void setTrackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
        }
    }
    
    public static class OrderData {
        @SerializedName("orderId")
        private int orderId;
        
        @SerializedName("orderCode")
        private String orderCode;
        
        @SerializedName("cartId")
        private Integer cartId;
        
        @SerializedName("userId")
        private int userId;
        
        @SerializedName("orderStatus")
        private int orderStatus;
        
        @SerializedName("orderDate")
        private String orderDate;
        
        @SerializedName("paymentId")
        private int paymentId;
        
        @SerializedName("orderDetailsCount")
        private int orderDetailsCount;
        
        public int getOrderId() {
            return orderId;
        }
        
        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }
        
        public String getOrderCode() {
            return orderCode;
        }
        
        public void setOrderCode(String orderCode) {
            this.orderCode = orderCode;
        }
        
        public Integer getCartId() {
            return cartId;
        }
        
        public void setCartId(Integer cartId) {
            this.cartId = cartId;
        }
        
        public int getUserId() {
            return userId;
        }
        
        public void setUserId(int userId) {
            this.userId = userId;
        }
        
        public int getOrderStatus() {
            return orderStatus;
        }
        
        public void setOrderStatus(int orderStatus) {
            this.orderStatus = orderStatus;
        }
        
        public String getOrderDate() {
            return orderDate;
        }
        
        public void setOrderDate(String orderDate) {
            this.orderDate = orderDate;
        }
        
        public int getPaymentId() {
            return paymentId;
        }
        
        public void setPaymentId(int paymentId) {
            this.paymentId = paymentId;
        }
        
        public int getOrderDetailsCount() {
            return orderDetailsCount;
        }
        
        public void setOrderDetailsCount(int orderDetailsCount) {
            this.orderDetailsCount = orderDetailsCount;
        }
    }
    
    public static class PaymentData {
        @SerializedName("paymentId")
        private int paymentId;
        
        @SerializedName("paymentMethod")
        private int paymentMethod;
        
        @SerializedName("paymentMethodName")
        private String paymentMethodName;
        
        @SerializedName("paymentStatus")
        private int paymentStatus;
        
        @SerializedName("amount")
        private double amount;
        
        @SerializedName("shippingFee")
        private double shippingFee;
        
        @SerializedName("productAmount")
        private double productAmount;
        
        public int getPaymentId() {
            return paymentId;
        }
        
        public void setPaymentId(int paymentId) {
            this.paymentId = paymentId;
        }
        
        public int getPaymentMethod() {
            return paymentMethod;
        }
        
        public void setPaymentMethod(int paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
        
        public String getPaymentMethodName() {
            return paymentMethodName;
        }
        
        public void setPaymentMethodName(String paymentMethodName) {
            this.paymentMethodName = paymentMethodName;
        }
        
        public int getPaymentStatus() {
            return paymentStatus;
        }
        
        public void setPaymentStatus(int paymentStatus) {
            this.paymentStatus = paymentStatus;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public void setAmount(double amount) {
            this.amount = amount;
        }
        
        public double getShippingFee() {
            return shippingFee;
        }
        
        public void setShippingFee(double shippingFee) {
            this.shippingFee = shippingFee;
        }
        
        public double getProductAmount() {
            return productAmount;
        }
        
        public void setProductAmount(double productAmount) {
            this.productAmount = productAmount;
        }
    }
    
    public static class ZaloPayData {
        @SerializedName("returnCode")
        private int returnCode;
        
        @SerializedName("returnMessage")
        private String returnMessage;
        
        @SerializedName("orderUrl")
        private String orderUrl;
        
        @SerializedName("zpTransToken")
        private String zpTransToken;
        
        @SerializedName("qrCode")
        private String qrCode;
        
        public int getReturnCode() {
            return returnCode;
        }
        
        public void setReturnCode(int returnCode) {
            this.returnCode = returnCode;
        }
        
        public String getReturnMessage() {
            return returnMessage;
        }
        
        public void setReturnMessage(String returnMessage) {
            this.returnMessage = returnMessage;
        }
        
        public String getOrderUrl() {
            return orderUrl;
        }
        
        public void setOrderUrl(String orderUrl) {
            this.orderUrl = orderUrl;
        }
        
        public String getZpTransToken() {
            return zpTransToken;
        }
        
        public void setZpTransToken(String zpTransToken) {
            this.zpTransToken = zpTransToken;
        }
        
        public String getQrCode() {
            return qrCode;
        }
        
        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }
    }
}

