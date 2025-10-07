package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class Ward {
    @SerializedName("wardCode")
    private String wardCode;
    
    @SerializedName("districtID")
    private int districtID;
    
    @SerializedName("wardName")
    private String wardName;

    public Ward() {}

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    public int getDistrictID() {
        return districtID;
    }

    public void setDistrictID(int districtID) {
        this.districtID = districtID;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }
    
    // Parse ward ID from wardCode (assuming it's numeric)
    public int getWardID() {
        try {
            return Integer.parseInt(wardCode);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        return wardName;
    }
}

