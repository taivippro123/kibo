package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class District {
    @SerializedName("districtID")
    private int districtID;
    
    @SerializedName("provinceID")
    private int provinceID;
    
    @SerializedName("districtName")
    private String districtName;
    
    @SerializedName("code")
    private int code;

    public District() {}

    public int getDistrictID() {
        return districtID;
    }

    public void setDistrictID(int districtID) {
        this.districtID = districtID;
    }

    public int getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(int provinceID) {
        this.provinceID = provinceID;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return districtName;
    }
}

