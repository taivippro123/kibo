package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CategoryResponse {
    @SerializedName("data")
    private List<Category> data;

    public List<Category> getData() {
        return data;
    }
}


