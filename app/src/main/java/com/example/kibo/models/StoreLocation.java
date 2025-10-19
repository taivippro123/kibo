package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class StoreLocation {
    @SerializedName("locationid")
    private int locationId;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("address")
    private String address;

    @SerializedName("provinceid")
    private Integer provinceId;

    @SerializedName("districtid")
    private Integer districtId;

    @SerializedName("wardid")
    private Integer wardId;

    @SerializedName("fullAddress")
    private String fullAddress;

    public int getLocationId() { return locationId; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public Integer getProvinceId() { return provinceId; }
    public Integer getDistrictId() { return districtId; }
    public Integer getWardId() { return wardId; }
    public String getFullAddress() { return fullAddress; }
}


