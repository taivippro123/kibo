package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class GHNTrackingLog {
    @SerializedName("status")
    private String status;
    
    @SerializedName("payment_type_id")
    private int paymentTypeId;
    
    @SerializedName("trip_code")
    private String tripCode;
    
    @SerializedName("updated_date")
    private String updatedDate;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getTripCode() {
        return tripCode;
    }

    public void setTripCode(String tripCode) {
        this.tripCode = tripCode;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
}

