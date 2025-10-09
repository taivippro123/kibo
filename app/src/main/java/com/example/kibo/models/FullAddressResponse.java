package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class FullAddressResponse {
    @SerializedName("province")
    private Province province;
    
    @SerializedName("district")
    private District district;
    
    @SerializedName("ward")
    private Ward ward;
    
    @SerializedName("fullAddressText")
    private String fullAddressText;

    public FullAddressResponse() {}

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public Ward getWard() {
        return ward;
    }

    public void setWard(Ward ward) {
        this.ward = ward;
    }

    public String getFullAddressText() {
        return fullAddressText;
    }

    public void setFullAddressText(String fullAddressText) {
        this.fullAddressText = fullAddressText;
    }
}

