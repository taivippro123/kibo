package com.example.kibo.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("categoryid")
    private int categoryId;

    @SerializedName("categoryname")
    private String categoryName;

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
}


