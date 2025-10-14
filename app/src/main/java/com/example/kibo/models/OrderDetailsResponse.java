package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDetailsResponse {
    @SerializedName("data")
    private List<OrderDetail> data;

    public List<OrderDetail> getData() { return data; }
}


