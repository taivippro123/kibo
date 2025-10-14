package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class ShippingFeeData {
    @SerializedName("total")
    private double total;
    
    @SerializedName("service_fee")
    private double serviceFee;
    
    @SerializedName("insurance_fee")
    private double insuranceFee;
    
    @SerializedName("pick_station_fee")
    private double pickStationFee;
    
    @SerializedName("coupon_value")
    private double couponValue;
    
    @SerializedName("r2s_fee")
    private double r2sFee;
    
    @SerializedName("return_again")
    private double returnAgain;
    
    @SerializedName("document_return")
    private double documentReturn;
    
    @SerializedName("double_check")
    private double doubleCheck;
    
    @SerializedName("cod_fee")
    private double codFee;
    
    @SerializedName("pick_remote_areas_fee")
    private double pickRemoteAreasFee;
    
    @SerializedName("deliver_remote_areas_fee")
    private double deliverRemoteAreasFee;
    
    @SerializedName("cod_failed_fee")
    private double codFailedFee;
    
    public double getTotal() {
        return total;
    }
    
    public void setTotal(double total) {
        this.total = total;
    }
    
    public double getServiceFee() {
        return serviceFee;
    }
    
    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }
    
    public double getInsuranceFee() {
        return insuranceFee;
    }
    
    public void setInsuranceFee(double insuranceFee) {
        this.insuranceFee = insuranceFee;
    }
    
    public double getPickStationFee() {
        return pickStationFee;
    }
    
    public void setPickStationFee(double pickStationFee) {
        this.pickStationFee = pickStationFee;
    }
    
    public double getCouponValue() {
        return couponValue;
    }
    
    public void setCouponValue(double couponValue) {
        this.couponValue = couponValue;
    }
    
    public double getR2sFee() {
        return r2sFee;
    }
    
    public void setR2sFee(double r2sFee) {
        this.r2sFee = r2sFee;
    }
    
    public double getReturnAgain() {
        return returnAgain;
    }
    
    public void setReturnAgain(double returnAgain) {
        this.returnAgain = returnAgain;
    }
    
    public double getDocumentReturn() {
        return documentReturn;
    }
    
    public void setDocumentReturn(double documentReturn) {
        this.documentReturn = documentReturn;
    }
    
    public double getDoubleCheck() {
        return doubleCheck;
    }
    
    public void setDoubleCheck(double doubleCheck) {
        this.doubleCheck = doubleCheck;
    }
    
    public double getCodFee() {
        return codFee;
    }
    
    public void setCodFee(double codFee) {
        this.codFee = codFee;
    }
    
    public double getPickRemoteAreasFee() {
        return pickRemoteAreasFee;
    }
    
    public void setPickRemoteAreasFee(double pickRemoteAreasFee) {
        this.pickRemoteAreasFee = pickRemoteAreasFee;
    }
    
    public double getDeliverRemoteAreasFee() {
        return deliverRemoteAreasFee;
    }
    
    public void setDeliverRemoteAreasFee(double deliverRemoteAreasFee) {
        this.deliverRemoteAreasFee = deliverRemoteAreasFee;
    }
    
    public double getCodFailedFee() {
        return codFailedFee;
    }
    
    public void setCodFailedFee(double codFailedFee) {
        this.codFailedFee = codFailedFee;
    }
}

