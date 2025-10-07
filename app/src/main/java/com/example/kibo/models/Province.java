package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class Province {
    @SerializedName("provinceID")
    private int provinceID;
    
    @SerializedName("provinceName")
    private String provinceName;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("countryID")
    private int countryID;

    public Province() {}

    public int getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(int provinceID) {
        this.provinceID = provinceID;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCountryID() {
        return countryID;
    }

    public void setCountryID(int countryID) {
        this.countryID = countryID;
    }
    
    @Override
    public String toString() {
        return provinceName;
    }
}

